import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class TableGenerator implements Generator {

    public enum Action {
        SHIFT, REDUCE
    }

    private Map<String, Set<String>> first;
    private Map<String, Set<List<String>>> sequences;
    private Map<ParserUtils.Production, Integer> productions;

    private static final String ACTION_ENUM = """
            public enum Action {
                    SHIFT, REDUCE
            }""";

    private static final String CELL_RECORD = """
            public record Cell(Action action, int number) {
            }""";


    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        productions = getProductions(ctx);
        first = ParserUtils.getFirst(productions.keySet());
        sequences = getSequences(ctx);
        Map<Set<Item>, StateInfo> states = getStates();
        Map<Set<MergedItem>, StateInfo> merged = mergeStates(states);
//        System.out.println(merged);
        Path outputFile = genDir.resolve(Path.of("Table.java"));
        List<String> tokens = ctx.ruleLexer()
                .stream()
                .map(GrammaticsParser.RuleLexerContext::TERMINAL)
                .map(ParseTree::getText)
                .collect(Collectors.toList());
        tokens.add("$");
        tokens.addAll(ctx.ruleParser()
                .stream()
                .map(GrammaticsParser.RuleParserContext::NONTERMINAL)
                .map(ParseTree::getText)
                .toList());
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String table = String.format("""
                            public class Table {
                            %s
                            }""",
                    String.join(
                            System.lineSeparator(),
                            getTable(merged, tokens),
                            ACTION_ENUM,
                            CELL_RECORD
                    ).indent(4)
            );
            writer.write(table);
        }
    }

    private String getTable(Map<Set<MergedItem>, StateInfo> merged, List<String> tokens) {
        List<Set<MergedItem>> states = new ArrayList<>(Collections.nCopies(merged.size(), null));
        for (var entry : merged.entrySet()) {
            states.set(entry.getValue().id, entry.getKey());
        }
        return String.format("""
                        public static final Cell[][] TABLE = new Cell[][] {
                        //    %s
                        %s
                        };""",
                getTableComment(tokens),
                states.stream()
                        .map(merged::get)
                        .map(info -> getTableRow(info, tokens))
                        .collect(Collectors.joining(",\n"))
                        .indent(4)
        );
    }

    private String getTableComment(List<String> tokens) {
        return tokens.stream().map(token -> String.format("%27s", token)).collect(Collectors.joining(" "));
    }

    private String getTableRow(StateInfo info, List<String> tokens) {
        return String.format("{ %s }", tokens.stream()
                .map(token -> String.format("%27s", getCell(info.transitions.get(token))))
                .collect(Collectors.joining(","))
        );
    }

    private String getCell(ActionNumber actionNumber) {
        if (actionNumber == null) {
            return null;
        }
        return String.format("new Cell(Action.%s, %d)", actionNumber.action, actionNumber.number);
    }

    private record MergedItem(ParserUtils.Production production, int position) {
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            MergedItem that = (MergedItem) o;
            return position == that.position && Objects.equals(production, that.production);
        }

        @Override
        public int hashCode() {
            return Objects.hash(production, position);
        }
    }

    private record ActionNumber(Action action, int number) {
    }

    private record Item(ParserUtils.Production production, int position, String lookAhead) {
    }

    private record StateInfo(int id, Map<String, ActionNumber> transitions) {
    }

    private Map<Set<MergedItem>, StateInfo> mergeStates(Map<Set<Item>, StateInfo> states) {
        List<Set<Item>> id2State = new ArrayList<>(Collections.nCopies(states.size(), null));
        for (var entry : states.entrySet()) {
            id2State.set(entry.getValue().id, entry.getKey());
        }
        Map<Set<Item>, Set<MergedItem>> mapper = new HashMap<>();
        Map<Set<MergedItem>, StateInfo> mergedStates = new HashMap<>();
        for (Set<Item> state : id2State) {
            StateInfo info = states.get(state);
            Set<MergedItem> mergedState = getMergedState(state, mapper, mergedStates);
            for (var transition : info.transitions.entrySet()) {
                if (transition.getValue().action == Action.REDUCE) {
                    mergedStates.get(mergedState).transitions.put(transition.getKey(), transition.getValue());
                    continue;
                }
                Set<MergedItem> mergedStateTo = getMergedState(
                        id2State.get(transition.getValue().number),
                        mapper,
                        mergedStates
                );
                mergedStates.get(mergedState).transitions.put(
                        transition.getKey(),
                        new ActionNumber(Action.SHIFT, mergedStates.get(mergedStateTo).id)
                );
            }
        }
        return mergedStates;
    }

    private Set<MergedItem> getMergedState(
            Set<Item> state,
            Map<Set<Item>, Set<MergedItem>> mapper,
            Map<Set<MergedItem>, StateInfo> mergedStates
    ) {
        if (mapper.containsKey(state)) {
            return mapper.get(state);
        }
        Set<MergedItem> merged = mergeState(state);
        mapper.put(state, merged);
        mergedStates.putIfAbsent(merged, new StateInfo(mergedStates.size(), new HashMap<>()));
        return merged;
    }

    private Set<MergedItem> mergeState(Set<Item> state) {
        return state.stream().map(item -> new MergedItem(item.production, item.position)).collect(Collectors.toSet());
    }

    private Map<Set<Item>, StateInfo> getStates() {
        Map<Set<Item>, StateInfo> states = new HashMap<>();
        Item item = new Item(new ParserUtils.Production("start'", List.of("start")), 0, "$");
        Set<Item> state = new HashSet<>() {{
            add(item);
        }};
        closure(state);
        states.put(state, new StateInfo(0, new HashMap<>()));
        runState(state, states);
        return states;
    }

    private void runState(Set<Item> state, Map<Set<Item>, StateInfo> states) {
        Map<String, List<Item>> groped = state.stream().collect(Collectors.groupingBy(item -> {
            if (item.position < item.production.sequence().size()) {
                return item.production.sequence().get(item.position);
            }
            return "$";
        }));
        for (Map.Entry<String, List<Item>> transition : groped.entrySet()) {
            if (transition.getKey().equals("$")) {
                doReduce(states.get(state), transition.getValue());
                continue;
            }
            Set<Item> newState = transition.getValue()
                    .stream()
                    .map(item -> new Item(item.production, item.position + 1, item.lookAhead))
                    .collect(Collectors.toSet());
            closure(newState);
            if (states.containsKey(newState)) {
                doShift(states.get(state), transition.getKey(), states.get(newState).id);
                continue;
            }
            states.put(newState, new StateInfo(states.size(), new HashMap<>()));
            doShift(states.get(state), transition.getKey(), states.get(newState).id);
//            states.get(state).transitions.put(
//                    transition.getKey(),
//                    new ActionNumber(Action.SHIFT, states.get(newState).id)
//            );
            runState(newState, states);
        }
    }

    private void doShift(StateInfo info, String token, int id) {
        info.transitions.put(
                token,
                new ActionNumber(Action.SHIFT, id)
        );
    }

    private void doReduce(StateInfo info, List<Item> items) {
        for (Item item : items) {
            int productionId = productions.get(item.production);
            info.transitions.put(item.lookAhead, new ActionNumber(Action.REDUCE, productionId));
        }
    }

    private void closure(Set<Item> state) {
        while (true) {
            boolean change = false;
            for (Item item : state.toArray(Item[]::new)) {
                if (item.position >= item.production.sequence().size()) {
                    continue;
                }
                if (ParserUtils.isTerminal(item.production.sequence().get(item.position))) {
                    continue;
                }
                String nonTerminal = item.production.sequence().get(item.position);
                List<String> subSequence = item.production.sequence().subList(
                        item.position + 1,
                        item.production.sequence().size()
                );
                Set<String> firstSet = ParserUtils.getFirst(subSequence, first);
                if (firstSet.contains("")) {
                    firstSet.remove("");
                    firstSet.add(item.lookAhead);
                }
                for (List<String> sequence : sequences.get(nonTerminal)) {
                    for (String lookAhead : firstSet) {
                        change |= state.add(new Item(new ParserUtils.Production(nonTerminal, sequence), 0, lookAhead));
                    }
                }
            }
            if (!change) {
                break;
            }
        }
    }


    private String getHeaders(List<String> tokens) {
        return tokens.stream()
                .map(token -> String.format("%27s", token))
                .collect(Collectors.joining("  "));
    }

    private Map<ParserUtils.Production, Integer> getProductions(GrammaticsParser.GrammaticsContext ctx) {
        Map<ParserUtils.Production, Integer> productions = new HashMap<>();
        productions.put(new ParserUtils.Production("start'", List.of("start")), 0);
        for (GrammaticsParser.RuleParserContext rule : ctx.ruleParser()) {
            rule.alternative().forEach(alternative -> {
                if (alternative.sequence() == null) {
                    productions.put(new ParserUtils.Production(
                                    rule.NONTERMINAL().getText(),
                                    Collections.emptyList()
                            ),
                            productions.size()
                    );
                } else {
                    productions.put(
                            new ParserUtils.Production(
                                    rule.NONTERMINAL().getText(),
                                    alternative.sequence().children.stream().map(ParseTree::getText).toList()
                            ),
                            productions.size()
                    );
                }
            });
        }
        return productions;
    }

    private Map<String, Set<List<String>>> getSequences(GrammaticsParser.GrammaticsContext ctx) {
        Map<String, Set<List<String>>> sequences = new HashMap<>();
        for (GrammaticsParser.RuleParserContext rule : ctx.ruleParser()) {
            sequences.put(rule.NONTERMINAL().getText(), rule
                    .alternative()
                    .stream()
                    .map(alternative -> {
                        if (alternative.sequence() == null) {
                            return Collections.<String>emptyList();
                        } else {
                            return alternative.sequence().children.stream().map(ParseTree::getText).toList();
                        }
                    })
                    .collect(Collectors.toSet()));
        }
        return sequences;
    }
}

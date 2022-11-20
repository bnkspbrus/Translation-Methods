import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TableGenerator implements Generator {

    public enum Action {
        SHIFT, REDUCE
    }

    private record Item(ParserUtils.Production production, int position, Set<String> lookAhead) {

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Item item = (Item) o;
            return position == item.position && Objects.equals(production, item.production);
        }

        boolean merge(Item other) {
            return lookAhead.addAll(other.lookAhead);
        }

        @Override
        public int hashCode() {
            return Objects.hash(production, position);
        }
    }

    private record IDState(State state, int id) {
    }

    private record State(Map<Item, Item> items) { // set<string> is lookAhead
        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            State state = (State) o;
            return Objects.equals(items.keySet(), state.items.keySet());
        }

        public boolean add(Item other) {
            if (items.containsKey(other)) {
                return items.get(other).merge(other);
            }
            items.put(other, other);
            return true;
        }

        public boolean addAll(State other) {
            boolean change = false;
            for (Item item : other.items.values()) {
                change |= add(item);
            }
            return change;
        }

        @Override
        public int hashCode() {
            return Objects.hash(items.keySet());
        }
    }

    private record Cell(Action action, int number) {
        @Override
        public String toString() {
            return String.format("new Cell(Action.%s, %s)", action, number);
        }
    }

    private record Row(IDState idState, Map<String, Cell> cells) {
    }

    private Map<String, Set<String>> first;
    private Map<String, Set<List<String>>> sequences;
    private Map<ParserUtils.Production, Integer> productions;

    private static final String ACTION_CLASS = """
            public enum Action {
                    SHIFT, REDUCE
            }""";

    private static final String CELL_CLASS = """
            public record Cell(Action action, int number) {
            }""";

    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        productions = getProductions(ctx);
        first = ParserUtils.getFirst(productions.keySet());
        sequences = getSequences(ctx);
        Map<State, Row> states = getStates();
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
                                public static final Cell[][] TABLE = new Cell[][] {
                            //        %s
                            %s
                                };
                            %s
                            %s
                            }""",
                    getHeaders(tokens),
                    states.values().stream()
                            .sorted(Comparator.comparingInt(row -> row.idState().id))
                            .map(row -> getTableRow(row, tokens))
                            .collect(Collectors.joining(",\n")).indent(8),
                    ACTION_CLASS.indent(4),
                    CELL_CLASS.indent(4)
            );
            writer.write(table);
        }
    }

    private String getHeaders(List<String> tokens) {
        return tokens.stream()
                .map(token -> String.format("%27s", token))
                .collect(Collectors.joining("  "));
    }

    private String getTableRow(Row row, List<String> tokens) {
        String values = tokens.stream()
                .map(row.cells::get)
                .map(cell -> String.format("%27s", cell))
                .collect(Collectors.joining(", "));
        return String.format("{ %s }", values);
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

    private Map<State, Row> getStates() {
        Map<State, Row> states = new HashMap<>();
        Item item = new Item(new ParserUtils.Production("start'", List.of("start")), 0, new HashSet<>() {{
            add("$");
        }});
        State state = new State(new HashMap<>() {{
            put(item, item);
        }});
        closure(state);
        states.put(state, new Row(new IDState(state, 0), new HashMap<>()));
        runState(state, states);
        return states;
    }

    private void doReduce(Row row, List<Item> items) {
        for (Item item : items) {
            int id = productions.get(item.production);
            item.lookAhead.forEach(token -> row.cells.put(token, new Cell(Action.REDUCE, id)));
        }
    }

    private void doShift(Row row, String token, int id) {
        row.cells.put(token, new Cell(Action.SHIFT, id));
    }

    private void runState(State state, Map<State, Row> states) {
        Map<String, List<Item>> shifts = state.items.keySet().stream().collect(Collectors.groupingBy(item -> {
            List<String> sequence = item.production.sequence();
            return item.position < sequence.size() ? sequence.get(item.position) : "$";
        }));
        for (Map.Entry<String, List<Item>> entry : shifts.entrySet()) {
            if (entry.getKey().equals("$")) {
                doReduce(states.get(state), entry.getValue());
                continue;
            }
            Map<Item, Item> newItems = entry.getValue()
                    .stream()
                    .map(item -> new Item(item.production, item.position + 1, new HashSet<>(item.lookAhead)))
                    .collect(Collectors.toMap(Function.identity(), Function.identity()));
            State newState = new State(newItems);
            closure(newState);
            if (states.containsKey(newState) && !states.get(newState).idState.state.addAll(newState)) {
                continue;
            }
            states.putIfAbsent(newState, new Row(new IDState(newState, states.size()), new HashMap<>()));
            doShift(states.get(state), entry.getKey(), states.get(newState).idState.id);
            runState(newState, states);
        }
    }

    private void closure(State state) {
        while (true) {
            boolean change = false;
            for (Item item : state.items.keySet().toArray(Item[]::new)) {
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
                    firstSet.addAll(item.lookAhead);
                }
                for (List<String> sequence : sequences.get(nonTerminal)) {
                    change |= state.add(new Item(new ParserUtils.Production(nonTerminal, sequence), 0, firstSet));
                }
            }
            if (!change) {
                break;
            }
        }
    }
}

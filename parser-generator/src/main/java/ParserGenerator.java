import org.antlr.v4.runtime.tree.ParseTree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singleton;

public class ParserGenerator implements Generator {

    private record Rule(String nonTerminal, List<String> sequence) {
    }

    private record StateItem(Rule rule, int idx, String lookAhead) {

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            StateItem stateItem = (StateItem) o;
            return idx == stateItem.idx && Objects.equals(rule, stateItem.rule);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rule, idx);
        }
    }

    private final Map<String, List<List<String>>> production = new HashMap<>();
    private final List<Rule> rules = new ArrayList<>();
    private final Map<String, Set<String>> first = new HashMap<>();
    private final Map<String, Set<String>> follow = new HashMap<>();

    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) {
        fillRules(ctx);
        fillProduction(ctx);
        fillFirst();
        fillFollow();
    }



    private void closure(Set<StateItem> state) {
        for (StateItem item : state) {
            if (isTerminal(item.rule.sequence.get(item.idx))) {
                continue;
            }
            String nonTerminal = item.rule.sequence.get(item.idx);
            List<String> subSequence = item.rule.sequence.subList(item.idx + 1, item.rule.sequence.size());
            Set<String> _first = getFirst(subSequence);
            if (_first.contains("")) {
                _first.remove("");
                _first.add(item.lookAhead);
            }
            for (List<String> sequence : production.get(nonTerminal)) {
                for (String newLookAhead : _first) {
                    state.add(new StateItem(new Rule(nonTerminal, sequence), 0, newLookAhead));
                }
            }
        }
    }

    private void fillProduction(GrammaticsParser.GrammaticsContext ctx) {
        production.clear();
        for (GrammaticsParser.RuleParserContext rule : ctx.ruleParser()) {
            production.put(rule.NONTERMINAL().getText(), rule
                    .alternative()
                    .stream()
                    .map(alternative -> {
                        if (alternative.sequence() == null) {
                            return Collections.<String>emptyList();
                        } else {
                            return alternative.sequence().children.stream().map(ParseTree::getText).toList();
                        }
                    })
                    .toList());
        }
    }

    private void fillFollow() {
        follow.clear();
        rules.stream().map(Rule::nonTerminal).forEach(nonTerminal -> follow.put(nonTerminal, new HashSet<>()));
        follow.get("start'").add("$");
        while (true) {
            boolean change = false;
            for (Rule rule : rules) {
                for (int i = 0; i < rule.sequence.size(); i++) {
                    if (isTerminal(rule.sequence.get(i))) {
                        continue;
                    }
                    Set<String> _first = getFirst(rule.sequence.subList(i + 1, rule.sequence.size()));
                    if (!_first.contains("")) {
                        change |= follow.get(rule.sequence.get(i)).addAll(_first);
                    } else {
                        _first.remove("");
                        change |= follow.get(rule.sequence.get(i)).addAll(_first);
                        change |= follow.get(rule.sequence.get(i)).addAll(follow.get(rule.nonTerminal));
                    }
                }
            }
            if (!change) {
                break;
            }
        }
    }

    private void fillRules(GrammaticsParser.GrammaticsContext ctx) {
        Optional<String> optional = ctx.ruleParser()
                .stream()
                .map(GrammaticsParser.RuleParserContext::NONTERMINAL)
                .map(ParseTree::getText)
                .filter(s -> s.equals("start"))
                .findAny();
        if (optional.isEmpty()) {
            throw new AssertionError("start rule not found!");
        }
        rules.clear();
        rules.add(new Rule("start'", List.of("start")));
        for (GrammaticsParser.RuleParserContext rule : ctx.ruleParser()) {
            for (GrammaticsParser.AlternativeContext alternative : rule.alternative()) {
                if (alternative.sequence() == null) {
                    rules.add(new Rule(rule.NONTERMINAL().getText(), Collections.emptyList()));
                } else {
                    rules.add(
                            new Rule(
                                    rule.NONTERMINAL().getText(),
                                    alternative.sequence().children.stream().map(ParseTree::getText).toList()
                            )
                    );
                }
            }
        }
    }

    private void fillFirst() {
        first.clear();
        rules.stream().map(Rule::nonTerminal).forEach(nonTerminal -> first.put(nonTerminal, new HashSet<>()));
        while (true) {
            boolean change = false;
            for (Rule rule : rules) {
                change |= first.get(rule.nonTerminal).addAll(getFirst(rule.sequence));
            }
            if (!change) {
                break;
            }
        }
    }

    private Set<String> getFirst(List<String> sequence) {
        if (sequence.isEmpty()) {
            return new HashSet<>(singleton(""));
        }
        if (isTerminal(sequence.get(0))) {
            return new HashSet<>(singleton(sequence.get(0)));
        }
        Set<String> _first = first.get(sequence.get(0));
        if (!_first.contains("")) {
            return new HashSet<>(_first);
        }
        Set<String> newFirst = new HashSet<>(_first);
        newFirst.remove("");
        newFirst.addAll(getFirst(sequence.subList(1, sequence.size())));
        return newFirst;
    }

    private boolean isTerminal(String element) {
        return Character.isUpperCase(element.charAt(0));
    }
}

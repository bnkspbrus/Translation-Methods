import java.util.*;

import static java.util.Collections.singleton;

public class ParserUtils {

    public record Production(String nonTerminal, List<String> sequence) {
    }

    public record Item(ParserUtils.Production production, int position, Set<String> lookAhead) {

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

    public record IDState(State state, int id) {
    }

    public record State(Map<Item, Item> items) { // set<string> is lookAhead
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

    public static Map<String, Set<String>> getFirst(Set<Production> productions) {
        Map<String, Set<String>> first = new HashMap<>();
        productions.stream()
                .map(Production::nonTerminal)
                .forEach(nonTerminal -> first.put(nonTerminal, new HashSet<>()));
        while (true) {
            boolean change = false;
            for (Production production : productions) {
                change |= first.get(production.nonTerminal).addAll(getFirst(production.sequence, first));
            }
            if (!change) {
                break;
            }
        }
        return first;
    }

    public static Set<String> getFirst(List<String> sequence, Map<String, Set<String>> first) {
        if (sequence.isEmpty()) {
            return new HashSet<>(singleton(""));
        }
        if (isTerminal(sequence.get(0))) {
            return new HashSet<>(singleton(sequence.get(0)));
        }
        Set<String> firstSet = first.get(sequence.get(0));
        if (!firstSet.contains("")) {
            return new HashSet<>(firstSet);
        }
        Set<String> newFirst = new HashSet<>(firstSet);
        newFirst.remove("");
        newFirst.addAll(getFirst(sequence.subList(1, sequence.size()), first));
        return newFirst;
    }

    public static Map<String, Set<String>> getFollow(Set<Production> productions, Map<String, Set<String>> first) {
        Map<String, Set<String>> follow = new HashMap<>();
        productions.stream().map(Production::nonTerminal).forEach(
                nonTerminal -> follow.put(nonTerminal, new HashSet<>()));
        follow.get("start'").add("$");
        while (true) {
            boolean change = false;
            for (Production production : productions) {
                for (int i = 0; i < production.sequence.size(); i++) {
                    if (isTerminal(production.sequence.get(i))) {
                        continue;
                    }
                    Set<String> firstSet = getFirst(production.sequence.subList(i + 1, production.sequence.size()),
                            first);
                    if (!firstSet.contains("")) {
                        change |= follow.get(production.sequence.get(i)).addAll(firstSet);
                    } else {
                        firstSet.remove("");
                        change |= follow.get(production.sequence.get(i)).addAll(firstSet);
                        change |= follow.get(production.sequence.get(i)).addAll(follow.get(production.nonTerminal));
                    }
                }
            }
            if (!change) {
                break;
            }
        }
        return follow;
    }

    public static boolean isTerminal(String element) {
        return Character.isUpperCase(element.charAt(0));
    }
}

public class TreeEvaluator {

    public int evalTree(Tree tree) {
        assert tree.node.equals("S");
        return S(tree, 0);
    }

    private int S(Tree tree, int prev) {
        assert tree.children.size() == 1;
        switch (tree.children.get(0).node) {
            case "E" -> {
                return E(tree.children.get(0), prev);
            }
            case "EMPTY" -> {
                return prev;
            }
            default -> throw new AssertionError();
        }
    }

    private int E(Tree tree, int prev) {
        assert tree.children.size() == 2;
        switch (tree.children.get(0).node) {
            case "T" -> {
                int t = T(tree.children.get(0), prev);
                assert tree.children.get(1).node.equals("E'");
                return EPrime(tree.children.get(1), t);
            }
            default -> throw new AssertionError();
        }
    }

    private int EPrime(Tree tree, int prev) {
        assert tree.children.size() == 1 || tree.children.size() == 3;
        switch (tree.children.get(0).node) {
            case "+" -> {
                assert tree.children.size() == 3;
                assert tree.children.get(1).node.equals("T");
                assert tree.children.get(2).node.equals("E'");
                int t = prev + T(tree.children.get(1), 0);
                return EPrime(tree.children.get(2), t);
            }
            case "-" -> {
                assert tree.children.size() == 3;
                assert tree.children.get(1).node.equals("T");
                assert tree.children.get(2).node.equals("E'");
                int t = prev - T(tree.children.get(1), 0);
                return EPrime(tree.children.get(2), t);
            }
            case Tree.EMPTY -> {
                return prev;
            }
            default -> throw new AssertionError();
        }
    }

    private int T(Tree tree, int prev) {
        assert tree.children.size() == 2;
        switch (tree.children.get(0).node) {
            case "F" -> {
                int f = F(tree.children.get(0), prev);
                assert tree.children.get(1).node.equals("T'");
                return TPrime(tree.children.get(1), f);
            }
            default -> throw new AssertionError();
        }
    }

    private int TPrime(Tree tree, int prev) {
        assert tree.children.size() == 1 || tree.children.size() == 3;
        switch (tree.children.get(0).node) {
            case "*" -> {
                assert tree.children.size() == 3;
                assert tree.children.get(1).node.equals("F");
                assert tree.children.get(2).node.equals("T'");
                int f = prev * F(tree.children.get(1), 0);
                return TPrime(tree.children.get(2), f);
            }
            case Tree.EMPTY -> {
                return prev;
            }
            default -> throw new AssertionError();
        }
    }

    private int F(Tree tree, int prev) {
        assert tree.children.size() >= 1 && tree.children.size() <= 4;
        switch (tree.children.get(0).node) {
            case "(" -> {
                assert tree.children.size() == 3;
                assert tree.children.get(1).node.equals("E");
                assert tree.children.get(2).node.equals(")");
                return E(tree.children.get(1), prev);
            }
            case "-" -> {
                assert tree.children.size() == 2;
                assert tree.children.get(1).node.equals("F");
                return -F(tree.children.get(1), prev);
            }
            default -> {
                if (tree.children.get(0).node.matches("\\d+")) {
                    assert tree.children.size() == 1;
                    return Integer.parseInt(tree.children.get(0).node);
                }
                if (tree.children.get(0).node.matches("[a-zA-Z]+")) {
                    assert tree.children.size() == 4;
                    assert tree.children.get(1).node.equals("(");
                    assert tree.children.get(3).node.equals(")");
                    assert tree.children.get(2).node.equals("E");
                    switch (tree.children.get(0).node) {
                        case "sin" -> {
                            return (int) Math.sin(E(tree.children.get(2), prev));
                        }
                    }
                }
                throw new AssertionError();
            }
        }
    }
}

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Tree {
    public static final String EMPTY = "EMPTY";
    public final int id = sharedTreeCounter++;
    private static int sharedTreeCounter = 0;
    public final String node;
    public List<Tree> children;

    public Tree(String node, Tree... children) {
        this.node = node;
        this.children = Arrays.asList(children);
    }

    public Tree(String node) {
        this.node = node;
    }

    @Override
    public String toString() {
        if (children == null) {
            return node.equals(EMPTY) ? "" : node;
        } else {
            StringBuilder sb = new StringBuilder();
            for (Tree child : children) {
                sb.append(child.toString());
            }
            return sb.toString();
        }
    }

    public String toDotLanguageString() {
        return String.format("digraph {%n%s%n}", edgeListToString());
    }

    private String edgeListToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%d [label=\"%s\"]", id, node));
        if (children != null) {
            for (Tree child : children) {
                sb.append(String.format("%n%d -> %d", id, child.id));
            }
            for (Tree child : children) {
                sb.append(String.format("%n%s", child.edgeListToString()));
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tree tree = (Tree) o;
        return Objects.equals(node, tree.node) && Objects.equals(children, tree.children);
    }
}

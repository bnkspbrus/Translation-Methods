import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TreeGenerator implements Generator {
    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        Path outputFile = genDir.resolve(Path.of("Tree.java"));
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String tree = """
                    import java.util.ArrayList;
                    import java.util.List;
                    import java.util.stream.Collectors;
                    public class Tree {
                        private final String node, text;
                        private final List<Tree> children = new ArrayList<>();
                        public Tree(String node) {
                            this.node = node;
                            text = null;
                        }
                        public Tree(String node, String text) {
                            this.node = node;
                            this.text = text;
                        }
                        public List<Tree> getChildren() {
                            return children;
                        }
                        public String getNode() {
                            return node;
                        }
                        public void addChild(Tree child) {
                            children.add(child);
                        }
                        public String getText() {
                            if (text != null) {
                                return text;
                            }
                            return children.stream().map(Tree::getText).collect(Collectors.joining());
                        }
                    }""";
            writer.write(tree);
        }
    }
}

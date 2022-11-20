import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TreeGenerator implements Generator {
    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        Path outputFile = genDir.resolve(Path.of(ctx.header().TERMINAL().getText() + "Tree.java"));
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String tree = String.format("""
                    import java.util.ArrayList;
                    import java.util.List;
                    import java.util.stream.Collectors;
                    public class %1$sTree {
                        private final String node, text;
                        private final List<%1$sTree> children = new ArrayList<>();
                        public %1$sTree(String node) {
                            this.node = node;
                            text = null;
                        }
                        public %1$sTree(String node, String text) {
                            this.node = node;
                            this.text = text;
                        }
                        public List<%1$sTree> getChildren() {
                            return children;
                        }
                        public String getNode() {
                            return node;
                        }
                        public void addChild(%1$sTree child) {
                            children.add(child);
                        }
                        public String getText() {
                            if (text != null) {
                                return text;
                            }
                            return children.stream().map(%1$sTree::getText).collect(Collectors.joining());
                        }
                    }""", ctx.header().TERMINAL().getText());
            writer.write(tree);
        }
    }
}

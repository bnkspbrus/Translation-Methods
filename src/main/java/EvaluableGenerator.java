import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EvaluableGenerator implements Generator {
    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        Path outputFile = genDir.resolve(Path.of("Evaluable.java"));
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String evaluable = """
                    import java.util.Map;
                                        
                    @FunctionalInterface
                    public interface Evaluable {
                        void evaluate(Tree result, Map<Token, Tree> operands);
                    }""";
            writer.write(evaluable);
        }
    }
}

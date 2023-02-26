import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ParserExceptionGenerator implements Generator {
    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        Path outputFile = genDir.resolve(Path.of("ParserException.java"));
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String lexer = """
                    public class ParserException extends RuntimeException {
                        public ParserException(String message) {
                            super(message);
                        }
                    }""";
            writer.write(lexer);
        }
    }
}

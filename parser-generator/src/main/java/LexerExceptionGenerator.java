import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LexerExceptionGenerator implements Generator {
    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        Path outputFile = genDir.resolve(Path.of("LexerException.java"));
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String lexer = """
                    public class LexerException extends RuntimeException {
                        public LexerException(String message) {
                            super(message);
                        }
                    }""";
            writer.write(lexer);
        }
    }
}

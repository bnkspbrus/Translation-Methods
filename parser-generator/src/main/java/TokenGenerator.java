import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class TokenGenerator implements Generator {

    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        Path tokenFile = genDir.resolve(Path.of(ctx.header().TERMINAL().getText() + "Token.java"));
        try (BufferedWriter writer = Files.newBufferedWriter(tokenFile)) {
            String token = String.format("""
                            public class %sToken {
                                %s
                            }""",
                    ctx.header().TERMINAL().getText(),
                    ctx.ruleLexer().stream().map(GrammaticsParser.RuleLexerContext::TERMINAL).map(
                            ParseTree::getText).collect(
                            Collectors.joining(", ")) + ", END"
            );
            writer.write(token);
        }
    }
}

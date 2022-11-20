import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.Path;

public class Translator {
    public static void main(String[] args) throws IOException {
        CharStream input = CharStreams.fromFileName("Expr.in");
        CommonTokenStream tokens = new CommonTokenStream(new GrammaticsLexer(input));
        GrammaticsParser parser = new GrammaticsParser(tokens);
        GrammaticsParser.GrammaticsContext ctx = parser.grammatics();
//
        Path genDir = Path.of("gen");
        ParserGenerator generator = new ParserGenerator();
        generator.generate(ctx, genDir);
    }
}

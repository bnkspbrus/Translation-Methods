import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class MainGenerator {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("2 arguments was expected. Given " + args.length);
            System.exit(1);
        }
        CharStream input = null;
        try {
            input = CharStreams.fromFileName(args[0]);
        } catch (InvalidPathException e) {
            System.err.println("Invalid path to grammar was given");
            System.exit(1);
        }
        CommonTokenStream tokens = new CommonTokenStream(new GrammaticsLexer(input));
        GrammaticsParser parser = new GrammaticsParser(tokens);
        GrammaticsParser.GrammaticsContext ctx = parser.grammatics();

        Path genDir = null;
        try {
            genDir = Path.of(args[1]);
        } catch (InvalidPathException e) {
            System.err.println("Invalid destination path was given");
            System.exit(1);
        }
        Generator generator = new TableGenerator();
        generator.generate(ctx, genDir);
        Generator generator1 = new TokenGenerator();
        generator1.generate(ctx, genDir);
        Generator generator2 = new LexerGenerator();
        generator2.generate(ctx, genDir);
        Generator generator3 = new TreeGenerator();
        generator3.generate(ctx, genDir);
        Generator generator4 = new ParserGenerator();
        generator4.generate(ctx, genDir);
        Generator generator5 = new ParserExceptionGenerator();
        generator5.generate(ctx, genDir);
        Generator generator6 = new LexerExceptionGenerator();
        generator6.generate(ctx, genDir);
        Generator generator7 = new EvaluableGenerator();
        generator7.generate(ctx, genDir);
    }
}

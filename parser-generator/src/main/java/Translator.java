import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class Translator {
    public static void main(String[] args) throws IOException {
        CharStream input = CharStreams.fromFileName("Expr.in");
        CommonTokenStream tokens = new CommonTokenStream(new GrammaticsLexer(input));
        GrammaticsParser parser = new GrammaticsParser(tokens);
        GrammaticsParser.GrammaticsContext grammatics = parser.grammatics();
        System.out.println(grammatics.ruleParser(0).sequense(1).children);
    }
}

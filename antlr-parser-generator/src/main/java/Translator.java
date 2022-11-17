import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;

public class Translator {
    public static void main(String[] args) throws IOException {
        new Translator().fromFileName("input.in");
    }

    public void fromFileName(String fileName) throws IOException {
        var lexer = new PrefixNotationLexer(CharStreams.fromFileName(fileName));
        var parser = new PrefixNotationParser(new CommonTokenStream(lexer));
        var tree = parser.codeBlock();
        var visitor = new ModifiedVisitor();
        visitor.visit(tree);
    }
}

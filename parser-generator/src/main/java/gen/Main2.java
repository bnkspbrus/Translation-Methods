package gen;

public class Main2 {
    public static void main(String[] args) {
        ExprLexer lexer = new ExprLexer("1 + 2 * 3");
        ExprParser parser = new ExprParser(lexer);
        Tree tree = parser.start();
    }
}

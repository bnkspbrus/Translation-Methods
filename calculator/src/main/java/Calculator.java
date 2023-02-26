public class Calculator {
    public double calculate(String expression) {
        ExprLexer lexer = new ExprLexer(expression);
        ExprParser parser = new ExprParser(lexer);
        return parser.start().val;
    }

    public static void main(String[] args) {
        String expression = args[0];
        System.out.println(new Calculator().calculate(expression));
    }
}

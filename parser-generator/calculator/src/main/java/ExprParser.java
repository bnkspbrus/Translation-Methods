import java.util.EnumMap;
import java.util.List;
import java.util.ArrayList;
public class ExprParser {
    private record Item(int idState, Tree tree) {}
    private record Production(Token nonTerminal, List<Token> sequence) {}
    private final ExprLexer lexer;
    public ExprParser(ExprLexer lexer) {
        this.lexer = lexer;
    }
    private static final Production[] productions = new Production[] {
        new Production(Token.start, List.of(Token.t)),
        new Production(Token.start, List.of(Token.start, Token.PLUS, Token.t)),
        new Production(Token.start, List.of(Token.start, Token.MINUS, Token.t)),
        new Production(Token.t, List.of(Token.f)),
        new Production(Token.t, List.of(Token.t, Token.STAR, Token.f)),
        new Production(Token.f, List.of(Token.NUMBER)),
        new Production(Token.f, List.of(Token.OBRACKET, Token.start, Token.CBRACKET)),
        new Production(Token.f, List.of(Token.MINUS, Token.f)),
        new Production(Token.f, List.of(Token.FUNC, Token.OBRACKET, Token.start, Token.CBRACKET)),
        new Production(Token.f, List.of(Token.f, Token.FACTORIAL))
    
    };
    private static final Evaluable[] evaluates = new Evaluable[] {
        (result, operands) -> {result.val = operands.get(Token.t).val;},
        (result, operands) -> {result.val = operands.get(Token.start).val + operands.get(Token.t).val;},
        (result, operands) -> {result.val = operands.get(Token.start).val - operands.get(Token.t).val;},
        (result, operands) -> {result.val = operands.get(Token.f).val;},
        (result, operands) -> {result.val = operands.get(Token.t).val * operands.get(Token.f).val;},
        (result, operands) -> {result.val = Integer.parseInt(operands.get(Token.NUMBER).getText());},
        (result, operands) -> {result.val = operands.get(Token.start).val;},
        (result, operands) -> {result.val = - operands.get(Token.f).val;},
        (result, operands) -> { if (operands.get(Token.FUNC).getText().equals("sin")) result.val = Math.sin(operands.get(Token.start).val); else result.val = 0;},
        (result, operands) -> {result.val = 1;
                                                                                                                                                                                                                                                                                 for (int i = 2; i <= operands.get(Token.f).val; i++)
                                                                                                                                                                                                                                                                                     result.val = result.val * i;
                                                                                                                                                                                                                                                                                 }
    
    };
    public Tree start() {
        List<Item> stack = new ArrayList<>();
        stack.add(new Item(0, new Tree("start'")));
        lexer.nextToken();
        while (true) {
            Token token = lexer.curToken();
            int idState = stack.get(stack.size() - 1).idState;
            Table.Cell cell = Table.TABLE[idState][token.ordinal()];
            if (cell == null) {
                if (token == Token.$) {
                    throw new ParserException("Unexpected EOF");
                } else {
                    throw new ParserException("Unexpected string: " + lexer.curString());
                }
            }
            switch (cell.action()) {
                case SHIFT -> {
                    stack.add(new Item(cell.number(), new Tree(lexer.curToken().name(), lexer.curString())));
                    lexer.nextToken();
                }
                case REDUCE -> {
                    if (cell.number() == 0) {
                        return stack.get(stack.size() - 1).tree;
                    }
                    Production production = productions[cell.number() - 1];
                    Token nonTerminal = production.nonTerminal;
                    Tree tree = new Tree(nonTerminal.name());
                    EnumMap<Token, Tree> operands = new EnumMap<>(Token.class);
                    int from = stack.size() - production.sequence.size();
                    for (int i = from; i < stack.size(); i++) {
                        tree.addChild(stack.get(i).tree);
                        operands.put(production.sequence.get(i - from), stack.get(i).tree);
                    }
                    evaluates[cell.number() - 1].evaluate(tree, operands);
                    stack = stack.subList(0, stack.size() - production.sequence.size());
                    Table.Cell shift = Table.TABLE[stack.get(stack.size() - 1).idState][nonTerminal.ordinal()];
                    stack.add(new Item(shift.number(), tree));
                }
            }
        }
    }

}
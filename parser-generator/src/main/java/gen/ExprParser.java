package gen;

import java.util.ArrayList;
import java.util.List;
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
        new Production(Token.t, List.of(Token.f)),
        new Production(Token.t, List.of(Token.t, Token.STAR, Token.f)),
        new Production(Token.f, List.of(Token.NUMBER)),
        new Production(Token.f, List.of(Token.OBRACKET, Token.start, Token.CBRACKET))
    
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
                    stack.add(new Item(cell.number(), new Tree(lexer.nextToken().name())));
                    lexer.nextToken();
                }
                case REDUCE -> {
                    if (cell.number() == 0) {
                        return stack.get(stack.size() - 1).tree;
                    }
                    Production production = productions[cell.number() - 1];
                    Token nonTerminal = production.nonTerminal;
                    Tree tree = new Tree(nonTerminal.name());
                    for (int i = stack.size() - production.sequence.size(); i < stack.size(); i++) {
                        tree.addChild(stack.get(i).tree);
                    }
                    stack = stack.subList(0, stack.size() - production.sequence.size());
                    Table.Cell shift = Table.TABLE[stack.get(stack.size() - 1).idState][nonTerminal.ordinal()];
                    stack.add(new Item(shift.number(), tree));
                }
            }
        }
    }

}
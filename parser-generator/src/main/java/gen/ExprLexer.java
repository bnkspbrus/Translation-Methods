package gen;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ExprLexer {
    private static final String REGEX = "(?<STAR>\\*)|(?<PLUS>\\+)|(?<OBRACKET>\\()|(?<CBRACKET>\\))|(?<NUMBER>[1-9][0-9]*)|\\s+";
    private static final List<Token> TERMINALS = List.of(Token.STAR, Token.PLUS, Token.OBRACKET, Token.CBRACKET, Token.NUMBER);
    private final Matcher matcher;
    private Token curToken;
    public ExprLexer(CharSequence cs) {
        Pattern pattern = Pattern.compile(REGEX);
        matcher = pattern.matcher(cs);
    }
    public Token curToken() {
        return curToken;
    }
    public String curString() {
        return matcher.group();
    }
    public Token nextToken() {
            while (!matcher.hitEnd()) {
                if (!matcher.lookingAt()) {
                    throw new LexerException("Unexpected token");
                }
                Token nextToken = null;
                for (Token token : TERMINALS) {
                    if (matcher.group(token.name()) != null) {
                        nextToken = token;
                        break;
                    }
                }
                matcher.region(matcher.end(), matcher.regionEnd());
                if (nextToken != null) {
                    return curToken = nextToken;
                }
            }
            return curToken = Token.$;
        }

}
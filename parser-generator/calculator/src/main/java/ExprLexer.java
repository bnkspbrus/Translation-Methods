import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ExprLexer {
    private static final String REGEX = "(?<FACTORIAL>!)|(?<PLUS>\\+)|(?<MINUS>-)|(?<STAR>\\*)|(?<FUNC>[a-z]+)|(?<NUMBER>[1-9]*[0-9]+)|(?<OBRACKET>\\()|(?<CBRACKET>\\))|\\s+";
    private static final List<Token> TERMINALS = List.of(Token.FACTORIAL, Token.PLUS, Token.MINUS, Token.STAR, Token.FUNC, Token.NUMBER, Token.OBRACKET, Token.CBRACKET);
    private final Matcher matcher;
    private Token curToken;
    private String curString;
    public ExprLexer(CharSequence cs) {
        Pattern pattern = Pattern.compile(REGEX);
        matcher = pattern.matcher(cs);
    }
    public Token curToken() {
        return curToken;
    }
    public String curString() {
        return curString;
    }
    public void nextToken() {
        while (matcher.regionEnd() - matcher.regionStart() > 0) {
            if (!matcher.lookingAt()) {
                throw new LexerException("Unexpected token at position " + matcher.regionStart());
            }
            Token nextToken = null;
            for (Token token : TERMINALS) {
                if ((curString = matcher.group(token.name())) != null) {
                    nextToken = token;
                    break;
                }
            }
            matcher.region(matcher.end(), matcher.regionEnd());
            if (nextToken != null) {
                curToken = nextToken;
                return;
            }
        }
        curToken = Token.$;
    }

}
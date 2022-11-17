import java.io.InputStream;
import java.text.ParseException;

public class Parser {

    LexicalAnalyzer lex;

    private Tree S() throws ParseException {
        switch (lex.curToken()) {
            case MINUS, NUMBER, OBRACKET, FUNCTION -> {
                Tree e = E();
                return new Tree("S", e);
            }
            case END -> {
                return new Tree("S", new Tree(Tree.EMPTY));
            }
            default -> throw new AssertionError();
        }
    }

    private Tree E() throws ParseException {
        switch (lex.curToken()) {
            case MINUS, NUMBER, OBRACKET, FUNCTION -> {
                Tree t = T();
                Tree ePrime = EPrime();
                return new Tree("E", t, ePrime);
            }
            default -> throw new AssertionError();
        }
    }

    private Tree EPrime() throws ParseException {
        switch (lex.curToken()) {
            case PLUS -> {
                lex.nextToken();
                Tree t = T();
                Tree ePrime = EPrime();
                return new Tree("E'", new Tree("+"), t, ePrime);
            }
            case MINUS -> {
                lex.nextToken();
                Tree t = T();
                Tree ePrime = EPrime();
                return new Tree("E'", new Tree("-"), t, ePrime);
            }
            case CBRACKET, END, COMMA -> {
                return new Tree("E'", new Tree(Tree.EMPTY));
            }
            default -> throw new AssertionError();
        }
    }

    private Tree T() throws ParseException {
        switch (lex.curToken()) {
            case MINUS, NUMBER, OBRACKET, FUNCTION -> {
                Tree f = F();
                Tree tPrime = TPrime();
                return new Tree("T", f, tPrime);
            }
            default -> throw new AssertionError();
        }
    }

    private Tree TPrime() throws ParseException {
        switch (lex.curToken()) {
            case MULTIPLY -> {
                lex.nextToken();
                Tree f = F();
                Tree tPrime = TPrime();
                return new Tree("T'", new Tree("*"), f, tPrime);
            }
            case PLUS, MINUS, END, CBRACKET, COMMA -> {
                return new Tree("T'", new Tree(Tree.EMPTY));
            }
            default -> throw new AssertionError();
        }
    }

    private Tree F() throws ParseException {
        switch (lex.curToken()) {
            case NUMBER -> {
                String num = lex.curSubString();
                lex.nextToken();
                return new Tree("F", new Tree(num));
            }
            case OBRACKET -> {
                lex.nextToken();
                Tree e = E();
                lex.nextToken();
                return new Tree("F", new Tree("("), e, new Tree(")"));
            }
            case FUNCTION -> {
                String fun = lex.curSubString();
                lex.nextToken();
                lex.nextToken();
                Tree e = E();
                Tree a = A();
                lex.nextToken();
                return new Tree("F", new Tree(fun), new Tree("("), e, a, new Tree(")"));
            }
            case MINUS -> {
                lex.nextToken();
                Tree f = F();
                return new Tree("F", new Tree("-"), f);
            }
            default -> throw new AssertionError();
        }
    }

    private Tree A() throws ParseException {
        switch (lex.curToken()) {
            case COMMA -> {
                lex.nextToken();
                Tree e = E();
                return new Tree("A", new Tree(","), e);
            }
            case OBRACKET -> {
                return new Tree("A", new Tree(Tree.EMPTY));
            }
            default -> throw new AssertionError();
        }
    }

    Tree parse(InputStream is) throws ParseException {
        lex = new LexicalAnalyzer(is);
        lex.nextToken();
        return S();
    }

    public String parseToDotLanguage(InputStream is) throws ParseException {
        Tree parsed = parse(is);
        return parsed.toDotLanguageString();
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(new Parser().parseToDotLanguage(System.in));
//        System.out.println(
//                new Parser().parse(new ByteArrayInputStream("(1+2)*sin(-3*(7-4)+2)".getBytes(StandardCharsets
//                .UTF_8))));
    }
}

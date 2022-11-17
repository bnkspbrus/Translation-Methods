import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public class LexicalAnalyzer {
    private final InputStream is;
    private int curChar;
    private int curPos;
    private Token curToken;

    private String curSubString;

    public LexicalAnalyzer(InputStream is) throws ParseException {
        this.is = is;
        curPos = 0;
        nextChar();
    }

    private void nextChar() throws ParseException {
        curPos++;
        try {
            curChar = is.read();
        } catch (IOException e) {
            throw new ParseException(e.getMessage(), curPos);
        }
    }

    public void nextToken() throws ParseException {
        while (Character.isWhitespace(curChar)) {
            nextChar();
        }
        switch (curChar) {
            case ',' -> {
                nextChar();
                curToken = Token.COMMA;
                curSubString = ",";
            }
            case '(' -> {
                nextChar();
                curToken = Token.OBRACKET;
                curSubString = "(";
            }
            case ')' -> {
                nextChar();
                curToken = Token.CBRACKET;
                curSubString = ")";
            }
            case '+' -> {
                nextChar();
                curToken = Token.PLUS;
                curSubString = "+";
            }
            case '-' -> {
                nextChar();
                curToken = Token.MINUS;
                curSubString = "-";
            }
            case '*' -> {
                nextChar();
                curToken = Token.MULTIPLY;
                curSubString = "*";
            }
            case -1 -> curToken = Token.END;
            default -> {
                if (Character.isDigit(curChar)) {
                    StringBuilder sb = new StringBuilder();
                    while (curChar != -1 && Character.isDigit(curChar)) {
                        sb.append((char) curChar);
                        nextChar();
                    }
                    curToken = Token.NUMBER;
                    curSubString = sb.toString();
                } else if (Character.isLetter(curChar)) {
                    StringBuilder sb = new StringBuilder();
                    while (curChar != -1 && Character.isLetter(curChar)) {
                        sb.append((char) curChar);
                        nextChar();
                    }
                    curToken = Token.FUNCTION;
                    curSubString = sb.toString();
                } else {
                    throw new ParseException("Illegal character " + (char) curChar, curPos);
                }
            }
        }
    }

    public int curPos() {
        return curPos;
    }

    public Token curToken() {
        return curToken;
    }

    public String curSubString() {
        return curSubString;
    }
}
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ParserTest {

    private static final Tree EMPTY_TREE = new Tree("S", new Tree(Tree.EMPTY));
    private final Random RANDOM = new Random();

    private final char[] WHITESPACES = new char[]{' ', '\r', '\n', '\t'};


    @Test
    public void testExampleFromTaskDescription() throws ParseException {
        Tree parsed = new Parser().
                parse(new ByteArrayInputStream("(1+2)*sin(-3*(7-4)+2)".getBytes(StandardCharsets.UTF_8)));
        int expected = (1 + 2) * ((int) Math.sin(-3 * (7 - 4) + 2));
        assertEquals(expected, new TreeEvaluator().evalTree(parsed));
    }

    @Test
    public void testBlankInput() throws ParseException {
        String input = "     \n   \t   ";
        Tree parsed = new Parser().parse(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        assertEquals(EMPTY_TREE, parsed);
        assertEquals(0, new TreeEvaluator().evalTree(parsed));
    }

    @Test
    public void testNumberInDoubleBrackets() throws ParseException {
        String input = "(((((1)))))";
        Tree parsed = new Parser().parse(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Tree expected = packNumberInExpression(1);
        for (int i = 0; i < 5; i++) {
            expected = packExpressionInBrackets(expected);
        }
        expected = addStartRootToExpression(expected);
        assertEquals(expected, parsed);
    }

    @Test
    public void testNumberWithManyMinuses() throws ParseException {
        String input = "-----1";
        Tree parsed = new Parser().parse(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        int evaluated = new TreeEvaluator().evalTree(parsed);
        assertEquals(-1, evaluated);
    }

    @Test
    public void testOperationPriority() throws ParseException {
        String input = "1+2*-sin(-3)";
        Tree actual = new Parser().parse(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        int evaluated = new TreeEvaluator().evalTree(actual);
        assertEquals(1 + 2 * -((int) Math.sin(-3)), evaluated);
    }

    @Test
    public void testManyUnaryOperations() throws ParseException {
        String input = "sin(sin(sin(1)))";
        Tree actual = new Parser().parse(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        Tree expected =
                addStartRootToExpression(
                        packExpressionInSine(
                                packExpressionInSine(
                                        packExpressionInSine(
                                                packNumberInExpression(1)))));
        assertEquals(expected, actual);
    }

    private Tree packNegatedNumberInExpression(int number) {
        return new Tree("E",
                new Tree("T",
                        new Tree("F",
                                new Tree("-"),
                                new Tree("F",
                                        new Tree(Integer.toString(number)))),
                        new Tree("T'",
                                new Tree(Tree.EMPTY))),
                new Tree("E'",
                        new Tree(Tree.EMPTY)));
    }

    private Tree packExpressionInSine(Tree expr) {
        return new Tree(
                "E",
                new Tree("T",
                        new Tree(
                                "F",
                                new Tree("sin"),
                                new Tree("("),
                                expr,
                                new Tree(")")),
                        new Tree("T'",
                                new Tree(Tree.EMPTY))),
                new Tree("E'",
                        new Tree(Tree.EMPTY)));
    }

    private Tree addStartRootToExpression(Tree expr) {
        return new Tree("S", expr);
    }

    private Tree packNumberInExpression(int number) {
        return new Tree("E",
                new Tree("T",
                        new Tree("F",
                                new Tree(Integer.toString(number))),
                        new Tree("T'",
                                new Tree(Tree.EMPTY))),
                new Tree("E'",
                        new Tree(Tree.EMPTY)));
    }

    private Tree packExpressionInBrackets(Tree expr) {
        return new Tree(
                "E",
                new Tree("T",
                        new Tree(
                                "F",
                                new Tree("("),
                                expr,
                                new Tree(")")),
                        new Tree("T'",
                                new Tree(Tree.EMPTY))),
                new Tree("E'",
                        new Tree(Tree.EMPTY)));
    }

    private String getRandomWhitespace() {
        return Character.toString(WHITESPACES[RANDOM.nextInt(WHITESPACES.length)]);
    }

    private String getRandomBlankString(int size) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ) {
            String whitespaces = getRandomWhitespace().repeat(RANDOM.nextInt(size - i + 1));
            sb.append(whitespaces);
            i += whitespaces.length();
        }
        return sb.toString();
    }
}

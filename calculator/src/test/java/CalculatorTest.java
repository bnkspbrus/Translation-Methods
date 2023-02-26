import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {


    private static Stream<Arguments> provideExpressions() {
        return Stream.of(
                Arguments.of("1 + 2 * 3", 1 + 2 * 3),
                Arguments.of("(1 + 2) * 3", (1 + 2) * 3),
                Arguments.of("(1) * (2) * (3)", (2) * (3)),
                Arguments.of("1 - 2 - 3", 1 - 2 - 3),
                Arguments.of("- 1", -1),
                Arguments.of("----1", 1),
                Arguments.of("sin(1)", Math.sin(1)),
                Arguments.of("1 - 2 - 3", 1 - 2 - 3),
                Arguments.of("1 !", 1),
                Arguments.of("(1 + 2 + 3) !", factorial(1 + 2 + 3)),
                Arguments.of("3!!", 720)
        );
    }

    private static long factorial(int n) {
        long fact = 1;
        for (int i = 2; i <= n; i++) {
            fact = fact * i;
        }
        return fact;
    }

    @ParameterizedTest
    @MethodSource("provideExpressions")
    void calculate(String expression, double expected) {
        double actual = new Calculator().calculate(expression);
        assertEquals(expected, actual);
    }
}
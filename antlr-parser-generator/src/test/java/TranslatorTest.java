import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslatorTest {

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    private static Stream<Arguments> provideFileNames() {
        return Stream.of(
                Arguments.of("input.in", """
                        {
                        if (2 > 3) {
                        print(3)
                        } elif (4 > 7 - 2) {
                        print(3 + 4)
                        }
                        }"""),
                Arguments.of("input2.in", """
                        {
                        if (((2 > (3)))) {
                        print((3))
                        } elif ((4 > 7 - 2)) {
                        print(((3) + 4))
                        }
                        }"""),
                Arguments.of("input3.in", """
                        {
                        if (2 > 3) {
                        id = 2
                        } else id = 3
                        }"""),
                Arguments.of("input4.in", """
                        {
                        s = (8 + (3 + (99 * 30 / 20)))
                        }"""),
                Arguments.of("input5.in", """
                        {
                        s = ---1
                        }""")
        );
    }

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @ParameterizedTest
    @MethodSource("provideFileNames")
    void fromFileName(String fileName, String expected) throws IOException {
        new Translator().fromFileName(fileName);
        assertEquals(expected, outputStreamCaptor.toString()
                .trim());
    }
}
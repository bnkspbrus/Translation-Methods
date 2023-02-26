import java.util.Map;

@FunctionalInterface
public interface Evaluable {
    void evaluate(Tree result, Map<Token, Tree> operands);
}
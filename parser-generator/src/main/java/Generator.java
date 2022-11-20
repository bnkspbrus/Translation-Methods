import java.io.IOException;
import java.nio.file.Path;

public interface Generator {
    void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException;
}

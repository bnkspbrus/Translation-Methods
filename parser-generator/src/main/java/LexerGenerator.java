import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class LexerGenerator implements Generator {

    private static final String IMPORTS = """
            import java.util.regex.Pattern;
            import java.util.regex.Matcher;
            import java.text.ParseException;""";

    private static final String MATCHER_FIELD = "private final Matcher matcher;";

    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        Path outputFile = genDir.resolve(Path.of(ctx.header().TERMINAL().getText() + "Lexer.java"));
        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            String lexer = String.format("""
                            %s
                            public class %sLexer {
                            %s
                            }""",
                    IMPORTS,
                    ctx.header().TERMINAL().getText(),
                    String.join(System.lineSeparator(),
                            getRegexField(ctx),
                            MATCHER_FIELD,
                            getCurTokenField(ctx),
                            getCharSequenceConstructor(ctx),
                            getCurTokenMethod(ctx),
                            getNextTokenMethod(ctx)
                    ).indent(4));
            writer.write(lexer);
        }
    }

    private String getNextTokenMethod(GrammaticsParser.GrammaticsContext ctx) {
        return String.format("""
                public %1$sToken nextToken() throws ParseException {
                        while (!matcher.hitEnd()) {
                            if (!matcher.lookingAt()) {
                                throw new ParseException(null, matcher.regionStart());
                            }
                            %1$sToken nextToken = null;
                            for (%1$sToken token : %1$sToken.values()) {
                                if (token == %1$sToken.END) {
                                    continue;
                                }
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
                        return curToken = %1$sToken.END;
                    }""", ctx.header().TERMINAL().getText());
    }

    private String getCurTokenMethod(GrammaticsParser.GrammaticsContext ctx) {
        return String.format("""
                public %sToken curToken() {
                    return curToken;
                }""", ctx.header().TERMINAL().getText());
    }

    private String getCurTokenField(GrammaticsParser.GrammaticsContext ctx) {
        return String.format("private %sToken curToken;", ctx.header().TERMINAL().getText());
    }

    private String getRegexField(GrammaticsParser.GrammaticsContext ctx) {
        return String.format("private static final String REGEX = \"%s\";",
                ctx.ruleLexer()
                        .stream()
                        .map(rule -> {
                            String regExpr = rule.REGEXPR().getText();
                            return "(?<" + rule.TERMINAL() + ">" + regExpr.substring(1, regExpr.length() - 1) + ")";
                        })
                        .collect(Collectors.joining("|"))
                        + "|\\\\s+"
        );
    }

    private String getCharSequenceConstructor(GrammaticsParser.GrammaticsContext ctx) {
        return String.format("""
                public %sLexer(CharSequence cs) {
                    Pattern pattern = Pattern.compile(REGEX);
                    matcher = pattern.matcher(cs);
                }""", ctx.header().TERMINAL().getText()
        );
    }
}

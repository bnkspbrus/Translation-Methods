import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class LexerGenerator implements Generator {

    private static final String IMPORTS = """
            import java.util.List;
            import java.util.regex.Matcher;
            import java.util.regex.Pattern;""";

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
                            getTerminals(ctx),
                            MATCHER_FIELD,
                            CUR_TOKEN_FIELD,
                            getCharSequenceConstructor(ctx),
                            CUR_TOKEN_METHOD,
                            CUR_STRING_METHOD,
                            GET_NEXT_TOKEN_METHOD
                    ).indent(4));
            writer.write(lexer);
        }
    }

    private String getTerminals(GrammaticsParser.GrammaticsContext ctx) {
        return String.format(
                "private static final List<Token> TERMINALS = List.of(%s);",
                ctx.ruleLexer()
                        .stream()
                        .map(GrammaticsParser.RuleLexerContext::TERMINAL)
                        .map(ParseTree::getText)
                        .map(terminal -> String.format("Token.%s", terminal))
                        .collect(Collectors.joining(", "))
        );
    }

    private static final String CUR_STRING_METHOD = """
            public String curString() {
                return matcher.group();
            }""";

    private static final String GET_NEXT_TOKEN_METHOD = """
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
                }""";

    private static final String CUR_TOKEN_METHOD = """
            public Token curToken() {
                return curToken;
            }""";

    private static final String CUR_TOKEN_FIELD = "private Token curToken;";

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

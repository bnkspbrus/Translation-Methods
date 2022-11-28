import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class ParserGenerator implements Generator {
    private static final String ITEM_RECORD = "private record Item(int idState, Tree tree) {}";
    private static final String PRODUCTION_RECORD = "private record Production(Token nonTerminal, List<Token>" +
            " " +
            "sequence) {}";
    private static final String LEXER_FIELD = "private final %sLexer lexer;";
    private static final String LEXER_CONSTRUCTOR = """
            public %1$sParser(%1$sLexer lexer) {
                this.lexer = lexer;
            }""";
    private static final String IMPORTS = """
            import java.util.List;
            import java.util.ArrayList;""";

    @Override
    public void generate(GrammaticsParser.GrammaticsContext ctx, Path genDir) throws IOException {
        Path tokenFile = genDir.resolve(Path.of(ctx.header().TERMINAL().getText() + "Parser.java"));
        try (BufferedWriter writer = Files.newBufferedWriter(tokenFile)) {
            String parser = String.format("""
                            %s
                            public class %sParser {
                            %s
                            }""",
                    IMPORTS,
                    ctx.header().TERMINAL().getText(),
                    String.join(
                            System.lineSeparator(),
                            ITEM_RECORD,
                            PRODUCTION_RECORD,
                            getLexerField(ctx),
                            getLexerConstructor(ctx),
                            getProductions(ctx),
                            START_METHOD
                    ).indent(4)
            );
            writer.write(parser);
        }
    }

    private String getLexerConstructor(GrammaticsParser.GrammaticsContext ctx) {
        return String.format(LEXER_CONSTRUCTOR, ctx.header().TERMINAL().getText());
    }

    private String getLexerField(GrammaticsParser.GrammaticsContext ctx) {
        return String.format(LEXER_FIELD, ctx.header().TERMINAL().getText());
    }

    private static final String START_METHOD = """
            public Tree start() {
                List<Item> stack = new ArrayList<>();
                stack.add(new Item(0, new Tree("start'")));
                lexer.nextToken();
                while (true) {
                    Token token = lexer.curToken();
                    int idState = stack.get(stack.size() - 1).idState;
                    Table.Cell cell = Table.TABLE[idState][token.ordinal()];
                    if (cell == null) {
                        if (token == Token.$) {
                            throw new ParserException("Unexpected EOF");
                        } else {
                            throw new ParserException("Unexpected string: " + lexer.curString());
                        }
                    }
                    switch (cell.action()) {
                        case SHIFT -> {
                            stack.add(new Item(cell.number(), new Tree(lexer.curToken().name())));
                            lexer.nextToken();
                        }
                        case REDUCE -> {
                            if (cell.number() == 0) {
                                return stack.get(stack.size() - 1).tree;
                            }
                            Production production = productions[cell.number() - 1];
                            Token nonTerminal = production.nonTerminal;
                            Tree tree = new Tree(nonTerminal.name());
                            for (int i = stack.size() - production.sequence.size(); i < stack.size(); i++) {
                                tree.addChild(stack.get(i).tree);
                            }
                            stack = stack.subList(0, stack.size() - production.sequence.size());
                            Table.Cell shift = Table.TABLE[stack.get(stack.size() - 1).idState][nonTerminal.ordinal()];
                            stack.add(new Item(shift.number(), tree));
                        }
                    }
                }
            }""";

    private String getProductions(GrammaticsParser.GrammaticsContext ctx) {
        return String.format("""
                        private static final Production[] productions = new Production[] {
                        %s
                        };""",
                ctx.ruleParser().stream()
                        .map(this::getProduction)
                        .collect(Collectors.joining(",\n"))
                        .indent(4)
        );
    }

    private String getProduction(GrammaticsParser.RuleParserContext ctx) {
        String nonTerminal = ctx.NONTERMINAL().getText();
        return ctx.alternative().stream()
                .map(alternative -> getAlternative(nonTerminal, alternative))
                .collect(Collectors.joining(",\n"));
    }

    private String getAlternative(String nonTerminal, GrammaticsParser.AlternativeContext ctx) {
        return String.format("new Production(Token.%s, %s)", nonTerminal, getSequence(ctx.sequence().children));
    }

    private String getSequence(List<ParseTree> sequence) {
        return String.format(
                "List.of(%s)",
                sequence.stream()
                        .map(elem -> String.format("Token.%s", elem.getText()))
                        .collect(Collectors.joining(", ")));
    }
}

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashSet;
import java.util.Set;

public class ModifiedVisitor extends PrefixNotationBaseVisitor<Void> {

    private final Set<String> variables = new HashSet<>();

    @Override
    public Void visit(ParseTree tree) {
        variables.clear();
        return super.visit(tree);
    }

    @Override
    public Void visitCodeBlock(PrefixNotationParser.CodeBlockContext ctx) {
        System.out.println("{");
        super.visitCodeBlock(ctx);
        System.out.print("\n}");
        return null;
    }

    @Override
    public Void visitAssign(PrefixNotationParser.AssignContext ctx) {
        variables.add(ctx.ID().getText());
        System.out.print(ctx.ID().getText() + " = ");
        visitExpression(ctx.expression());
        return null;
    }

    @Override
    public Void visitPrint(PrefixNotationParser.PrintContext ctx) {
        System.out.print("print(");
        visitExpression(ctx.expression());
        System.out.print(")");
        return null;
    }

    @Override
    public Void visitBranch(PrefixNotationParser.BranchContext ctx) {
        System.out.print("if (");
        visitExpressionLogical(ctx.expressionLogical());
        System.out.print(") ");
        visitCodeBlock(ctx.codeBlock());
        if (ctx.elifBlock() != null) {
            visitElifBlock(ctx.elifBlock());
        } else {
            visitElseBlock(ctx.elseBlock());
        }
        return null;
    }

    @Override
    public Void visitElifBlock(PrefixNotationParser.ElifBlockContext ctx) {
        if (ctx.children != null) {
            System.out.print(" el");
        }
        return super.visitElifBlock(ctx);
    }

    @Override
    public Void visitElseBlock(PrefixNotationParser.ElseBlockContext ctx) {
        if (ctx.children != null) {
            System.out.print(" else ");
        }
        return super.visitElseBlock(ctx);
    }

    @Override
    public Void visitCompare(PrefixNotationParser.CompareContext ctx) {
        visitOperandArithmetical(ctx.operandArithmetical(0));
        System.out.print(" " + ctx.SIGN_COMPARE().getText() + " ");
        visitOperandArithmetical(ctx.operandArithmetical(1));
        return null;
    }

    @Override
    public Void visitSingleVariable(PrefixNotationParser.SingleVariableContext ctx) {
        if (!variables.contains(ctx.ID().getText())) {
            throw new AssertionError("Unknown variable: " + ctx.ID().getText());
        }
        return super.visitSingleVariable(ctx);
    }

    @Override
    public Void visitTerminal(TerminalNode node) {
        System.out.print(node.getText());
        return null;
    }

    @Override
    public Void visitArithmetical(PrefixNotationParser.ArithmeticalContext ctx) {
        if (ctx.operandArithmetical().size() == 2) {
            visitOperandArithmetical(ctx.operandArithmetical(0));
            System.out.print(" " + ctx.SIGN_ARITHMETICAL().getText() + " ");
            visitOperandArithmetical(ctx.operandArithmetical(1));
        } else {
            System.out.print(ctx.SIGN_ARITHMETICAL().getText());
            visitOperandArithmetical(ctx.operandArithmetical(0));
        }
        return null;
    }

    @Override
    public Void visitLogical(PrefixNotationParser.LogicalContext ctx) {
        if (ctx.operandLogical().size() == 2) {
            visitOperandLogical(ctx.operandLogical(0));
            System.out.print(" " + ctx.SIGN_LOGICAL().getText() + " ");
            visitOperandLogical(ctx.operandLogical(1));
        } else {
            System.out.print(ctx.SIGN_LOGICAL().getText());
            visitOperandLogical(ctx.operandLogical(0));
        }
        return null;
    }

}

package cool.compiler;

import cool.parser.CoolParser;
import cool.parser.CoolParserBaseVisitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ASTConstructionVisitor extends CoolParserBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitClass(CoolParser.ClassContext ctx) {
        var classNode = new ClassNode();
        classNode.ctx = ctx;
        classNode.id = ctx.type.getText();
        for (var feat : ctx.feats) {
            Feature feature = (Feature) visit(feat);
            classNode.features.add(feature);
        }
        if (ctx.parent != null) {
            classNode.parent = ctx.parent.getText();
        }
        return classNode;
    }

    @Override
    public ASTNode visitMethod(CoolParser.MethodContext ctx) {
        var method = new Method();
        method.ctx = ctx;
        method.name = ctx.name.getText();
        method.type = ctx.type.getText();
        method.body = (Expression) visit(ctx.body);
        for (var arg : ctx.args) {
            method.formals.add((Formal) visit(arg));
        }
        return method;
    }

    @Override
    public ASTNode visitClass_var(CoolParser.Class_varContext ctx) {
        var attribute = new Attribute();
        attribute.ctx = ctx;
        attribute.name = ctx.var_decl().name.getText();
        attribute.type = ctx.var_decl().type.getText();
        if (ctx.var_decl().init != null) {
            attribute.init = (Expression) visit(ctx.var_decl().init);
        }
        return attribute;
    }

    @Override
    public ASTNode visitFormal(CoolParser.FormalContext ctx) {
        var formal = new Formal();
        formal.ctx = ctx;
        formal.name = ctx.name.getText();
        formal.type = ctx.type.getText();
        return formal;
    }

    @Override
    public ASTNode visitNew(CoolParser.NewContext ctx) {
        var newOperation = new NewOp();
        newOperation.ctx = ctx;
        newOperation.type = ctx.type.getText();
        return newOperation;
    }

    @Override
    public ASTNode visitCompl(CoolParser.ComplContext ctx) {
        var unary = new IntNeg();
        unary.op = "~";
        unary.ctx = ctx;
        unary.operand = (Expression) visit(ctx.expr());
        return unary;
    }

    @Override
    public ASTNode visitNot(CoolParser.NotContext ctx) {
        var unary = new BoolNeg();
        unary.op = "not";
        unary.ctx = ctx;
        unary.operand = (Expression) visit(ctx.expr());
        return unary;
    }

    @Override
    public ASTNode visitIsvoid(CoolParser.IsvoidContext ctx) {
        var unary = new isVoid();
        unary.op = "isvoid";
        unary.ctx = ctx;
        unary.operand = (Expression) visit(ctx.expr());
        return unary;
    }

    @Override
    public ASTNode visitMultDiv(CoolParser.MultDivContext ctx) {
        var binary = new BinaryOp();
        binary.op = ctx.op.getText();
        binary.ctx = ctx;
        binary.opToken = ctx.op;
        binary.leftToken = ctx.left.start;
        binary.rightToken = ctx.right.start;
        binary.operand1 = (Expression) visit(ctx.left);
        binary.operand2 = (Expression) visit(ctx.right);
        return binary;
    }

    @Override
    public ASTNode visitPlusMinus(CoolParser.PlusMinusContext ctx) {
        var binary = new BinaryOp();
        binary.op = ctx.op.getText();
        binary.ctx = ctx;
        binary.opToken = ctx.op;
        binary.leftToken = ctx.left.start;
        binary.rightToken = ctx.right.start;
        binary.operand1 = (Expression) visit(ctx.left);
        binary.operand2 = (Expression) visit(ctx.right);
        return binary;
    }

    @Override
    public ASTNode visitRelational(CoolParser.RelationalContext ctx) {
        var binary = new BinaryOp();
        binary.op = ctx.op.getText();
        binary.ctx = ctx;
        binary.opToken = ctx.op;
        binary.leftToken = ctx.left.start;
        binary.rightToken = ctx.right.start;
        binary.operand1 = (Expression) visit(ctx.left);
        binary.operand2 = (Expression) visit(ctx.right);
        return binary;
    }

    @Override
    public ASTNode visitAssignment(CoolParser.AssignmentContext ctx) {
        var assignment = new Assignment();
        var var = new Variable();
        var.name = ctx.name.getText();
        var.ctx = ctx;
        assignment.var = var;
        assignment.ctx = ctx;
        assignment.expr = (Expression) visit(ctx.expr());
        return assignment;
    }

    @Override
    public ASTNode visitExpldispatch(CoolParser.ExpldispatchContext ctx) {
        var expldisp = new ExplDispatch();
        expldisp.method = ctx.name.getText();
        expldisp.ctx = ctx;
        if (ctx.static_method != null) {
            expldisp.static_method = ctx.static_method.getText();
        }
        expldisp.object = (Expression) visit(ctx.object);
        expldisp.paramtokens = ctx.params.stream().map((context) -> context.start).collect(Collectors.toList());

        for (var param : ctx.params) {
            expldisp.params.add((Expression) visit(param));
        }
        return expldisp;
    }

    @Override
    public ASTNode visitImpldispatch(CoolParser.ImpldispatchContext ctx) {
        var impldisp = new ImplDispatch();
        impldisp.method = ctx.name.getText();
        impldisp.ctx = ctx;
        impldisp.paramtokens = ctx.params.stream().map((context) -> context.start).collect(Collectors.toList());

        for (var param : ctx.params) {
            impldisp.params.add((Expression) visit(param));
        }
        return impldisp;
    }

    @Override
    public ASTNode visitWhile(CoolParser.WhileContext ctx) {
        var loop = new Loop();
        loop.ctx = ctx;
        loop.cond = (Expression) visit(ctx.cond);
        loop.body = (Expression) visit(ctx.body);
        return loop;
    }

    @Override
    public ASTNode visitIf(CoolParser.IfContext ctx) {
        var decision = new Decision();
        decision.ctx = ctx;
        decision.cond = (Expression) visit(ctx.cond);
        decision.thenExpr = (Expression) visit(ctx.thenBranch);
        decision.elseExpr = (Expression) visit(ctx.elseBranch);
        return decision;
    }

    @Override
    public ASTNode visitCase(CoolParser.CaseContext ctx) {
        var caseNode = new Case();
        caseNode.ctx = ctx;
        caseNode.expr = (Expression) visit(ctx.main);
        for (var branch : ctx.branches) {
            caseNode.branches.add((CaseBranch) visit(branch));
        }
        return caseNode;
    }

    @Override
    public ASTNode visitCase_branch(CoolParser.Case_branchContext ctx) {
        var caseBranch = new CaseBranch();
        caseBranch.ctx = ctx;
        caseBranch.name = ctx.name.getText();
        caseBranch.type = ctx.type.getText();
        caseBranch.body = (Expression) visit(ctx.body);
        return caseBranch;
    }

    @Override
    public ASTNode visitLet(CoolParser.LetContext ctx) {
        var let = new Let();
        let.ctx = ctx;
        let.body = (Expression) visit(ctx.body);
        for (var var : ctx.vars) {
            var local = new Local();
            local.name = var.name.getText();
            local.type = var.type.getText();
            if (var.init != null) {
                local.init = (Expression) visit(var.init);
            }
            let.locals.add(local);
        }
        return let;
    }

    @Override
    public ASTNode visitBlock(CoolParser.BlockContext ctx) {
        var block = new Block();
        block.ctx = ctx;
        for (var expr : ctx.expressions) {
            block.expressions.add((Expression) visit(expr));
        }
        return block;
    }

    @Override
    public ASTNode visitParen_expr(CoolParser.Paren_exprContext ctx) {
        return visit(ctx.e);
    }

    @Override
    public ASTNode visitVariable(CoolParser.VariableContext ctx) {
        var var = new Variable();
        var.ctx = ctx;
        var.name = ctx.getText();
        return var;
    }

    @Override
    public ASTNode visitString(CoolParser.StringContext ctx) {
        var literal = new Literal();
        literal.ctx = ctx;
        String str = ctx.getText().substring(1, ctx.getText().length() - 1);

        StringBuffer myStringBuffer = new StringBuffer();
        Pattern pattern = Pattern.compile("(\\\\(.|[ \f\t\n\r]))");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            String g = matcher.group(1);
            String replacement =
            switch (g) {
                case "\\t" -> "\t";
                case "\\n" -> "\n";
                case "\\r" -> "\r";
                case "\\\\" -> "\\\\";
                default    -> g.substring(1,2);
            };
            matcher.appendReplacement(myStringBuffer, replacement);
        }
        matcher.appendTail(myStringBuffer);
        literal.val = myStringBuffer.toString();

        literal.type = Literal.LiteralType.STRING;
        return literal;
    }

    @Override
    public ASTNode visitIntBool(CoolParser.IntBoolContext ctx) {
        var literal = new Literal();
        literal.val = ctx.getText();
        literal.ctx = ctx;
        if (literal.val.equals("false") || literal.val.equals("true"))
            literal.type = Literal.LiteralType.BOOL;
        else
            literal.type = Literal.LiteralType.INT;
        return literal;
    }

    @Override
    public ASTNode visitProgram(CoolParser.ProgramContext ctx) {
        var program = new Program();
        for (var cl : ctx.class_()) {
            program.classes.add((ClassNode) visit(cl));
        }
        return program;
    }
}

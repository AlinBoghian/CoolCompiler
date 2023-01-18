package cool.compiler;

import cool.parser.CoolParser;
import cool.structures.Scope;
import cool.structures.TypeSymbol;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class ASTNode {

    public ParserRuleContext ctx;

    public abstract String serialize(int level);

    public String indentedLine(int level, String str) {
        return "  ".repeat(level) + str + System.lineSeparator();
    }
    public <T> T accept(ASTVisitor<T> visitor) {
        return null;
    }
}

class Program extends ASTNode {
    ArrayList<ClassNode> classes = new ArrayList<>();

    Scope scope;
    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder()
                .append(" ".repeat(level))
                .append("program")
                .append(System.lineSeparator());
        for (var clazz : classes) {
            final_string.append(clazz.serialize(level + 1));
        }
        return final_string.toString();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Feature extends ASTNode {
    public String name;
    public String type;
}
class Method extends Feature {
    ArrayList<Formal> formals = new ArrayList<>();
    Expression body;
    Scope scope;
    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder()
                .append(indentedLine(level, "method"))
                .append(indentedLine(level + 1, name));
        for (var formal : formals) {
            final_string.append(formal.serialize(level + 1));
        }
        final_string.append(indentedLine(level + 1, type)).append(body.serialize(level + 1));
        return final_string.toString();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Formal extends ASTNode {

    public String name;
    public String type;

    @Override
    public String serialize(int level) {
        return indentedLine(level, "formal") +
                indentedLine(level + 1, name) +
                indentedLine(level + 1, type);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Attribute extends Feature {

    Expression init;
    boolean hasErr = false;

    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder()
                .append(indentedLine(level,"attribute"))
                .append(indentedLine(level + 1, name))
                .append(indentedLine(level + 1, type));
        if (init != null) {
            final_string.append(init.serialize(level + 1));
        }
        return final_string.toString();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ClassNode extends ASTNode {
    public String tag;
    String id;
    String parent;
    ArrayList<Feature> features = new ArrayList<>();

    public TypeSymbol symbol;
    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder()
                .append(indentedLine(level, "class"))
                .append(indentedLine(level + 1, id))
                .append(parent == null ? "" : indentedLine(level + 1, parent));
        for (var feature : features) {
            final_string.append(feature.serialize(level + 1));
        }
        return final_string.toString();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

abstract class Expression extends ASTNode{}

class Literal extends Expression {

    String val;
    enum LiteralType {STRING, BOOL, INT};
    public LiteralType type;
    @Override
    public String serialize(int level) {
        return indentedLine(level,val);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Assignment extends Expression {
    Variable var;
    Expression expr;
    @Override
    public String serialize(int level) {
        return indentedLine(level, "<-") + var.serialize(level + 1) + expr.serialize(level + 1);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Variable extends Expression {
    String name;
    @Override
    public String serialize(int level) {
        return indentedLine(level, name);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class  BinaryOp extends Expression {
    public String op;
    public Expression operand1, operand2;
    public Token opToken;
    public Token leftToken;
    public Token rightToken;
    @Override
    public String serialize(int level) {
        return indentedLine(level,op) + operand1.serialize(level + 1) + operand2.serialize(level + 1);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class BoolNeg extends UnaryOp {
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class IntNeg extends UnaryOp {
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class isVoid extends UnaryOp {
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
class NewOp extends Expression
{
    String type;
    @Override
    public String serialize(int level) {
        return indentedLine(level,"new " + type);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
abstract class UnaryOp extends  Expression {
    String op;
    Expression operand;
    @Override
    public String serialize(int level) {
        return indentedLine(level,op) + operand.serialize(level + 1);
    }
}

class ExplDispatch extends Expression {
    public String method;
    public String static_method;
    Expression object;
    ArrayList<Expression> params = new ArrayList<>();
    List<Token> paramtokens;

    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder()
                .append(indentedLine(level,"."))
                .append(object.serialize(level + 1))
                .append(static_method == null ? "" : indentedLine(level + 1, static_method))
                .append(indentedLine(level + 1,method));
        for (var param : params) {
            final_string.append(param.serialize(level + 1));
        }
        return final_string.toString();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class ImplDispatch extends  Expression {

    String method;
    ArrayList<Expression> params = new ArrayList<>();
    List<Token> paramtokens;


    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder()
                .append(indentedLine(level,"implicit dispatch"))
                .append(indentedLine(level + 1,method));
        for (var param : params) {
            final_string.append(param.serialize(level + 1));
        }
        return final_string.toString();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Decision extends Expression {

    Expression cond;
    Expression thenExpr;
    Expression elseExpr;
    @Override
    public String serialize(int level) {
        return indentedLine(level, "if")
                + cond.serialize(level + 1)
                + thenExpr.serialize(level + 1)
                + elseExpr.serialize(level + 1);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Loop extends Expression {
    Expression cond;
    Expression body;
    @Override
    public String serialize(int level) {
        return indentedLine(level,"while")
                + cond.serialize(level + 1)
                + body.serialize(level + 1);
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Let extends Expression {

    ArrayList<Local> locals = new ArrayList<>();
    Expression body;

    Scope scope;
    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder().append(indentedLine(level,"let"));
        for (var local : locals) {
            final_string.append(local.serialize(level + 1));
        }
        final_string.append(body.serialize(level + 1));
        return final_string.toString();
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Local extends ASTNode {
    String name;
    String type;
    Expression init;

    @Override
    public String serialize(int level) {
        return indentedLine(level,"local")
                + indentedLine(level + 1, name)
                + indentedLine(level + 1, type)
                + (init == null ? "" : init.serialize(level + 1));
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Case extends Expression {

    ArrayList<CaseBranch> branches = new ArrayList<>();
    Expression expr;
    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder()
                .append(indentedLine(level, "case"))
                .append(expr.serialize(level + 1));
        for (var br : branches) {
            final_string.append(br.serialize(level + 1));
        }
        return final_string.toString();
    }


    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }

}

class CaseBranch extends ASTNode {

    String name;
    String type;
    Expression body;

    Scope scope;
    @Override
    public String serialize(int level) {
        return indentedLine(level, "case branch")
                + indentedLine(level + 1, name)
                + indentedLine(level + 1, type)
                + body.serialize(level + 1);
    }


    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}

class Block extends Expression {

    ArrayList<Expression> expressions = new ArrayList<>();

    @Override
    public String serialize(int level) {
        StringBuilder final_string = new StringBuilder().append(indentedLine(level, "block"));
        for (var expr : expressions) {
            final_string.append(expr.serialize(level + 1));
        }
        return final_string.toString();
    }


    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}





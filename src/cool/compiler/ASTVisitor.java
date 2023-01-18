package cool.compiler;

import org.antlr.v4.runtime.Token;

public interface ASTVisitor<T> {
    T visit(Method method);
    T visit(Formal formal);
    T visit(Attribute attribute);
    T visit(ClassNode classNode);
    T visit(Literal literal);
    T visit(Assignment assignment);
    T visit(Variable variable);
    T visit(IntNeg intNeg);
    T visit(BoolNeg intNeg);
    T visit(isVoid isVoid);
    T visit(ExplDispatch explDispatch);
    T visit(ImplDispatch implDispatch);
    T visit(Decision decision);
    T visit(Loop loop);
    T visit(Let let);
    T visit(Program program);
    T visit(Block block);
    T visit(Case caseNode);
    T visit(CaseBranch caseBranch);

    public static void error(Token token, String message) {
        System.err.println("line " + token.getLine()
                + ":" + (token.getCharPositionInLine() + 1)
                + ", " + message);
    }

    T visit(Local local);
    T visit(BinaryOp binaryOp);
    T visit(NewOp newOp);
}

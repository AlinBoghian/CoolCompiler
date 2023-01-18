package cool.compiler;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;

public class CodeGenPassVisitor implements ASTVisitor<ST>{

    static STGroupFile templates = new STGroupFile("cgen.stg");
    static int tagCounter = 4;

    @Override
    public ST visit(Method method) {
        return null;
    }
    @Override
    public ST visit(Formal formal) {
        return null;
    }

    @Override
    public ST visit(Attribute attribute) {
        return null;
    }

    @Override
    public ST visit(ClassNode classNode) {
        classNode.tag = "_" + classNode.id + "_tag";
        templates.getInstanceOf("tag")
                .add("tag", classNode.tag)
                .add("counter", tagCounter);
        var prototypeTemplate = templates.getInstanceOf("protObj");
        String attributesString = "";
        for(var feat : classNode.features) {
            if (feat instanceof Attribute) {
                attributesString = attributesString + feat.accept(this).toString();
            }
        }
        prototypeTemplate.add("attributes", attributesString)
                .add("tag", tagCounter)
                .add("dispatchPtr", classNode.id + "_dispTab");

        tagCounter++;
        return null;
    }

    @Override
    public ST visit(Literal literal) {
        return null;
    }

    @Override
    public ST visit(Assignment assignment) {
        return null;
    }

    @Override
    public ST visit(Variable variable) {
        return null;
    }

    @Override
    public ST visit(IntNeg intNeg) {
        return null;
    }

    @Override
    public ST visit(BoolNeg intNeg) {
        return null;
    }

    @Override
    public ST visit(isVoid isVoid) {
        return null;
    }

    @Override
    public ST visit(ExplDispatch explDispatch) {
        return null;
    }

    @Override
    public ST visit(ImplDispatch implDispatch) {
        return null;
    }

    @Override
    public ST visit(Decision decision) {
        return null;
    }

    @Override
    public ST visit(Loop loop) {
        return null;
    }

    @Override
    public ST visit(Let let) {
        return null;
    }

    @Override
    public ST visit(Program program) {
        return null;
    }

    @Override
    public ST visit(Block block) {
        return null;
    }

    @Override
    public ST visit(Case caseNode) {
        return null;
    }

    @Override
    public ST visit(CaseBranch caseBranch) {
        return null;
    }

    @Override
    public ST visit(Local local) {
        return null;
    }

    @Override
    public ST visit(BinaryOp binaryOp) {
        return null;
    }

    @Override
    public ST visit(NewOp newOp) {
        return null;
    }
}

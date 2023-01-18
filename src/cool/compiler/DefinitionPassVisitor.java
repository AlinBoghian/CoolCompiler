package cool.compiler;

import cool.parser.CoolParser;
import cool.structures.*;

public class  DefinitionPassVisitor implements ASTVisitor<Void> {
    public Scope currentScope;
    public TypeSymbol currentClass;
    @Override
    public Void visit(Program program) {
        SymbolTable.defineBasicClasses();
        currentScope = SymbolTable.globals;
        for (var cl : program.classes) {
            cl.accept(this);
        }
        return null;
    }

    @Override
    public Void visit(Block block) {
        return null;
    }

    @Override
    public Void visit(Case caseNode) {
        return null;
    }

    @Override
    public Void visit(CaseBranch caseBranch) {
        return null;
    }

    @Override
    public Void visit(Local local) {
        return null;
    }

    @Override
    public Void visit(Method method) {

        if (currentClass.methodsScope.lookup(method.name) != null) {
            String err = "Class " + currentClass.getName() + " redefines method " + method.name;
            SymbolTable.error(method.ctx, ((CoolParser.MethodContext) method.ctx).name, err);
        }
        MethodSymbol funSymb = new MethodSymbol(currentScope, method.name);
        funSymb.type = method.type;
        method.scope = funSymb;
        currentClass.methodsScope.add(funSymb);

        for (var formal : method.formals) {
            var formSym = new IdSymbol(formal.name);
            formSym.type = formal.type;
            if (!method.scope.add(formSym)) {
                String err = "Method " + method.name + " of class " + currentClass.getName() + " redefines formal parameter " + formal.name;
                SymbolTable.error(formal.ctx, ((CoolParser.FormalContext) formal.ctx).name, err);
            }
        }

        return null;
    }

    @Override
    public Void visit(Attribute attribute) {
        if (currentClass.lookup(attribute.name) != null) {
            String err = "Class " + currentClass.getName() + " redefines attribute " + attribute.name;
            SymbolTable.error(attribute.ctx, ((CoolParser.Class_varContext) attribute.ctx).var_decl().name, err);
            attribute.hasErr = true;
        }
        if (attribute.name.equals("self")) {
            String err = "Class " + currentClass.getName() + " has attribute with illegal name self";
            SymbolTable.error(attribute.ctx, ((CoolParser.Class_varContext) attribute.ctx).var_decl().name, err);
            attribute.hasErr = true;
        }

        var idSym = new IdSymbol(attribute.name);
        idSym.type = attribute.type;
        currentClass.attributesScope.add(idSym);
        return null;
    }

    @Override
    public Void visit(ClassNode classNode) {

        if (classNode.id.equals("SELF_TYPE")) {
            String err = "Class has illegal name SELF_TYPE";
            SymbolTable.error(classNode.ctx, ((CoolParser.ClassContext) classNode.ctx).type, err);
        }
        else if (currentScope.lookup(classNode.id) != null) {
            String err = "Class "+classNode.id+" is redefined";
            SymbolTable.error(classNode.ctx, ((CoolParser.ClassContext) classNode.ctx).type, err);
        }

        classNode.symbol = new TypeSymbol(classNode.id);
        if (classNode.parent != null) {
            classNode.symbol.superClass = classNode.parent;
        }
        currentClass = classNode.symbol;
        SymbolTable.globals.add(classNode.symbol);

        for (var feat : classNode.features) {
            feat.accept(this);
        }

        return null;
    }

    @Override
    public Void visit(Formal formal) {
        return null;
    }

    @Override
    public Void visit(Literal literal) {
        return null;
    }

    @Override
    public Void visit(Assignment assignment) {
        return null;

    }

    @Override
    public Void visit(Variable variable) {
        return null;
    }

    @Override
    public Void visit(IntNeg intNeg) {
        return null;
    }

    @Override
    public Void visit(BoolNeg intNeg) {
        return null;
    }

    @Override
    public Void visit(NewOp newOp) {
        return null;
    }

    @Override
    public Void visit(isVoid isVoid) {
        return null;
    }

    @Override
    public Void visit(BinaryOp binaryOp) {
        return null;
    }

    @Override
    public Void visit(ExplDispatch explDispatch) {
        return null;
    }

    @Override
    public Void visit(ImplDispatch implDispatch) {
        return null;
    }

    @Override
    public Void visit(Decision decision) {
        return null;
    }

    @Override
    public Void visit(Loop loop) {
        return null;
    }

    @Override
    public Void visit(Let let) {
        return null;
    }
}
package cool.compiler;

import cool.parser.CoolParser;
import cool.structures.*;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;

public class ResolutionPassVisitor implements ASTVisitor<TypeSymbol> {

    public Scope currentScope;
    public MethodSymbol currentMethod;
    public TypeSymbol currentClass;


    MethodSymbol getOverriden(TypeSymbol classSymbol,MethodSymbol methodSymbol) {

        while (classSymbol.superClass != null) {
            classSymbol = (TypeSymbol) SymbolTable.globals.lookup(classSymbol.superClass);
            MethodSymbol overridenMethodSym = (MethodSymbol) classSymbol.methodsScope.lookup(methodSymbol.getName());
            if (overridenMethodSym != null)
                return overridenMethodSym;
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Method method) {
        currentMethod = (MethodSymbol) method.scope;
        currentMethod.setParent(currentClass);
        TypeSymbol typeSymbol = (TypeSymbol) SymbolTable.globals.lookup(method.type);
        if (typeSymbol == null) {
            String err = "Class " + currentClass.getName() + " has method "
                    + method.name + " with undefined return type " + method.type;
            SymbolTable.error(method.ctx, ((CoolParser.MethodContext) method.ctx).type, err);
        }
        currentMethod.type = method.type;
        currentScope = currentMethod;
        for (var formal : method.formals) {
            if (formal.name.equals("self")) {
                String err = "Method "+ method.name+ " of class " + currentClass.getName() + " has formal parameter with illegal name self";
                SymbolTable.error(formal.ctx, ((CoolParser.FormalContext)formal.ctx).name,err);
            }
            if (formal.type.equals("SELF_TYPE")) {
                String err = "Method "+ method.name+ " of class " + currentClass.getName() + " has formal parameter "
                        + formal.name + " with illegal type SELF_TYPE";
                SymbolTable.error(formal.ctx, ((CoolParser.FormalContext)formal.ctx).type,err);
            }

            var typeSym = SymbolTable.globals.lookup(formal.type);
            if (typeSym == null) {
                String err = "Method " + method.name + " of class " + currentClass.getName() + " has formal parameter " + formal.name + " with undefined type " + formal.type;
                SymbolTable.error(formal.ctx, ((CoolParser.FormalContext)formal.ctx).type,err);
            }
        }

        var overridenMethod = getOverriden(currentClass, currentMethod);
        if (overridenMethod != null) {
            if (!currentMethod.type.equals(overridenMethod.type)) {
                String err = "Class "+ currentClass.getName() + " overrides method " + method.name + " but changes return type from "
                        + overridenMethod.type + " to " +currentMethod.type;
                SymbolTable.error(method.ctx, ((CoolParser.MethodContext)method.ctx).type,err);
            }
            if (currentMethod.getFormals().size() != overridenMethod.getFormals().size()) {
                String err = "Class "+ currentClass.getName() + " overrides method " + method.name + " with different number of formal parameters";
                SymbolTable.error(method.ctx, ((CoolParser.MethodContext)method.ctx).name,err);
            }
            else {
                var iterCurrent = currentMethod.getFormals().values().iterator();
                var iterOverriden = overridenMethod.getFormals().values().iterator();
                int i = 0;
                while (iterCurrent.hasNext()) {
                    IdSymbol thisSym = (IdSymbol) iterCurrent.next();
                    IdSymbol overSym = (IdSymbol) iterOverriden.next();

                    if (!overSym.type.equals(thisSym.type)) {
                        String err = "Class "+currentClass.getName()+" overrides method "+currentMethod.getName()+" but changes type of formal parameter " +
                                thisSym.getName() + " from " + overSym.type + " to " + thisSym.type;
                        CoolParser.FormalContext ctxt = (CoolParser.FormalContext) method.formals.get(i).ctx;
                        SymbolTable.error(method.ctx, ctxt.type,err);
                    }
                    i++;
                }
            }
        }

        TypeSymbol bodyType = method.body.accept(this);
        if (bodyType == TypeSymbol.SELFTYPE && typeSymbol != TypeSymbol.SELFTYPE)
            bodyType = currentClass;
//        if (typeSymbol == TypeSymbol.SELFTYPE)
//            typeSymbol = currentClass;

        if (bodyType != null && !isSubClass(typeSymbol,bodyType)) {
            String err = "Type " + bodyType.getName() + " of the body of method " + method.name +
                    " is incompatible with declared return type " + method.type;
            SymbolTable.error(method.ctx, ((CoolParser.MethodContext)method.ctx).body.start,err);
        }
        return typeSymbol;
    }

    @Override
    public TypeSymbol visit(Formal formal) {
        return null;
    }

    boolean checkRedefine(TypeSymbol classSymbol,String attrName) {

        while (classSymbol.superClass != null) {
            classSymbol = (TypeSymbol) SymbolTable.globals.lookup(classSymbol.superClass);
            if (classSymbol.lookup(attrName) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TypeSymbol visit(Attribute attribute) {
        if (attribute.hasErr)
            return null;
        TypeSymbol attrType = (TypeSymbol) SymbolTable.globals.lookup(attribute.type);
        if (checkRedefine(currentClass, attribute.name)) {
            String err = "Class "+currentClass.getName()+" redefines inherited attribute " + attribute.name;
            SymbolTable.error(attribute.ctx, ((CoolParser.Class_varContext) attribute.ctx).var_decl().name, err);
        }
        else if (attrType == null) {
            String err = "Class " + currentClass.getName() + " has attribute " + attribute.name +
                    " with undefined type " + attribute.type;
            SymbolTable.error(attribute.ctx, ((CoolParser.Class_varContext) attribute.ctx).var_decl().type, err);
        }
        else if (attribute.init != null) {
            TypeSymbol initType = attribute.init.accept(this);
            if (initType != null && !isSubClass(attrType,initType)) {
                String err = "Type " + initType.getName() + " of initialization expression of attribute " +
                        attribute.name + " is incompatible with declared type " + attrType.getName();
                SymbolTable.error(attribute.ctx, attribute.init.ctx.start, err);
            }
        }
        return null;
    }

    boolean checkCycle(TypeSymbol classSymbol) {
        String name = classSymbol.getName();

        while (classSymbol.superClass != null) {
            if (name.equals(classSymbol.superClass)) {
                return true;
            }
            classSymbol = (TypeSymbol) SymbolTable.globals.lookup(classSymbol.superClass);
            if (classSymbol == null)
                return false;
        }
        return false;

    }
    @Override
    public TypeSymbol visit(ClassNode classNode) {
        TypeSymbol classType = (TypeSymbol) SymbolTable.globals.lookup(classNode.id);
        currentClass = classType;
        currentScope = currentClass;
        if(classNode.parent != null) {
            TypeSymbol parentType = (TypeSymbol) SymbolTable.globals.lookup(classNode.parent);
            if (classNode.parent.equals("Int") ||
                classNode.parent.equals("String") ||
                classNode.parent.equals("Bool")  ||
                classNode.parent.equals("SELF_TYPE")) {

                String err = "Class "+classNode.id+" has illegal parent "+classNode.parent;
                SymbolTable.error(classNode.ctx, ((CoolParser.ClassContext) classNode.ctx).parent, err);
            }
            if (parentType == null) {
                String err = "Class "+classNode.id+" has undefined parent "+classNode.parent;
                SymbolTable.error(classNode.ctx, ((CoolParser.ClassContext) classNode.ctx).parent, err);
            }
            else {
                currentClass.attributesScope.setParent(parentType.attributesScope);
                if (checkCycle(classType)) {
                    String err = "Inheritance cycle for class "+classNode.id;
                    SymbolTable.error(classNode.ctx, ((CoolParser.ClassContext) classNode.ctx).type, err);
                }
            }
        }
        else {
            classType.superClass = "Object";
        }
        for (var feat : classNode.features) {
            feat.accept(this);
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Literal literal) {
        switch (literal.type) {
            case STRING -> {
                return TypeSymbol.STRING;
            }
            case BOOL -> {
                return TypeSymbol.BOOL;
            }
            case INT -> {
                return TypeSymbol.INT;
            }
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Assignment assignment) {
        if (assignment.var.name.equals("self")) {
            String err = "Cannot assign to self";
            SymbolTable.error(assignment.ctx, ((CoolParser.AssignmentContext) assignment.ctx).name, err);
            return null;
        }
        TypeSymbol varType = assignment.var.accept(this);
        TypeSymbol assignedType = assignment.expr.accept(this);


        if (varType != null && assignedType != null) {
            String assignedStr = assignedType.getName();
            String varStr = varType.getName();
            if (assignedType == TypeSymbol.SELFTYPE)
                assignedType = currentClass;
            if (varType == TypeSymbol.SELFTYPE)
                varType = currentClass;
            if (!isSubClass(varType,assignedType)) {
                String err = "Type " + assignedStr+ " of assigned expression is incompatible with declared type "
                        + varStr + " of identifier " + assignment.var.name;
                SymbolTable.error(assignment.ctx, ((CoolParser.AssignmentContext) assignment.ctx).expr().start, err);
                return null;
            }
            return assignedType;
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Variable variable) {
        if (variable.name.equals("self")) {
            return TypeSymbol.SELFTYPE;
        }
        IdSymbol id = (IdSymbol) currentScope.lookup(variable.name);
        if (id == null) {
            String err = "Undefined identifier " + variable.name;
            SymbolTable.error(variable.ctx, variable.ctx.start, err);
            return null;
        }
        var varType = (TypeSymbol) SymbolTable.globals.lookup(id.type);
        return  varType;
    }

    @Override
    public TypeSymbol visit(BinaryOp binaryOp) {
        TypeSymbol retType;
        if (binaryOp.op.equals("<") || binaryOp.op.equals("=") || binaryOp.op.equals("<="))
            retType = TypeSymbol.BOOL;
        else
            retType = TypeSymbol.INT;

        TypeSymbol typeOperand1 = binaryOp.operand1.accept(this);
        TypeSymbol typeOperand2 = binaryOp.operand2.accept(this);
        if (binaryOp.op.equals("=")) {
            if (typeOperand1 != null && typeOperand2 != null) {
                if (typeOperand1.equals(TypeSymbol.INT) || typeOperand1.equals(TypeSymbol.STRING) || typeOperand1.equals(TypeSymbol.BOOL) ||
                    typeOperand2.equals(TypeSymbol.INT) || typeOperand2.equals(TypeSymbol.STRING) || typeOperand2.equals(TypeSymbol.BOOL)) {
                    if (typeOperand1 == typeOperand2) {
                        return retType;
                    }
                    else {
                        String err = "Cannot compare "+ typeOperand1.getName() +" with "+ typeOperand2.getName();
                        SymbolTable.error(binaryOp.ctx, binaryOp.opToken, err);
                        return null;
                    }
                }
            }
            return retType;
        }
        boolean flag = true;
        if(typeOperand1 != null && typeOperand1 != TypeSymbol.INT) {
            String err = "Operand of " + binaryOp.op +" has type "+ typeOperand1.getName() + " instead of Int";
            SymbolTable.error(binaryOp.ctx, binaryOp.leftToken, err);
            flag = false;
        }
        if(typeOperand2 != null && typeOperand2 != TypeSymbol.INT) {
            String err = "Operand of " + binaryOp.op +" has type "+ typeOperand2.getName() + " instead of Int";
            SymbolTable.error(binaryOp.ctx, binaryOp.rightToken, err);
            flag = false;
        }
        if (flag) {
            return retType;
        }
        return null;
    }

    @Override
    public TypeSymbol visit(IntNeg intNeg) {
        TypeSymbol typeOperand = intNeg.operand.accept(this);
        if (typeOperand != null && typeOperand != TypeSymbol.INT) {
            String err = "Operand of " + intNeg.op +" has type "+ typeOperand.getName() + " instead of Int";
            SymbolTable.error(intNeg.ctx, ((CoolParser.ComplContext)intNeg.ctx).e.start, err);
            return null;
        }
        return TypeSymbol.INT;
    }

    @Override
    public TypeSymbol visit(BoolNeg boolNeg) {
        TypeSymbol typeOperand = boolNeg.operand.accept(this);
        if (typeOperand != null && typeOperand != TypeSymbol.BOOL) {
            String err = "Operand of " + boolNeg.op +" has type "+ typeOperand.getName() + " instead of Bool";
            SymbolTable.error(boolNeg.ctx, ((CoolParser.NotContext)boolNeg.ctx).e.start, err);
            return null;
        }
        return TypeSymbol.BOOL;
    }

    @Override
    public TypeSymbol visit(NewOp newOp) {
        TypeSymbol type = (TypeSymbol) SymbolTable.globals.lookup(newOp.type);
        if (type == null) {
            String err = "new is used with undefined type "+newOp.type;
            SymbolTable.error(newOp.ctx, ((CoolParser.NewContext) newOp.ctx).type, err);
            return null;
        }
        return type;
    }

    @Override
    public TypeSymbol visit(isVoid isVoid) {
        return TypeSymbol.BOOL;
    }

    @Override
    public TypeSymbol visit(ExplDispatch explDispatch) {
        TypeSymbol objType = explDispatch.object.accept(this);
        if (objType == null) {
            return null;
        }
        if (objType == TypeSymbol.SELFTYPE)
            objType = currentClass;
        MethodSymbol methodSymbol = null;
        if (explDispatch.static_method != null) {
            if (explDispatch.static_method.equals("SELF_TYPE")) {
                String err = "Type of static dispatch cannot be SELF_TYPE";
                SymbolTable.error(explDispatch.ctx, ((CoolParser.ExpldispatchContext)explDispatch.ctx).static_method, err);
                return null;
            }
            TypeSymbol staticType = (TypeSymbol) SymbolTable.globals.lookup(explDispatch.static_method);
            if(staticType == null){
                String err = "Type " + explDispatch.static_method + " of static dispatch is undefined";
                SymbolTable.error(explDispatch.ctx, ((CoolParser.ExpldispatchContext)explDispatch.ctx).static_method, err);
                return null;
            }
            methodSymbol = (MethodSymbol) staticType.methodsScope.lookup(explDispatch.method);
            if (methodSymbol == null) {
                String err = "Undefined method " + explDispatch.method + " in class " + explDispatch.static_method;
                SymbolTable.error(explDispatch.ctx, ((CoolParser.ExpldispatchContext) explDispatch.ctx).name, err);
                return null;
            }
            if (!isSubClass(staticType, objType)) {
                String err = "Type " + explDispatch.static_method + " of static dispatch is not a superclass of type " + objType.getName();
                SymbolTable.error(explDispatch.ctx, ((CoolParser.ExpldispatchContext) explDispatch.ctx).static_method, err);
                return null;
            }
        }
        else {
            methodSymbol = SymbolTable.getMethod(objType, explDispatch.method);
        }

        if (methodSymbol == null) {
            String err = "Undefined method " + explDispatch.method + " in class " + objType.getName();
            SymbolTable.error(explDispatch.ctx, ((CoolParser.ExpldispatchContext) explDispatch.ctx).name, err);
            return null;
        }
        if (explDispatch.params.size() != methodSymbol.getFormals().size()) {
            String err = "Method "+explDispatch.method+" of class "+objType.getName()+" is applied to wrong number of arguments";
            SymbolTable.error(explDispatch.ctx, ((CoolParser.ExpldispatchContext) explDispatch.ctx).name, err);
            return null;
        }
        var formalIter = methodSymbol.getFormals().values().iterator();
        var argsIter = explDispatch.params.iterator();
        var tokenIter = explDispatch.paramtokens.iterator();
        while (formalIter.hasNext()) {
            TypeSymbol passedType = argsIter.next().accept(this);
            IdSymbol formal = (IdSymbol) formalIter.next();
            TypeSymbol formalType = null;
            Token paramToken = tokenIter.next();
            try {
                formalType = (TypeSymbol) SymbolTable.globals.lookup(formal.type);
            }catch(Exception e) {
                e.printStackTrace();
            }
            if (!isSubClass(formalType, passedType)) {
                String err = "In call to method " + explDispatch.method + " of class " + objType.getName() +
                        ", actual type " + passedType.getName() + " of formal parameter " +
                        formal.getName() + " is incompatible with declared type " + formalType.getName();
                SymbolTable.error(explDispatch.ctx, paramToken, err);
            }
        }
        TypeSymbol methodReturn = (TypeSymbol) SymbolTable.globals.lookup(methodSymbol.type);
        if (methodReturn == null) {
            return null;
        }
        if (methodReturn == TypeSymbol.SELFTYPE)
            return objType;
        return methodReturn;

    }

    @Override
    public TypeSymbol visit(ImplDispatch implDispatch) {
        MethodSymbol methodSymbol = SymbolTable.getMethod(currentClass, implDispatch.method);
        if (methodSymbol == null) {
            String err = "Undefined method " + implDispatch.method + " in class " + currentClass.getName();
            SymbolTable.error(implDispatch.ctx, ((CoolParser.ImpldispatchContext) implDispatch.ctx).name, err);
            return null;
        }
        if (implDispatch.params.size() != methodSymbol.getFormals().size()) {
            String err = "Method "+implDispatch.method+" of class "+currentClass.getName()+" is applied to wrong number of arguments";
            SymbolTable.error(implDispatch.ctx, ((CoolParser.ImpldispatchContext) implDispatch.ctx).name, err);
            return null;
        }
        var formalIter = methodSymbol.getFormals().values().iterator();
        var argsIter = implDispatch.params.iterator();
        var tokenIter = implDispatch.paramtokens.iterator();
        while (formalIter.hasNext()) {
            TypeSymbol passedType = argsIter.next().accept(this);
            IdSymbol formal = (IdSymbol) formalIter.next();
            TypeSymbol formalType = (TypeSymbol) SymbolTable.globals.lookup(formal.type);
            Token paramToken = tokenIter.next();
            if (!isSubClass(formalType, passedType)) {
                String err = "In call to method " + implDispatch.method + " of class " + currentClass.getName() +
                        ", actual type " + passedType.getName() + " of formal parameter " +
                        formal.getName() + " is incompatible with declared type " + formalType.getName();
                SymbolTable.error(implDispatch.ctx, paramToken, err);
            }
        }
        TypeSymbol methodReturn = (TypeSymbol) SymbolTable.globals.lookup(methodSymbol.type);
        if (methodReturn == null) {
            return null;
        }
        if (methodReturn == TypeSymbol.SELFTYPE) {
            return currentClass;
        }
        return methodReturn;
    }

    @Override
    public TypeSymbol visit(Decision decision) {
        TypeSymbol condType = decision.cond.accept(this);
        if (condType!= null && condType != TypeSymbol.BOOL) {
            String err = "If condition has type "+condType.getName()+" instead of Bool";
            SymbolTable.error(decision.ctx, ((CoolParser.IfContext) decision.ctx).cond.start, err);
        }

        TypeSymbol thenType = decision.thenExpr.accept(this);
        TypeSymbol elseType = decision.elseExpr.accept(this);
        if (thenType != null && elseType != null) {
            TypeSymbol commonType = mostSpecificCommonAncestor(thenType,elseType);
            if (commonType == null) {
                return null;
            }
            return commonType;
        }

        if (thenType != null) {
            return thenType;
        }
        return elseType;
    }

    @Override
    public TypeSymbol visit(Loop loop) {
        TypeSymbol condType = loop.cond.accept(this);
        if (condType!= null && condType != TypeSymbol.BOOL) {
            String err = "While condition has type "+condType.getName()+" instead of Bool";
            SymbolTable.error(loop.ctx, ((CoolParser.WhileContext) loop.ctx).cond.start, err);
        }
        loop.body.accept(this);
        return TypeSymbol.OBJECT;
    }

    @Override
    public TypeSymbol visit(Let let) {
        var auxScope = currentScope;
        let.scope = new DefaultScope(currentScope);
        currentScope = let.scope;

        int i = 0;
        for (var variable : let.locals) {
            if (variable.name.equals("self")) {
                String err = "Let variable has illegal name self";
                SymbolTable.error(let.ctx, ((CoolParser.LetContext)let.ctx).var_decl().get(i).name,err);
            }
            TypeSymbol varType = (TypeSymbol) SymbolTable.globals.lookup(variable.type);
            if (varType == null) {
                String err = "Let variable " + variable.name +" has undefined type " + variable.type;
                SymbolTable.error(let.ctx, ((CoolParser.LetContext)let.ctx).var_decl().get(i).type,err);
            }
            else if (variable.init != null) {
                TypeSymbol initType = variable.init.accept(this);

                if (initType != null && !isSubClass(varType, initType)) {
                    String err = "Type "+initType.getName()+" of initialization expression of identifier "+variable.name+" is " +
                            "incompatible with declared type " + variable.type;
                    SymbolTable.error(let.ctx, variable.init.ctx.start,err);
                }
            }
            IdSymbol id = new IdSymbol(variable.name);
            id.type = variable.type;
            currentScope.add(id);
            i++;
        }
        TypeSymbol retType = let.body.accept(this);
        currentScope = auxScope;
        return retType;
    }

    @Override
    public TypeSymbol visit(Program program) {
        for(var cl : program.classes) {
            cl.accept(this);
        }
        return null;
    }

    @Override
    public TypeSymbol visit(Block block) {
        TypeSymbol typeSymbol = null;
        for (var expr : block.expressions) {
            typeSymbol = expr.accept(this);
        }
        return typeSymbol;
    }

    boolean isSubClass(TypeSymbol t1, TypeSymbol t2) {

        while (true) {
            if (t2 == t1) {
                return true;
            }
            if (t2.getName().equals("Object"))
                break;
            t2 = (TypeSymbol) SymbolTable.globals.lookup(t2.superClass);
        }
        return false;
    }
    TypeSymbol mostSpecificCommonAncestor(TypeSymbol t1, TypeSymbol t2) {

        while (true) {
            if (isSubClass(t1,t2)){
                return t1;
            }
            if (t1.getName().equals("Object"))
                break;
            t1 = (TypeSymbol) SymbolTable.globals.lookup(t1.superClass);
        }
        return TypeSymbol.OBJECT;
    }
    @Override
    public TypeSymbol visit(Case caseNode) {
        TypeSymbol exprEvalType = caseNode.expr.accept(this);
        ArrayList<TypeSymbol> branchReturns = new ArrayList<>();
        for (var branch : caseNode.branches) {
            branch.scope = new DefaultScope(currentScope);
            branchReturns.add(branch.accept(this));
        }

        if (branchReturns.size() == 1) {
            return branchReturns.get(0);
        }

        TypeSymbol retType = branchReturns.remove(0);
        for (var brRet : branchReturns) {
            retType = mostSpecificCommonAncestor(retType,brRet);
        }
        return  retType;
    }

    @Override
    public TypeSymbol visit(CaseBranch caseBranch) {
        var auxScope = currentScope;
        currentScope = caseBranch.scope;

        IdSymbol variable = new IdSymbol(caseBranch.name);
        variable.type = caseBranch.type;
        currentScope.add(variable);
        TypeSymbol varType = (TypeSymbol) SymbolTable.globals.lookup(variable.type);
        if (variable.getName().equals("self")) {
            String err = "Case variable has illegal name self";
            SymbolTable.error(caseBranch.ctx, ((CoolParser.Case_branchContext)caseBranch.ctx).name,err);
        }
        if (varType == null) {
            String err = "Case variable " + variable.getName() + " has undefined type " + variable.type;
            SymbolTable.error(caseBranch.ctx, ((CoolParser.Case_branchContext)caseBranch.ctx).type,err);
        }
        else if (varType == TypeSymbol.SELFTYPE) {
            String err = "Case variable " + variable.getName() + " has illegal type SELF_TYPE";
            SymbolTable.error(caseBranch.ctx, ((CoolParser.Case_branchContext)caseBranch.ctx).type,err);
        }

        TypeSymbol retType = caseBranch.body.accept(this);
        currentScope = auxScope;
        return retType;
    }

    @Override
    public TypeSymbol visit(Local local) {
        return null;
    }
};
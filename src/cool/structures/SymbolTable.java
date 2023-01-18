package cool.structures;

import java.io.File;

import org.antlr.v4.runtime.*;

import cool.compiler.Compiler;
import cool.parser.CoolParser;

public class SymbolTable {
    public static Scope globals;

    private static boolean semanticErrors;

    public static void defineBasicClasses() {
        globals = new DefaultScope(null);
        semanticErrors = false;

        // TODO Populate global scope.

        globals.add(TypeSymbol.OBJECT);
        globals.add(TypeSymbol.IO);
        globals.add(TypeSymbol.INT);
        globals.add(TypeSymbol.STRING);
        globals.add(TypeSymbol.SELFTYPE);
        globals.add(TypeSymbol.BOOL);

        var abortFun = new MethodSymbol(TypeSymbol.OBJECT, "abort", TypeSymbol.OBJECT);
        TypeSymbol.OBJECT.methodsScope.add(abortFun);
        abortFun.type = "Object";
        var typenameFun = new MethodSymbol(TypeSymbol.OBJECT, "type_name", TypeSymbol.STRING);
        TypeSymbol.OBJECT.methodsScope.add(typenameFun);
        typenameFun.type = "String";
        var copyFun = new MethodSymbol(TypeSymbol.OBJECT, "copy", TypeSymbol.SELFTYPE);
        copyFun.type = "SELF_TYPE";
        TypeSymbol.OBJECT.methodsScope.add(copyFun);

        var outstringFun = new MethodSymbol(TypeSymbol.IO, "out_string", TypeSymbol.SELFTYPE);
        TypeSymbol.IO.methodsScope.add(outstringFun);
        outstringFun.type = "IO";
        IdSymbol outstringFormal = new IdSymbol("x");
        outstringFormal.type = "String";
        outstringFun.formals.put(outstringFormal.name, outstringFormal);
        var outintFun = new MethodSymbol(TypeSymbol.IO, "out_int", TypeSymbol.SELFTYPE);
        TypeSymbol.IO.methodsScope.add(outintFun);
        outintFun.type = "IO";
        IdSymbol outintFormal = new IdSymbol("x");
        outintFormal.type = "Int";
        outintFun.formals.put(outintFormal.name, outintFormal);
        var inintFun = new MethodSymbol(TypeSymbol.IO, "in_int", TypeSymbol.INT);
        TypeSymbol.IO.methodsScope.add(inintFun);
        inintFun.type = "Int";
        var instringFun = new MethodSymbol(TypeSymbol.IO, "in_string", TypeSymbol.STRING);
        instringFun.type = "String";
        TypeSymbol.IO.methodsScope.add(instringFun);

        var lengthFun = new MethodSymbol(TypeSymbol.STRING, "length", TypeSymbol.INT);
        TypeSymbol.STRING.methodsScope.add(lengthFun);
        lengthFun.type = "Int";
        var concat = new MethodSymbol(TypeSymbol.STRING, "concat", TypeSymbol.STRING);
        IdSymbol concatFormal = new IdSymbol("s");
        concatFormal.type = "String";
        concat.formals.put("s",concatFormal);
        concat.type = "String";
        TypeSymbol.STRING.methodsScope.add(concat);
        var substr = new MethodSymbol(TypeSymbol.STRING, "substr", TypeSymbol.STRING);
        IdSymbol substrFormal_1 = new IdSymbol("i");
        substrFormal_1.type = "Int";
        IdSymbol substrFormal_2 = new IdSymbol("l");
        substrFormal_2.type = "Int";
        substr.formals.put("i",substrFormal_1);
        substr.formals.put("l",substrFormal_2);
        substr.type = "String";
        TypeSymbol.STRING.methodsScope.add(substr);

    }

    /**
     * Displays a semantic error message.
     *
     * @param ctx  Used to determine the enclosing class context of this error,
     *             which knows the file name in which the class was defined.
     * @param info Used for line and column information.
     * @param str  The error message.
     */
    public static void error(ParserRuleContext ctx, Token info, String str) {
        while (! (ctx.getParent() instanceof CoolParser.ProgramContext))
            ctx = ctx.getParent();
        
        String message = "\"" + new File(Compiler.fileNames.get(ctx)).getName()
                + "\", line " + info.getLine()
                + ":" + (info.getCharPositionInLine() + 1)
                + ", Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }

    public static MethodSymbol getMethod(TypeSymbol currentClass, String methodName) {
        MethodSymbol methodSymbol;
        while(true) {
            methodSymbol = (MethodSymbol) currentClass.methodsScope.lookup(methodName);
            if (methodSymbol != null) {
                break;
            }
            if(currentClass.superClass == null)
                break;
            currentClass = (TypeSymbol) globals.lookup(currentClass.superClass);
        }
        return methodSymbol;
    }

    public static MethodSymbol getSpecificMethod(TypeSymbol currentClass, String className, String methodName) {
        MethodSymbol methodSymbol = null;
        while(true) {
            if(currentClass.name.equals(className))
                methodSymbol = (MethodSymbol) currentClass.lookup(methodName);
            if (methodSymbol != null) {
                break;
            }
            if(currentClass.superClass == null)
                break;
            currentClass = (TypeSymbol) globals.lookup(currentClass.superClass);
        }
        return methodSymbol;
    }
    
    public static void error(String str) {
        String message = "Semantic error: " + str;
        
        System.err.println(message);
        
        semanticErrors = true;
    }
    
    public static boolean hasSemanticErrors() {
        return semanticErrors;
    }
}

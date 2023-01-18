package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

public class TypeSymbol extends Symbol implements Scope{

    public Scope attributesScope = new DefaultScope(null);
    public Scope methodsScope = new DefaultScope(null);
    public String superClass;

    public TypeSymbol(String name) {
        super(name);
        if (!name.equals("Object"))
            superClass = "Object";
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }
    
    // Symboluri aferente tipurilor, definite global
    public static final TypeSymbol INT   = new TypeSymbol("Int");
    public  static final TypeSymbol STRING = new TypeSymbol("String");
    public static final TypeSymbol BOOL  = new TypeSymbol("Bool");
    public  static final TypeSymbol OBJECT = new TypeSymbol("Object");
    public  static final TypeSymbol IO = new TypeSymbol("IO");

    public static final TypeSymbol  SELFTYPE = new TypeSymbol("SELF_TYPE");

    private final Scope parent = null;

    @Override
    public boolean add(Symbol sym) {
        return attributesScope.add(sym);
    }

    @Override
    public Symbol lookup(String name) {
        return attributesScope.lookup(name);
    }

    @Override
    public Scope getParent() {
        return attributesScope.getParent();
    }

    @Override
    public Scope setParent(Scope parent) {
        attributesScope.setParent(parent);
        return parent;
    }

    @Override
    public String toString() {
        return attributesScope.toString();
    }
}

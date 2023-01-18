package cool.structures;

import java.util.LinkedHashMap;
import java.util.Map;

// O functie este atât simbol, cât și domeniu de vizibilitate pentru parametrii
// săi formali.

// TODO 1: Implementați clasa FunctionSymbol, suprascriind metodele din interfață
// și adăugându-i un nume.
public class MethodSymbol extends IdSymbol implements Scope {
 
    // LinkedHashMap reține ordinea adăugării.
    protected Map<String, Symbol> formals = new LinkedHashMap<>();
    
    protected TypeSymbol classParent;
    Scope parent;
    
    public MethodSymbol(Scope parent, String name, TypeSymbol returnType) {
        super(name);
        this.parent = parent;
    }
    public MethodSymbol(Scope parent, String name) {
        super(name);
        this.parent = parent;
    }
    public Map<String, Symbol> getFormals() {
        return formals;
    }

    @Override
    public boolean add(Symbol sym) {
        // Reject duplicates in the same scope.
        if (formals.containsKey(sym.getName()))
            return false;

        formals.put(sym.getName(), sym);

        return true;
    }

    @Override
    public Symbol lookup(String name) {
        var sym = formals.get(name);

        if (sym != null)
            return sym;

        if (parent != null)
            return parent.lookup(name);

        return null;
    }

    public Symbol lookupFormal(String name) {
        return formals.get(name);
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    @Override
    public Scope setParent(Scope parent) {
        this.parent = parent;
        return parent;
    }

    @Override
    public String toString() {
        return formals.values().toString();
    }
}
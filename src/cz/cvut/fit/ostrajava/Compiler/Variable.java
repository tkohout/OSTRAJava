package cz.cvut.fit.ostrajava.Compiler;

/**
 * Created by tomaskohout on 11/15/15.
 */
public class Variable {
    protected String name;
    protected Type type;

    public Variable(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }
}

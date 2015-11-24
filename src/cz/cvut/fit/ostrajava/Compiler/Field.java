package cz.cvut.fit.ostrajava.Compiler;

import cz.cvut.fit.ostrajava.Type.Type;

import java.util.List;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class Field {
    protected List<String> flags;
    protected String name;
    protected Type type;

    public Field(String name, Type type){
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

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }
}

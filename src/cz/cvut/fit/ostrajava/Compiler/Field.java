package cz.cvut.fit.ostrajava.Compiler;

import java.util.List;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class Field {
    protected List<String> flags;
    protected String name;
    protected String type;

    public Field(String name, String type){
        this.name = name;
        this.type = type;
    }

    public List<String> getFlags() {
        return flags;
    }

    public void setFlags(List<String> flags) {
        this.flags = flags;
    }
}

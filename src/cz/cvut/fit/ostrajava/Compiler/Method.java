package cz.cvut.fit.ostrajava.Compiler;

import java.util.List;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class Method {
    protected String name;
    protected List<String> args;
    protected String returnType;
    protected ByteCode byteCode;

    public Method(String name, List<String> args, String returnType) {
        this.name = name;
        this.args = args;
        this.returnType = returnType;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ByteCode getByteCode() {
        return byteCode;
    }

    public void setByteCode(ByteCode byteCode) {
        this.byteCode = byteCode;
    }

}

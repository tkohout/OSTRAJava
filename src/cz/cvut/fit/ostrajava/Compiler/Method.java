package cz.cvut.fit.ostrajava.Compiler;

import cz.cvut.fit.ostrajava.Type.Type;
import cz.cvut.fit.ostrajava.Type.Types;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class Method {
    protected String name;
    protected List<Type> args;
    protected Type returnType;
    protected ByteCode byteCode;
    protected int localVariablesCount =  0;

    public Method() {
        args = new ArrayList<>();
    }

    public Method(String name, List<Type> args, Type returnType) {
        this.name = name;
        this.args = args;
        this.returnType = returnType;
    }

    public Method(String descriptor) {
        String[] parts = descriptor.split(":");
        args = new ArrayList<>();
        //TODO: Put real return type
        returnType = Types.Void();

        for (int i=0; i < parts.length; i++){
             if (i == 0){
                 name = parts[i];
             }else{
                 args.add(Types.fromString(parts[i]));
             }
        }
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setReturnType(Type returnType) {
        this.returnType = returnType;
    }

    public List<Type> getArgs() {
        return args;
    }

    public void setArgs(List<Type> args) {
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

    public int getLocalVariablesCount() {
        return localVariablesCount;
    }

    public void setLocalVariablesCount(int localVariablesCount) {
        this.localVariablesCount = localVariablesCount;
    }

    public String getDescriptor(){
        StringBuilder sb = new StringBuilder();
        sb.append(name);

        for (Type arg: args){
            sb.append(":" + arg.toString().toLowerCase());
        }

        return sb.toString();
    }
}

package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;

import java.util.List;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class InterpretedClass extends Class {

    InterpretedClass superClass;
    int classPoolAddress;

    public InterpretedClass(Class c) {
        super(c.getClassName(), c.getSuperName());

        //Copy
        for (Field field: c.getFields()){
            this.addFields(field);
        }

        for (Method method: c.getMethods()){
            this.addMethod(new InterpretedMethod(method));
        }

        //TODO: Copy flags
    }

    public InterpretedClass getSuperClass() {
        return superClass;
    }

    public void setSuperClass(InterpretedClass superClass) {
        this.superClass = superClass;
    }

    public int getClassPoolAddress() {
        return classPoolAddress;
    }

    public void setClassPoolAddress(int classPoolAddress) {
        this.classPoolAddress = classPoolAddress;
    }

    //TODO: add looking up by descriptor
    public InterpretedMethod lookupMethod(String name) throws LookupException {
        String lowercase = name.toLowerCase();

        for (Method method: methods){
            if (method.getName().toLowerCase().equals(lowercase)){
                return (InterpretedMethod)method;
            }
        }

        throw new LookupException("Method '" + name + "' not found");
    }


}

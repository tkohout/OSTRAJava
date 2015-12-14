package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;

import java.util.*;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class InterpretedClass extends Class {


    int classPoolAddress;




    public InterpretedClass(Class c) {
        super(c.getClassName(), c.getSuperName());

        //Copy
        for (Field field: c.getFields()){
            this.addField(field);
        }

        for (Method method: c.getMethods()){
            this.addMethod(new InterpretedMethod(method));
        }

        setConstantPool(c.getConstantPool());

        //TODO: Copy flags
    }



    public int getClassPoolAddress() {
        return classPoolAddress;
    }

    public void setClassPoolAddress(int classPoolAddress) {
        this.classPoolAddress = classPoolAddress;
    }



    public InterpretedMethod lookupMethod(String descriptor, ClassPool classPool) throws LookupException {
        return (InterpretedMethod)super.lookupMethod(descriptor, classPool);
    }



}

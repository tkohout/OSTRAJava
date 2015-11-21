package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;

import java.util.*;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class InterpretedClass extends Class {

    InterpretedClass superClass;
    int classPoolAddress;
    //Including super fields
    Set<Field> allFields;


    public InterpretedClass(Class c) {
        super(c.getClassName(), c.getSuperName());

        //Copy
        for (Field field: c.getFields()){
            this.addFields(field);
        }

        for (Method method: c.getMethods()){
            this.addMethod(new InterpretedMethod(method));
        }

        setConstantPool(c.getConstantPool());

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


    public Set<Field> getAllFields(){
        if (allFields == null) {
            allFields = new LinkedHashSet<>();
            if (superClass != null) {
                allFields.addAll(getSuperClass().getAllFields());
            }
            allFields.addAll(getFields());
        }

        return allFields;
    }

    //TODO: add looking up by descriptor
    public InterpretedMethod lookupMethod(String name) throws LookupException {
        for (Method method: methods){
            if (method.getName().equals(name)){
                return (InterpretedMethod)method;
            }
        }

        if (superClass != null) {
            return superClass.lookupMethod(name);
        }

        throw new LookupException("Method '" + name + "' not found");
    }

    public int lookupField(String name) throws LookupException {
        int i = 0;

        for (Field field: getAllFields()){
            if (field.getName().equals(name)){
                return i;
            }
            i++;
        }

        throw new LookupException("Field '" + name + "' not found");
    }


}

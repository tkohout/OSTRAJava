package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.Class;
import cz.cvut.fit.ostrajava.Compiler.Field;

import java.util.List;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class Heap {
    protected  Object[] objectArray;
    int last = 0;
    int size;

    public Heap(int size){
        objectArray = new Object[size];
        this.size = size;
    }

    public int alloc(InterpretedClass objectClass){
        last++;

        int address = last-1;
        objectArray[address] = new Object(objectClass);

        return address;
    }

    public Object load(int reference) throws InterpreterException {
        if (reference > size-1 || reference > last-1){
            throw new InterpreterException("Can't load object on " + reference);
        }

        return objectArray[reference];
    }

    public void dealloc(int address){

    }

}

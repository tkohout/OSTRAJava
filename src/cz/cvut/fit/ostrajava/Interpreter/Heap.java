package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.Class;
import cz.cvut.fit.ostrajava.Compiler.CompilerException;
import cz.cvut.fit.ostrajava.Compiler.Field;

import java.util.List;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class Heap {
    protected  HeapObject[] objectArray;
    int last = 0;
    int size;

    public Heap(int size){
        objectArray = new HeapObject[size];
        this.size = size;
    }

    public int allocObject(InterpretedClass objectClass) throws InterpreterException {
        Object object = new Object(objectClass);
        return alloc(object);
    }

    public int allocArray(int size) throws InterpreterException {
        Array array = new Array(size);
        return alloc(array);
    }

    private int alloc(HeapObject object) throws InterpreterException {

        last++;
        if (last >= size){
            //TODO: Do some garbage collecting
            throw new InterpreterException("Heap overflow");
        }

        int index = last-1;
        objectArray[index] = object;

        return indexToReference(index);
    }

    private int referenceToIndex(int reference){
        return reference - 1;
    }
    //0 reference is null
    private int indexToReference(int index){
        return index + 1;
    }

    public HeapObject load(int reference) throws InterpreterException {
        if (reference == 0){
            throw new NullPointerException();
        }

        int index = referenceToIndex(reference);

        if (index > size-1 || index > last-1){
            throw new InterpreterException("Can't load object on address: " + reference);
        }

        return objectArray[index];
    }

    public void dealloc(int address){

    }

}

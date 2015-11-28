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

    public StackValue allocObject(InterpretedClass objectClass) throws InterpreterException {
        Object object = new Object(objectClass);
        return alloc(object);
    }

    public StackValue allocArray(int size) throws InterpreterException {
        Array array = new Array(size);
        return alloc(array);
    }

    private StackValue alloc(HeapObject object) throws InterpreterException {

        last++;
        if (last >= size){
            //TODO: Do some garbage collecting
            throw new InterpreterException("Heap overflow");
        }

        int index = last-1;
        objectArray[index] = object;

        return indexToReference(index);
    }

    private int referenceToIndex(StackValue reference){
        return reference.intValue() - 1;
    }

    //0 reference is null
    //Returns pointer
    private StackValue indexToReference(int index){
        return new StackValue(index + 1, StackValue.Type.Pointer);
    }


    public Array loadArray(StackValue reference) throws InterpreterException{
        HeapObject obj = load(reference);
        if (obj instanceof Array) {
            return (Array)obj;
        }else{
            throw new InterpreterException("Type mismatch on " + reference + ", expected ArrayType");
        }
    }

    public Object loadObject(StackValue reference) throws InterpreterException{
        HeapObject obj = load(reference);
        if (obj instanceof Object) {
            return (Object)obj;
        }else{
            throw new InterpreterException("Type mismatch on " + reference + ", expected Object");
        }
    }

    public HeapObject load(StackValue reference) throws InterpreterException {
        if (reference.isNullPointer()){
            throw new NullPointerException();
        }

        int index = referenceToIndex(reference);

        if (index > size-1 || index > last-1){
            throw new InterpreterException("Can't load object on address: " + reference);
        }

        return objectArray[index];
    }

    public StackValue getLastReference(){
        return indexToReference(last);
    }

    public void dealloc(int address){

    }

}

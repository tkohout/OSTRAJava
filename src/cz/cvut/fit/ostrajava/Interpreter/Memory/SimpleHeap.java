package cz.cvut.fit.ostrajava.Interpreter.Memory;

import cz.cvut.fit.ostrajava.Interpreter.InterpretedClass;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.GarbageCollector;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

import java.util.LinkedList;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class SimpleHeap implements Heap{
    protected  HeapItem[] objectArray;
    protected  LinkedList<Integer> emptyList;

    protected GarbageCollector garbageCollector;
    int size;
    int addressStart;

    public SimpleHeap(int size, int addressStart){
        objectArray = new HeapItem[size];
        emptyList = new LinkedList<>();
        this.addressStart = addressStart;

        this.size = size;

        for (int i=size-1; i >=0; i--){
            emptyList.add(i);
        }
    }

    public SimpleHeap(int size){
        //We start address at, 0 is reserved for null
        this(size, 1);
    }

    public GarbageCollector getGarbageCollector() {
        return garbageCollector;
    }

    public void setGarbageCollector(GarbageCollector garbageCollector) {
        this.garbageCollector = garbageCollector;
    }

    public StackValue allocObject(InterpretedClass objectClass) throws HeapOverflow {
        cz.cvut.fit.ostrajava.Interpreter.Memory.Object object = new Object(objectClass);
        return alloc(object);
    }

    public StackValue allocArray(int size) throws HeapOverflow {
        Array array = new Array(size);
        return alloc(array);
    }

    public StackValue alloc(HeapItem object) throws HeapOverflow {
        if (isOutOfSpace()) {
            throw new HeapOverflow();
        }

        int index = emptyList.getLast();
        emptyList.removeLast();

        objectArray[index] = object;

        return indexToReference(index);
    }

    public boolean isOutOfSpace(){
        return (spaceLeft() == 0);
    }

    public int spaceLeft(){
        return emptyList.size();
    }

    public int spaceUsed(){
        return size - spaceLeft();
    }

    public int referenceToIndex(StackValue reference){
        return reference.intValue() - addressStart;
    }

    //0 reference is null
    //Returns pointer
    public StackValue indexToReference(int index){
        return new StackValue(index + addressStart, StackValue.Type.Pointer);
    }

    public boolean referenceIsOutOfBounds(StackValue reference){
        int i = referenceToIndex(reference);
        return (i < 0 || i >= getSize());
    }


    public Array loadArray(StackValue reference){
        HeapItem obj = load(reference);

        if (obj == null){
            throw new NullPointerException();
        }

        if (obj instanceof Array) {
            return (Array)obj;
        }else{
            throw new IllegalArgumentException("Type mismatch on " + reference + ", expected ArrayType");
        }
    }

    public Object loadObject(StackValue reference){
        HeapItem obj = load(reference);

        if (obj == null){
            throw new NullPointerException();
        }

        if (obj instanceof Object) {
            return (Object)obj;
        }else{
            throw new IllegalArgumentException("Type mismatch on " + reference + ", expected Object");
        }
    }

    public HeapItem load(StackValue reference) {
        if (reference.isNullPointer()){
            throw new NullPointerException();
        }

        int index = referenceToIndex(reference);

        if (index > size-1 || index < 0){
            throw new IndexOutOfBoundsException("Can't load object on address: " + reference);
        }

        return objectArray[index];
    }

    public int getSize(){
        return size;
    }

    public void dealloc(StackValue address){
        int index = referenceToIndex(address);
        objectArray[index] = null;
        emptyList.addFirst(index);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < getSize(); i++) {
            StackValue ref = indexToReference(i);
            if (load(ref) != null){
                sb.append(ref + " ");
            }
        }

        return sb.toString();
    }
}

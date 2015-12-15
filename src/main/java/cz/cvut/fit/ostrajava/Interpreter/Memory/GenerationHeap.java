package cz.cvut.fit.ostrajava.Interpreter.Memory;

import cz.cvut.fit.ostrajava.Interpreter.InterpretedClass;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.GenerationCollector;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.MarkAndSweepCollector;
import cz.cvut.fit.ostrajava.Interpreter.Stack;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/28/15.
 */
public class GenerationHeap implements Heap {

    protected SimpleHeap eden, tenure;

    protected int edenSize, tenureSize;
    protected GenerationCollector garbageCollector;

    public GenerationHeap(int edenSize, int tenureSize, Stack stack){

        this.edenSize = edenSize;
        this.tenureSize = tenureSize;

        eden = new SimpleHeap(edenSize);
        eden.setGarbageCollector(new MarkAndSweepCollector(eden));

        //Address start at end of eden
        tenure = new SimpleHeap(tenureSize, edenSize+1);
        tenure.setGarbageCollector(new MarkAndSweepCollector(tenure));
    }


    public SimpleHeap getEden() {
        return eden;
    }

    public SimpleHeap getTenure() {
        return tenure;
    }

    public int getEdenSize() {
        return edenSize;
    }

    public int getTenureSize() {
        return tenureSize;
    }

    public StackValue allocObject(InterpretedClass objectClass) throws HeapOverflow {
        Object object = new Object(objectClass);
        return alloc(object);
    }

    public StackValue allocArray(int size) throws HeapOverflow {
        Array array = new Array(size);
        return alloc(array);
    }

    public StackValue alloc(HeapItem obj) throws HeapOverflow{
        try {
            return eden.alloc(obj);
        } catch (HeapOverflow heapOverflow) {
            garbageCollector.run(null);

            //Try again
            StackValue ref =  eden.alloc(obj);

            return ref;
        }
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

    public boolean isEdenReference(StackValue reference){
        return !eden.referenceIsOutOfBounds(reference);
    }

    public HeapItem load(StackValue reference){

        if (isEdenReference(reference)){
            return eden.load(reference);
        }else{
            return tenure.load(reference);
        }
    }


    public int getSize() {
        return edenSize + tenureSize;
    }


    public void dealloc(StackValue reference) {
        if (isEdenReference(reference)){
            eden.dealloc(reference);
        }else{
            tenure.dealloc(reference);
        }
    }

    public void addDirtyLink(StackValue from, StackValue reference){
        garbageCollector.addDirtyLink(from, reference);
    }


    public GenerationCollector getGarbageCollector() {
        return garbageCollector;
    }


    public void setGarbageCollector(GenerationCollector garbageCollector) { this.garbageCollector = garbageCollector; }


    @Override
    public StackValue[] getAllocated() {
        StackValue[] edenAllocated = eden.getAllocated();
        StackValue[] tenureAllocated = tenure.getAllocated();

        int edenLen = edenAllocated.length;
        int tenureLen = tenureAllocated.length;
        StackValue[] all= new StackValue[edenLen+tenureLen];
        System.arraycopy(edenAllocated, 0, all, 0, edenLen);
        System.arraycopy(tenureAllocated, 0, all, edenLen, tenureLen);
        return all;

    }

    @Override
    public String toString() {
        return "Eden: \n " + eden.toString() + " \n Tenure: \n " + tenure.toString();
    }
}

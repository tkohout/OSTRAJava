package cz.cvut.fit.ostrajava.Interpreter.Memory;

import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.GarbageCollector;
import cz.cvut.fit.ostrajava.Interpreter.InterpretedClass;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.MarkAndSweepCollector;
import cz.cvut.fit.ostrajava.Interpreter.Stack;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/28/15.
 */
public class GenerationHeap implements Heap {

    protected SimpleHeap eden, tenure;

    protected int edenSize, tenureSize;
    protected GarbageCollector garbageCollector;

    public GenerationHeap(int edenSize, int tenureSize, Stack stack){

        this.edenSize = edenSize;
        this.tenureSize = tenureSize;

        eden = new SimpleHeap(edenSize);
        eden.setGarbageCollector(new MarkAndSweepCollector(stack, eden));

        tenure = new SimpleHeap(tenureSize);
        tenure.setGarbageCollector(new MarkAndSweepCollector(stack, tenure));
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

        try {
            return eden.allocObject(objectClass);
        } catch (HeapOverflow heapOverflow) {
            garbageCollector.run();
            //Try again
            return allocObject(objectClass);
        }

    }

    public StackValue allocArray(int size) throws HeapOverflow {
        try {
            return eden.allocArray(size);
        } catch (HeapOverflow heapOverflow) {
            garbageCollector.run();
            //Try again
            return allocArray(size);
        }
    }


    public PrimitiveArray loadArray(StackValue reference){
        HeapItem obj = load(reference);

        if (obj == null){
            throw new NullPointerException();
        }

        if (obj instanceof PrimitiveArray) {
            return (PrimitiveArray)obj;
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

    public StackValue referenceToTenure(StackValue reference){
        return new StackValue(reference.intValue() - edenSize, StackValue.Type.Pointer);
    }

    public boolean isEdenReference(StackValue reference){
        return (reference.intValue() < edenSize);
    }

    public HeapItem load(StackValue reference){

        if (isEdenReference(reference)){
            return eden.load(reference);
        }else{
            StackValue tenureRef = referenceToTenure(reference);
            return tenure.load(tenureRef);
        }
    }


    public int getSize() {
        return edenSize + tenureSize;
    }


    public void dealloc(StackValue reference) {
        if (isEdenReference(reference)){
            eden.dealloc(reference);
        }else{
            StackValue tenureRef = referenceToTenure(reference);
            tenure.dealloc(tenureRef);
        }
    }


    public GarbageCollector getGarbageCollector() {
        return garbageCollector;
    }


    public void setGarbageCollector(GarbageCollector garbageCollector) { this.garbageCollector = garbageCollector; }
}

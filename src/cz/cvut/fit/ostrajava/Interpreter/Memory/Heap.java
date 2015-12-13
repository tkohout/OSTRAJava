package cz.cvut.fit.ostrajava.Interpreter.Memory;

import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.GarbageCollector;
import cz.cvut.fit.ostrajava.Interpreter.InterpretedClass;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/28/15.
 */
public interface Heap {

    public GarbageCollector getGarbageCollector();


    public StackValue allocObject(InterpretedClass objectClass) throws HeapOverflow;
    public StackValue allocArray(int size) throws HeapOverflow;

    public Array loadArray(StackValue reference);
    public Object loadObject(StackValue reference);
    public HeapItem load(StackValue reference);

    public int getSize();

    public void dealloc(StackValue address);
    public StackValue[] getAllocated();

}

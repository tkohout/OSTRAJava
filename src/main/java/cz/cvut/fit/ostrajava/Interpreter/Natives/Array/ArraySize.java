package cz.cvut.fit.ostrajava.Interpreter.Natives.Array;

import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 12/1/15.
 */
public abstract class ArraySize extends Native {

    public ArraySize(Heap heap) {
        super(heap);
    }

    @Override
    public StackValue invoke(StackValue[] args) throws HeapOverflow, InterpreterException {
        StackValue ref = args[0];
        int size = heap.loadArray(ref).getSize();
        return new StackValue(size, StackValue.Type.Primitive);
    }
}

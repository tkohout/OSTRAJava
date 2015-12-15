package cz.cvut.fit.ostrajava.Interpreter.Natives.Array;

import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 12/1/15.
 */
public class CharArraySize extends ArraySize {
    public CharArraySize(Heap heap) {
        super(heap);
    }

    @Override
    public StackValue invoke(StackValue[] args) throws HeapOverflow, InterpreterException {
        return super.invoke(args);
    }
}

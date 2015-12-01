package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/23/15.
 */
public abstract class Native {
    protected Heap heap;

    public Native(Heap heap) {
        this.heap = heap;
    }

    public abstract StackValue invoke(StackValue args[]) throws HeapOverflow, InterpreterException;
}

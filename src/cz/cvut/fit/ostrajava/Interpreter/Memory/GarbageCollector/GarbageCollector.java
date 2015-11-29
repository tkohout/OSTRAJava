package cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector;

import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Memory.SimpleHeap;
import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Stack;

/**
 * Created by tomaskohout on 11/28/15.
 */
public abstract class GarbageCollector {
    Stack stack;
    Heap heap;

    public  GarbageCollector(Stack stack, Heap heap){
        this.stack = stack;
        this.heap = heap;
    }
    public abstract void run() throws HeapOverflow;

}

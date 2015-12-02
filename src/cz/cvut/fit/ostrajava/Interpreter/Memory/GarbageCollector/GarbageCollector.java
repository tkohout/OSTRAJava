package cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector;

import cz.cvut.fit.ostrajava.Interpreter.Frame;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapItem;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Memory.SimpleHeap;
import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Stack;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tomaskohout on 11/28/15.
 */
public abstract class GarbageCollector {
    Heap heap;

    public  GarbageCollector(Heap heap){
        this.heap = heap;
    }
    //Returns dirty links
    public abstract Set<StackValue> run(Set<StackValue> roots) throws HeapOverflow;

    public static Set<StackValue> getRootsFromStack(Stack stack) {
        Set<StackValue> roots = new HashSet<>();

        //Go through all frames and get references
        for (int i = 0; i < stack.getFramesNumber(); i++) {
            Frame frame = stack.getFrame(i);

            for (int j = 0; j < frame.getLocalVariablesCount(); j++){
                StackValue reference = frame.loadVariable(j);
                if (reference.isPointer() && !reference.isNullPointer()){
                    roots.add(reference);
                }
            }

            //There can also be references directly on stack
            for (int j = frame.getStackOffset(); j < frame.getSize(); j+=StackValue.size){
                StackValue reference = frame.get(j);
                //Translate every value from stack if it's eden reference
                if (reference.isPointer() && !reference.isNullPointer()) {
                    roots.add(reference);
                }
            }
        }

        return roots;
    }

}

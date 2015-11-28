package cz.cvut.fit.ostrajava.Interpreter.GarbageCollector;

import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Object;

/**
 * Created by tomaskohout on 11/28/15.
 */
public class MarkAndSweep {
    Stack stack;
    Heap heap;

    public MarkAndSweep(Stack stack, Heap heap) {
        this.stack = stack;
        this.heap = heap;
    }

    public void run() throws InterpreterException {
        mark();
    }

    protected void mark() throws InterpreterException {
        markAllDead(heap);

        for (int i = 0; i < stack.getFramesNumber(); i++) {
            Frame frame = stack.currentFrame();
            markFrame(frame);
        }
    }

    protected void markFrame(Frame frame) throws InterpreterException {
        for (int i = 0; i < frame.getLocalVariablesCount(); i++){
            StackValue reference = frame.loadVariable(i);

            markObject(reference);
        }
    }

    protected void markObject(StackValue reference) throws InterpreterException {
        if (reference.isPointer() && !reference.isNullPointer()){
            HeapObject heapObj = heap.load(reference);
            heapObj.setGCState(State.Live);

            if (heapObj instanceof Object){
                Object obj = (Object) heapObj;

                for (int i =0; i< obj.getFieldsNumber(); i++){
                    StackValue fieldRef = obj.getField(i);
                    markObject(fieldRef);
                }
            }
        }
    }

    public void markAllDead(Heap heap){
        for (int i=1; i<heap.getLastReference().intValue(); i++){
            try {
                HeapObject obj = heap.load(new StackValue(i, StackValue.Type.Pointer));
                obj.setGCState(State.Dead);
            } catch (InterpreterException e) {
                //Object already deleted
            }

        }
    }

}

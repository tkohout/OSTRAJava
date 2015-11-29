package cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector;

import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapItem;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Object;
import cz.cvut.fit.ostrajava.Interpreter.Memory.SimpleHeap;

/**
 * Created by tomaskohout on 11/28/15.
 */
public class MarkAndSweepCollector extends GarbageCollector{


    public MarkAndSweepCollector(Stack stack, SimpleHeap heap) {
        super(stack, heap);
    }

    public void run() {
        mark();
        sweep();
    }

    protected void mark()  {
        //markAllDead(heap);

        for (int i = 0; i < stack.getFramesNumber(); i++) {
            Frame frame = stack.currentFrame();
            markFrame(frame);
        }
    }

    protected void markFrame(Frame frame)  {
        for (int i = 0; i < frame.getLocalVariablesCount(); i++){
            StackValue reference = frame.loadVariable(i);

            markObject(reference);
        }
    }

    protected void markObject(StackValue reference)  {
        if (reference.isPointer() && !reference.isNullPointer()){
            HeapItem heapObj = heap.load(reference);
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

    public void markAllDead(SimpleHeap heap){
        for (int i=1; i<heap.getSize(); i++){

            HeapItem obj = heap.load(new StackValue(i, StackValue.Type.Pointer));

            if (obj != null) {
                obj.setGCState(State.Dead);
            }

        }
    }

    public void sweep(){
        for (int i=1; i<heap.getSize(); i++){

                StackValue reference = new StackValue(i, StackValue.Type.Pointer);

                HeapItem obj = heap.load(reference);

                if (obj != null) {
                    if (obj.getGCState() == State.Dead) {
                        heap.dealloc(reference);
                    } else {
                        //Reset all to dead state
                        obj.setGCState(State.Dead);
                    }
                }


        }
    }

}

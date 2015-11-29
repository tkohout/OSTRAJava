package cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector;

import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapItem;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Object;
import cz.cvut.fit.ostrajava.Interpreter.Memory.SimpleHeap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tomaskohout on 11/28/15.
 */
public class MarkAndSweepCollector extends GarbageCollector{
    SimpleHeap heap;

    public MarkAndSweepCollector(SimpleHeap heap) {
        super(heap);
        this.heap = heap;
    }

    public Set<StackValue> run(Set<StackValue> roots) {

        Set<StackValue> dirtyLinks = mark(roots);
        sweep();

        return dirtyLinks;
    }

    protected Set<StackValue> mark(Set<StackValue> roots)  {
        Set<StackValue> dirtyLinks = new HashSet<>();

        for (StackValue rootRef: roots) {

            //We clean only stuff from this heap
            if (!heap.referenceIsOutOfBounds(rootRef)) {
                dirtyLinks.addAll(markObject(rootRef));
            }
        }

        return dirtyLinks;
    }


    protected Set<StackValue> markObject(StackValue reference)  {
        Set<StackValue> dirtyLinks = new HashSet<>();

        if (reference.isPointer() && !reference.isNullPointer()){
            //Reference is leading to different generation, don't follow, mark dirty link
            if (heap.referenceIsOutOfBounds(reference)) {
                dirtyLinks.add(reference);
                return dirtyLinks;
            }

            HeapItem heapObj = heap.load(reference);
            heapObj.setGCState(State.Live);

            if (heapObj instanceof Object){
                Object obj = (Object) heapObj;

                for (int i =0; i< obj.getFieldsNumber(); i++){
                    StackValue fieldRef = obj.getField(i);
                    dirtyLinks.addAll(markObject(fieldRef));
                }
            }
        }

        return dirtyLinks;
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
        for (int i=0; i<heap.getSize(); i++){

                StackValue reference = heap.indexToReference(i);

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

package cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector;

import cz.cvut.fit.ostrajava.Interpreter.Memory.*;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Object;
import cz.cvut.fit.ostrajava.Interpreter.Stack;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/29/15.
 */
public class GenerationCollector extends GarbageCollector {

    GenerationHeap heap;

    public GenerationCollector(Stack stack, GenerationHeap heap) {
        super(stack, heap);
        this.heap = heap;
    }

    @Override
    public void run() throws HeapOverflow{
        heap.getEden().getGarbageCollector().run();

        //Move all survivors to tenure
        moveEdenToTenure();
    }

    protected void moveEdenToTenure() throws HeapOverflow{
        //if there is not enough space in tenure, clean it
        if (heap.getTenure().spaceLeft() <= heap.getEden().spaceUsed()){
            heap.getTenure().getGarbageCollector().run();

            if (heap.getTenure().spaceLeft() <= heap.getEden().spaceUsed()){
                throw new HeapOverflow();
            }
        }

        int[] referenceMap = new int[heap.getEdenSize()];

        for (int i = 1; i < heap.getEdenSize(); i++) {
            StackValue oldReference = new StackValue(i, StackValue.Type.Pointer);
            HeapItem obj = heap.getEden().load(oldReference);

            if (obj != null) {
                //Passing as reference, no need to deep copy
                StackValue newReference = heap.getTenure().alloc(obj);
                referenceMap[heap.getEden().referenceToIndex(oldReference)] = heap.getTenure().referenceToIndex(newReference);
                heap.getEden().dealloc(oldReference);
            }
        }

        translateReferences(referenceMap);
    }



    protected void translateReferences(int[] referenceMap) throws HeapOverflow {
        for (int objRef: referenceMap){
            HeapItem heapObj = heap.getTenure().load(heap.getTenure().indexToReference(objRef));

            //Only change fields of Object, not PrimitiveArray
            if (heapObj != null && heapObj instanceof cz.cvut.fit.ostrajava.Interpreter.Memory.Object) {
                Object obj = (Object)heapObj;

                for (int i = 0; i < obj.getFieldsNumber(); i++) {
                    StackValue fieldValue = obj.getField(i);

                    if (fieldValue.isPointer() && !fieldValue.isNullPointer()) {

                        //If it points to tenure, there is nothing to translate
                        if (heap.isEdenReference(fieldValue)) {
                            int newIndex = referenceMap[heap.getEden().referenceToIndex(fieldValue)];
                            obj.setField(i, heap.getTenure().indexToReference(newIndex));
                        }
                    }
                }
            }
        }
    }
}

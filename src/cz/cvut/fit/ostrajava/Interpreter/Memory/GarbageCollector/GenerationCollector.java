package cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector;

import cz.cvut.fit.ostrajava.Interpreter.Frame;
import cz.cvut.fit.ostrajava.Interpreter.Memory.*;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Object;
import cz.cvut.fit.ostrajava.Interpreter.Stack;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tomaskohout on 11/29/15.
 */
public class GenerationCollector extends GarbageCollector {

    GenerationHeap heap;
    Stack stack;

    //Maximum size is edenSize
    List<DirtyLink> dirtyLinks;

    public GenerationCollector(Stack stack, GenerationHeap heap) {
        super(heap);
        this.heap = heap;
        this.stack = stack;

        dirtyLinks = new ArrayList<>();
    }

    @Override
    public Set<StackValue> run(Set<StackValue> roots) throws HeapOverflow{

        Set<StackValue> stackRoots = GarbageCollector.getRootsFromStack(stack);
        Set<StackValue> tenureDirtyLinks = collect(heap.getEden(), stackRoots, dirtyLinksToRoots());

        //if there is not enough space in tenure, clean it
        if (tenureOutOfSpace()){
            collect(heap.getTenure(), stackRoots, tenureDirtyLinks);

            //If still full - overflow
            if (tenureOutOfSpace()){
                throw new HeapOverflow();
            }
        }

        //Move all survivors to tenure
        moveEdenToTenure();

        dirtyLinks.clear();

        return null;
    }

    public void addDirtyLink(StackValue from, StackValue reference){
        if (!reference.isPointer() || reference.isNullPointer()){
            return;
        }

        if (heap.isEdenReference(from)){
            //We will find the eden -> tenure dirty links in garbage collection
            return;
        }

        // it's tenure object
        // assigning reference to eden
        if (heap.isEdenReference(reference)){
            dirtyLinks.add(new DirtyLink(from, reference));
        }
    }

    protected Set<StackValue> dirtyLinksToRoots(){
        Set<StackValue> roots = new HashSet<>();
        for (DirtyLink link:  dirtyLinks){
            roots.add(link.getReference());
        }
        return roots;
    }

    protected boolean tenureOutOfSpace(){
        return heap.getTenure().spaceLeft() <= heap.getEden().spaceUsed();
    }

    protected Set<StackValue> collect(Heap heapToCollect, Set<StackValue> stackRoots, Set<StackValue> dirtyLinks) throws HeapOverflow {
        //Run eden garbage collection on stack roots
        Set<StackValue> roots = new HashSet<StackValue>();
        roots.addAll(stackRoots);
        roots.addAll(dirtyLinks);

        return heapToCollect.getGarbageCollector().run(roots);
    }



    protected void moveEdenToTenure() throws HeapOverflow{


        StackValue[] referenceMap = new StackValue[heap.getEdenSize()];

        for (int i = 0; i < heap.getEdenSize(); i++) {
            StackValue oldReference = heap.getEden().indexToReference(i);
            HeapItem obj = heap.getEden().load(oldReference);

            if (obj != null) {
                //Passing as reference, no need to deep copy
                StackValue newReference = heap.getTenure().alloc(obj);
                referenceMap[heap.getEden().referenceToIndex(oldReference)] = newReference;
                heap.getEden().dealloc(oldReference);
            }
        }

        translateReferencesFromEden(referenceMap);
        translateReferencesOnStack(referenceMap);
        translateDirtyReferences(referenceMap);
    }



    protected void translateReferencesFromEden(StackValue[] referenceMap){
        for (StackValue objRef: referenceMap){
            if (objRef == null){
                continue;
            }
            HeapItem heapObj = heap.getTenure().load(objRef);

            //Only change fields of Object, not PrimitiveArray
            if (heapObj instanceof Object) {
                Object obj = (Object)heapObj;
                translateReferencesInObject(obj, referenceMap);
            }
        }


    }

    protected void translateReferencesInObject(Object obj, StackValue[] referenceMap){
        for (int i = 0; i < obj.getFieldsNumber(); i++) {
            StackValue fieldValue = obj.getField(i);

            if (fieldValue.isPointer() && !fieldValue.isNullPointer()) {

                //If it points to tenure, there is nothing to translate
                if (heap.isEdenReference(fieldValue)) {
                    StackValue newRef = translateReference(fieldValue, referenceMap);
                    obj.setField(i,newRef);
                }
            }
        }
    }

    protected void translateDirtyReferences(StackValue[] referenceMap){
        Set<StackValue> roots = new HashSet<>();
        for (DirtyLink link:  dirtyLinks){
            Object obj = heap.getTenure().loadObject(link.getFrom());
            translateReferencesInObject(obj, referenceMap);
        }
    }



    protected StackValue translateReference(StackValue reference, StackValue[] referenceMap){
        return referenceMap[heap.getEden().referenceToIndex(reference)];
    }

    protected void translateReferencesOnStack(StackValue[] referenceMap){
        for (int i = 0; i < stack.getFramesNumber(); i++) {
            Frame frame = stack.getFrame(i);

            for (int j = 0; j < frame.getLocalVariablesCount(); j++){
                StackValue reference = frame.loadVariable(j);
                //Translate every variable on stack if it's eden reference
                if (reference.isPointer() && !reference.isNullPointer() && heap.isEdenReference(reference)) {
                    StackValue newRef = translateReference(reference, referenceMap);
                    frame.storeVariable(j, newRef);
                }
            }
        }
    }
}

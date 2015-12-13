package cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector;

import cz.cvut.fit.ostrajava.Interpreter.Debugger;
import cz.cvut.fit.ostrajava.Interpreter.Frame;
import cz.cvut.fit.ostrajava.Interpreter.Memory.*;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Object;
import cz.cvut.fit.ostrajava.Interpreter.Stack;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tomaskohout on 11/29/15.
 */




public class GenerationCollector extends GarbageCollector {
    private static final Logger log = Logger.getLogger( GenerationCollector.class.getName() );

    GenerationHeap heap;
    Stack stack;

    //Maximum size is edenSize
    List<DirtyLink> generationDirtyLinks;

    public GenerationCollector(Stack stack, GenerationHeap heap) {
        super(heap);
        this.heap = heap;
        this.stack = stack;

        generationDirtyLinks = new ArrayList<>();
    }

    @Override
    public Set<StackValue> run(Set<StackValue> roots) throws HeapOverflow{

        //Debugger.print(heap);
        log.log(Level.FINE, "Collecting Eden");
        Set<StackValue> stackRoots = GarbageCollector.getRootsFromStack(stack);
        Set<StackValue> tenureDirtyLinks = collect(heap.getEden(), stackRoots, dirtyLinksToRoots());



        //if there is not enough space in tenure, clean it
        if (tenureOutOfSpace()){
            log.log(Level.FINE, "Collecting tenure");
            collect(heap.getTenure(), stackRoots, tenureDirtyLinks);

            //If still full - overflow
            if (tenureOutOfSpace()){
                throw new HeapOverflow();
            }
        }


        //Move all survivors to tenure
        moveEdenToTenure();

        generationDirtyLinks.clear();

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
            generationDirtyLinks.add(new DirtyLink(from, reference));
        }
    }

    protected Set<StackValue> dirtyLinksToRoots(){
        Set<StackValue> roots = new HashSet<>();
        log.log(Level.FINE, "Eden dirty links: ");

        for (DirtyLink link:  generationDirtyLinks){
            log.log(Level.FINE, link.getFrom() + "~" + link.getReference() + ", ");
            roots.add(link.getReference());
        }

        return roots;
    }

    protected boolean tenureOutOfSpace(){
        return heap.getTenure().spaceLeft() <= heap.getEden().spaceUsed();
    }

    protected Set<StackValue> collect(SimpleHeap heapToCollect, Set<StackValue> stackRoots, Set<StackValue> dirtyLinks) throws HeapOverflow {
        log.log(Level.FINE, "Roots: " + stackRoots);
        log.log(Level.FINE, "Dirty links: " + dirtyLinks);


        //Run eden garbage collection on stack roots
        Set<StackValue> roots = new HashSet<StackValue>();
        roots.addAll(stackRoots);
        roots.addAll(dirtyLinks);


        int used = heapToCollect.spaceUsed();
        Set<StackValue> heapDirtyLinks =  heapToCollect.getGarbageCollector().run(roots);

        int collected = used - heapToCollect.spaceUsed();

        log.log(Level.FINE, "Collected " + collected + " items");

        return heapDirtyLinks;
    }





    protected void moveEdenToTenure() throws HeapOverflow{


        StackValue[] referenceMap = new StackValue[heap.getEdenSize()];

        log.log(Level.FINE, "Moving to tenure");

        for (int i = 0; i < heap.getEdenSize(); i++) {
            StackValue oldReference = heap.getEden().indexToReference(i);
            HeapItem obj = heap.getEden().load(oldReference);

            if (obj != null) {
                //Passing as reference, no need to deep copy
                StackValue newReference = heap.getTenure().alloc(obj);
                referenceMap[heap.getEden().referenceToIndex(oldReference)] = newReference;
                log.log(Level.FINE, oldReference + ", ");

                heap.getEden().dealloc(oldReference);
            }
        }

        log.log(Level.FINE, "Translating eden");
        translateReferencesFromEden(referenceMap);

        log.log(Level.FINE, "Translating stack");
        translateReferencesOnStack(referenceMap);

        log.log(Level.FINE, "Translating dirty links");
        translateDirtyReferences(referenceMap);
    }



    protected void translateReferencesFromEden(StackValue[] referenceMap){
        for (StackValue objRef: referenceMap){
            if (objRef == null){
                continue;
            }
            HeapItem heapObj = heap.getTenure().load(objRef);

            if (heapObj instanceof Object) {
                Object obj = (Object)heapObj;
                translateReferencesInObject(obj, referenceMap);
            }else if (heapObj instanceof Array){
                translateReferencesInArray((Array)heapObj, referenceMap);
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

    protected void translateReferencesInArray(Array array, StackValue[] referenceMap){
        for (int i = 0; i < array.getSize(); i++) {
            StackValue value = array.get(i);

            if (value.isPointer() && !value.isNullPointer()) {

                //If it points to tenure, there is nothing to translate
                if (heap.isEdenReference(value)) {
                    StackValue newRef = translateReference(value, referenceMap);
                    array.set(i, newRef);
                }
            }
        }
    }

    protected void translateDirtyReferences(StackValue[] referenceMap){
        for (DirtyLink link:  generationDirtyLinks){
            HeapItem obj = heap.getTenure().load(link.getFrom());
            log.log(Level.FINE, link.getFrom() + "~" + link.getReference());

            if (obj == null){
                continue;
            }
            if (obj instanceof Object) {
                translateReferencesInObject((Object)obj, referenceMap);
            }else{
                translateReferencesInArray((Array)obj, referenceMap);
            }
        }

        return;
    }



    protected StackValue translateReference(StackValue reference, StackValue[] referenceMap){
        StackValue translated = referenceMap[heap.getEden().referenceToIndex(reference)];
        log.log(Level.FINE, reference + "->" + translated);
        return translated;
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

            //There can also be references directly on stack
            for (int j = frame.getStackOffset(); j < frame.getSize(); j+=StackValue.size){
                StackValue reference = frame.get(j);
                //Translate every value from stack if it's eden reference
                if (reference.isPointer() && !reference.isNullPointer() && heap.isEdenReference(reference)) {
                    StackValue newRef = translateReference(reference, referenceMap);
                    frame.set(j, newRef);
                }
            }
        }
    }
}

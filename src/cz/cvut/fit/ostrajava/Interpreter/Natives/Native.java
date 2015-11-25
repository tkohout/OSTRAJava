package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Interpreter.*;

/**
 * Created by tomaskohout on 11/23/15.
 */
public abstract class Native {

    protected Heap heap;
    protected ClassPool classPool;

    public Native(ClassPool classPool, Heap heap){
        this.heap = heap;
        this.classPool = classPool;
    }

    public abstract void invoke(cz.cvut.fit.ostrajava.Interpreter.Array object);
}

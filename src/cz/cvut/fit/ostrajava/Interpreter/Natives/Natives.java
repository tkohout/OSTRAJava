package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Object;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomaskohout on 11/24/15.
 */
public class Natives {

    private Map<String, Native> nativesMap;
    protected Heap heap;
    protected ClassPool classPool;

    public Natives(ClassPool classPool, Heap heap){
        this.heap = heap;
        this.classPool = classPool;
        init();
    }

    private void init(){
        if (nativesMap == null) {
            nativesMap = new HashMap<>();
            nativesMap.put("pravit:dryst", new PrintString(this.classPool, this.heap));
        }
    }



    public void invoke(String descriptor, Object... args) throws InterpreterException {

        if (nativesMap.containsKey(descriptor)){
            nativesMap.get(descriptor).invoke(args[0]);
        }else{
            throw new InterpreterException("Trying to call non-existent method '" + descriptor + "'");
        }
    }

}

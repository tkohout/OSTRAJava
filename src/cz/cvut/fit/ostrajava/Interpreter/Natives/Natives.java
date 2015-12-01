package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Natives.IO.Console.*;
import cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File.CloseReader;
import cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File.OpenReader;
import cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File.ReadLine;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Math.LogInt;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Math.PowInt;
import cz.cvut.fit.ostrajava.Type.Types;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tomaskohout on 11/24/15.
 */
public class Natives {

    protected  Map<String, Native> nativesMap;

    protected Heap heap;

    public Natives(Heap heap)
    {
        this.heap = heap;
        init();
    }

    private void init(){
        if (nativesMap == null) {
            nativesMap = new HashMap<>();
            nativesMap.put("print:" + Types.Char(), new PrintChar(heap));
            nativesMap.put("print:" + Types.CharArray(), new PrintChars(heap));
            nativesMap.put("print:" + Types.Number(), new PrintInt(heap));
            nativesMap.put("print:" + Types.Boolean(), new PrintBool(heap));
            nativesMap.put("print:" + Types.Float(), new PrintFloat(heap));
            nativesMap.put("logint:" + Types.Number() + ":" + Types.Number(), new LogInt(heap));
            nativesMap.put("powint:" + Types.Number() + ":" + Types.Number(), new PowInt(heap));
            nativesMap.put("openreader:" + Types.CharArray(), new OpenReader(heap));
            nativesMap.put("readline:" + Types.Number(), new ReadLine(heap));
            nativesMap.put("closereader:" + Types.Number(), new CloseReader(heap));
        }
    }

    public boolean nativeExist(String descriptor){
        return (nativesMap.containsKey(descriptor));
    }


    public StackValue invoke(String descriptor, StackValue[] args) throws InterpreterException, HeapOverflow {

        if (nativeExist(descriptor)){
            return nativesMap.get(descriptor).invoke(args);
        }else{
            throw new InterpreterException("Trying to call non-existent method '" + descriptor + "'");
        }
    }

}

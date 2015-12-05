package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Compiler.Method;
import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Array.CharArraySize;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Array.IntArraySize;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Array.ReferenceArraySize;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Conversion.CharArrayToInt;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Conversion.IntToCharArray;
import cz.cvut.fit.ostrajava.Interpreter.Natives.IO.Console.*;
import cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File.CloseReader;
import cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File.OpenReader;
import cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File.ReadLine;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Math.LogInt;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Math.PowInt;
import cz.cvut.fit.ostrajava.Type.Type;
import cz.cvut.fit.ostrajava.Type.Types;
import  cz.cvut.fit.ostrajava.Compiler.Class;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tomaskohout on 11/24/15.
 */
public class Natives {

    protected  Map<String, Native> nativesMap;
    protected  Class nativeClass;
    protected  ClassPool classPool;
    protected Heap heap;

    public Natives(Heap heap, ClassPool classPool)
    {
        this.heap = heap;
        this.classPool = classPool;
        init();
    }

    private List<Type> argsList(Type ... types){
        List<Type> list = new ArrayList<>();
        for (Type type:types){
            list.add(type);
        }

        return list;
    }

    private void addNative(String descriptor, Native implementation){
        nativeClass.addMethod(new Method(descriptor));
        nativesMap.put(descriptor, implementation);
    }

    private void init(){


        if (nativesMap == null) {
            nativeClass = new Class("Natives", null);
            nativesMap = new HashMap<>();
            addNative("print:" + Types.Char(), new PrintChar(heap));
            addNative("print:" + Types.CharArray(), new PrintChars(heap));
            addNative("print:" + Types.Number(), new PrintInt(heap));
            addNative("print:" + Types.Boolean(), new PrintBool(heap));
            addNative("print:" + Types.Float(), new PrintFloat(heap));
            addNative("logint:" + Types.Number() + ":" + Types.Number(), new LogInt(heap));
            addNative("powint:" + Types.Number() + ":" + Types.Number(), new PowInt(heap));
            addNative("openreader:" + Types.CharArray(), new OpenReader(heap));
            addNative("readline:" + Types.Number(), new ReadLine(heap));
            addNative("closereader:" + Types.Number(), new CloseReader(heap));
            addNative("arraysize:" + Types.CharArray(), new CharArraySize(heap));
            addNative("arraysize:" + Types.NumberArray(), new IntArraySize(heap));
            addNative("arraysize:" + "bazmek[]", new ReferenceArraySize(heap));
            addNative("chararraytoint:" + Types.CharArray(), new CharArrayToInt(heap));
            addNative("inttochararray:" + Types.Number(), new IntToCharArray(heap));
        }
    }

    public boolean nativeExist(String descriptor){
        try {
            nativeClass.lookupMethod(descriptor, classPool);
            return true;
        } catch (LookupException e) {
            return false;
        }
    }


    public StackValue invoke(String descriptor, StackValue[] args) throws InterpreterException, HeapOverflow {

        try {

            Method method = nativeClass.lookupMethod(descriptor, classPool);

            return nativesMap.get(method.getDescriptor()).invoke(args);
        }catch(LookupException ex){
            throw new InterpreterException("Trying to call non-existent method '" + descriptor + "'");
        }
    }

}

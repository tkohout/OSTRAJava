package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File;

import cz.cvut.fit.ostrajava.Interpreter.Converter;
import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Array;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Created by tomaskohout on 12/1/15.
 */
public class OpenReader extends Native {

    public OpenReader(Heap heap) {
        super(heap);
    }

    @Override
    public StackValue invoke(StackValue[] args) throws InterpreterException {
        StackValue charRef = args[0];

        Array array = heap.loadArray(charRef);
        char[] chars = Converter.arrayToCharArray(array);

        String fileName = new String(chars);
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            int handle = Readers.getInstance().add(br);
            return new StackValue(handle, StackValue.Type.Primitive);
        } catch (FileNotFoundException e) {
            throw new InterpreterException(e.getMessage());
        }
    }
}

package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File;

import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Memory.PrimitiveArray;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by tomaskohout on 12/1/15.
 */
public class ReadLine extends Native {


    public ReadLine(Heap heap) {
        super(heap);
    }

    @Override
    public StackValue invoke(StackValue[] args) throws HeapOverflow, InterpreterException {
        int handle = args[0].intValue();

        BufferedReader br = Readers.getInstance().get(handle);

        try {
            StackValue reference;
            String line = br.readLine();

            if (line != null){

                //Create array of chars
                reference = heap.allocArray(line.length());
                PrimitiveArray charArray = heap.loadArray(reference);

                for (int i = 0; i < line.length(); i++){
                    charArray.set(i, line.charAt(i));
                }

            }else{
                //Null reference
                reference = new StackValue(0, StackValue.Type.Pointer);
            }

            return reference;

        } catch (IOException e) {
            throw new InterpreterException(e.getMessage());
        }

    }
}

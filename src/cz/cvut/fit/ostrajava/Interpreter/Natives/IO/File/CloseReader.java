package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File;

import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by tomaskohout on 12/1/15.
 */
public class CloseReader extends Native {
    public CloseReader(Heap heap) {
        super(heap);
    }

    @Override
    public StackValue invoke(StackValue[] args) throws HeapOverflow, InterpreterException {
        int handle = args[0].intValue();

        BufferedReader br = Readers.getInstance().get(handle);
        try {
            br.close();
            Readers.getInstance().remove(handle);
        } catch (IOException e) {
            throw new InterpreterException(e.getMessage());
        }
        return null;
    }
}

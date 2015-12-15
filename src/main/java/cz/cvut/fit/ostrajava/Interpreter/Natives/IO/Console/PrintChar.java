package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.Console;

import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintChar extends Native {
    public PrintChar(Heap heap) {
        super(heap);
    }

    @Override
    //Arguments: char
    public StackValue invoke(StackValue[] args) {
        StackValue arg = args[0];
        char value = arg.charValue();
        System.out.println(value);
        return null;
    }
}

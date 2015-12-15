package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.Console;

import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintInt extends Native {

    public PrintInt(Heap heap) {
        super(heap);
    }

    //arguments: int
    public StackValue invoke(StackValue args[])  {
        StackValue arg = args[0];
        int value = arg.intValue();

        System.out.println(value);
        return null;
    }
}

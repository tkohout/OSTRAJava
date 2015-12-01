package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.Console;

import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintBool extends Native {

    public PrintBool(Heap heap) {
        super(heap);
    }

    //arguments: bool
    public StackValue invoke(StackValue args[])  {
        StackValue arg = args[0];
        boolean value = arg.boolValue();

        System.out.println(value == false ? "false" : "true");

        return null;
    }
}

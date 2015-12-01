package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.Console;

import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintFloat extends Native {

    public PrintFloat(Heap heap) {
        super(heap);
    }

    //arguments: float
    public StackValue invoke(StackValue args[])  {
        StackValue arg = args[0];
        float value = arg.floatValue();
        System.out.println(value);
        return null;
    }
}

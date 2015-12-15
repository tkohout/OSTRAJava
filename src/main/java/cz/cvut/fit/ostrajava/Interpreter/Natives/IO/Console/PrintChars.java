package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.Console;

import cz.cvut.fit.ostrajava.Interpreter.Converter;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Array;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintChars extends Native {

    public PrintChars(Heap heap) {
        super(heap);
    }

    //arguments: char[]
    public StackValue invoke(StackValue args[])  {
        StackValue ref = args[0];

        Array array = heap.loadArray(ref);
        char[] chars = Converter.arrayToCharArray(array);

        System.out.println(chars);
        return null;
    }
}

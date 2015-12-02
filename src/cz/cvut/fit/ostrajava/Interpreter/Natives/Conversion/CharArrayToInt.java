package cz.cvut.fit.ostrajava.Interpreter.Natives.Conversion;

import cz.cvut.fit.ostrajava.Interpreter.Converter;
import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Array;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 12/1/15.
 */
public class CharArrayToInt extends Native {

    public CharArrayToInt(Heap heap) {
        super(heap);
    }

    @Override
    public StackValue invoke(StackValue[] args) throws HeapOverflow, InterpreterException {
        StackValue ref = args[0];

        Array array = heap.loadArray(ref);
        char[] chars = Converter.arrayToCharArray(array);
        int i = Integer.parseInt(new String(chars));

        StackValue result = new StackValue(i, StackValue.Type.Primitive);
        return result;
    }
}

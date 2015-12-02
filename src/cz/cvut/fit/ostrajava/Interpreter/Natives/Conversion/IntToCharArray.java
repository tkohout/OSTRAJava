package cz.cvut.fit.ostrajava.Interpreter.Natives.Conversion;

import cz.cvut.fit.ostrajava.Interpreter.InterpreterException;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Array;
import cz.cvut.fit.ostrajava.Interpreter.Memory.Heap;
import cz.cvut.fit.ostrajava.Interpreter.Memory.HeapOverflow;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Native;
import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 12/1/15.
 */
public class IntToCharArray extends Native {
    public IntToCharArray(Heap heap) {
        super(heap);
    }

    @Override
    public StackValue invoke(StackValue[] args) throws HeapOverflow, InterpreterException {
        int number = args[0].intValue();

        String s = Integer.toString(number);

        //Create array of chars
        StackValue reference = heap.allocArray(s.length());
        Array charArray = heap.loadArray(reference);

        for (int i = 0; i < s.length(); i++){
            StackValue charValue = new StackValue(s.charAt(i), StackValue.Type.Primitive);
            charArray.set(i, charValue);
        }

        return reference;
    }
}

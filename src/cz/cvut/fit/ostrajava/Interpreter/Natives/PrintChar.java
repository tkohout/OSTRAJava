package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintChar extends Native {
    @Override
    //Arguments: char
    public NativeValue invoke(NativeValue[] args) {
        NativeValue arg = args[0];
        char value = arg.charValue();
        System.out.println(value);
        return null;
    }
}

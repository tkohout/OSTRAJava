package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintChar extends Native {
    @Override
    //Arguments: char
    public void invoke(NativeArgument[] args) {
        NativeArgument arg = args[0];
        char value = arg.charValue();
        System.out.println(value);
    }
}

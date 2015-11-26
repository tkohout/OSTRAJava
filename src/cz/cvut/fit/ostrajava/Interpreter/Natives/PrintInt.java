package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintInt extends Native{

    //arguments: int
    public NativeValue invoke(NativeValue args[])  {
        NativeValue arg = args[0];
        int value = arg.intValue();

        System.out.println(value);
        return null;
    }
}

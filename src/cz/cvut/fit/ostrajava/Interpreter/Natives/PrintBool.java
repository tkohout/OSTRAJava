package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintBool extends Native{

    //arguments: bool
    public NativeValue invoke(NativeValue args[])  {
        NativeValue arg = args[0];
        boolean value = arg.boolValue();

        System.out.println(value == false ? "false" : "true");

        return null;
    }
}

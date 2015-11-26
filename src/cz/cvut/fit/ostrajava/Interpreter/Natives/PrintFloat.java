package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintFloat extends Native{

    //arguments: float
    public NativeValue invoke(NativeValue args[])  {
        NativeValue arg = args[0];
        float value = arg.floatValue();
        System.out.println(value);
        return null;
    }
}

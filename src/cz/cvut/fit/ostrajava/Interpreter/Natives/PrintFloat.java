package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintFloat extends Native{

    //arguments: float
    public void invoke(NativeArgument args[])  {
        NativeArgument arg = args[0];
        float value = arg.floatValue();
        System.out.println(value);
    }
}

package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class PrintBool extends Native{

    //arguments: bool
    public void invoke(NativeArgument args[])  {
        NativeArgument arg = args[0];
        boolean value = arg.boolValue();

        System.out.println(value == false ? "false" : "true");
    }
}

package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintBools extends Native {

    //arguments: bool[]
    public void invoke(NativeArgument args[])  {
            NativeArgument arg = args[0];
            boolean[] bools = arg.boolArray();
            System.out.println(bools);
    }
}

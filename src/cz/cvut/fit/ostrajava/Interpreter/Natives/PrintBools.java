package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintBools extends Native {

    //arguments: bool[]
    public NativeValue invoke(NativeValue args[])  {
        NativeValue arg = args[0];
        boolean[] bools = arg.boolArray();
        System.out.println(bools);
        return null;
    }
}

package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintInts extends Native {

    //arguments: int[]
    public NativeValue invoke(NativeValue args[])  {
        NativeValue arg = args[0];
        int[] ints = arg.intArray();
        System.out.println(ints);
        return null;
    }
}

package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintInts extends Native {

    //arguments: int[]
    public void invoke(NativeArgument args[])  {
            NativeArgument arg = args[0];
            int[] ints = arg.intArray();
            System.out.println(ints);
    }
}

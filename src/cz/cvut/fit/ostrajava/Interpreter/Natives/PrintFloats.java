package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintFloats extends Native {

    //arguments: float[]
    public void invoke(NativeArgument args[])  {
            NativeArgument arg = args[0];
            float[] floats = arg.floatArray();
            System.out.println(floats);
    }
}

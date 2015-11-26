package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintFloats extends Native {

    //arguments: float[]
    public NativeValue invoke(NativeValue args[])  {
        NativeValue arg = args[0];
        float[] floats = arg.floatArray();
        System.out.println(floats);
        return null;
    }
}

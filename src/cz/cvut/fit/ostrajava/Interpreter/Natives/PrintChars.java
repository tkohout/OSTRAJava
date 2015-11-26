package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintChars extends Native {

    //arguments: char[]
    public NativeValue invoke(NativeValue args[])  {
        NativeValue arg = args[0];
        char[] chars = arg.charArray();

        System.out.println(chars);
        return null;
    }
}

package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/23/15.
 */
public abstract class Native {
    public abstract NativeValue invoke(NativeValue args[]);
}

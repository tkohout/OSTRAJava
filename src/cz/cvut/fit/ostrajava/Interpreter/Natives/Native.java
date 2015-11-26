package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Interpreter.*;

/**
 * Created by tomaskohout on 11/23/15.
 */
public abstract class Native {
    public abstract void invoke(NativeArgument args[]);
}

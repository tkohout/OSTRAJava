package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;
import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Object;

import java.util.List;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintChars extends Native {

    //arguments: char[]
    public void invoke(NativeArgument args[])  {
            NativeArgument arg = args[0];
            char[] chars = arg.charArray();

            System.out.println(chars);
    }
}

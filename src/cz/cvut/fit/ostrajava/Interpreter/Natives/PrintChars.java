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

    public PrintChars(ClassPool classPool, Heap heap) {
        super(classPool, heap);
    }

    public void invoke(Array array)  {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < array.getSize(); i++){
                sb.append((char)array.get(i));
            }

            System.out.println(sb.toString());
    }
}

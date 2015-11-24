package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;
import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Object;

import java.util.List;

/**
 * Created by tomaskohout on 11/23/15.
 */
public class PrintString extends Native {

    public PrintString(ClassPool classPool, Heap heap) {
        super(classPool, heap);
    }

    public void invoke(Object object)  {

        try {
            InterpretedClass c = object.loadClass(classPool);
            int position = c.lookupField("chachari");
            int arrayRef = object.getField(position);
            Array array = (Array) this.heap.load(arrayRef);

            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < array.getSize(); i++){
                sb.append((char)array.get(i));
            }

            System.out.println(sb.toString());

        } catch (LookupException|InterpreterException e) {
            e.printStackTrace();
        }

    }
}

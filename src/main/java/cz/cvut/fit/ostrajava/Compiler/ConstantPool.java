package cz.cvut.fit.ostrajava.Compiler;

import cz.cvut.fit.ostrajava.Interpreter.ClassPool;
import cz.cvut.fit.ostrajava.Interpreter.InterpretedClass;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by tomaskohout on 11/21/15.
 */
public class ConstantPool {


    Set<String> constants;

    public ConstantPool() {
        super();
        constants = new LinkedHashSet<>();
    }

    //Merge constants from all classes
    public ConstantPool(ClassPool classPool) {
        constants = new LinkedHashSet<>();

        //Go through all classes and methods and get constants
        for (InterpretedClass c: classPool.getClasses()){
            for (Method method: c.getMethods()){

                List<Instruction> instructions = method.getByteCode().getInstructions();
                ConstantPool pool = c.getConstantPool();

                //We have to change constants positions
                for (Instruction inst: instructions){
                    switch (inst.getInstruction()){
                        case PushConstant:
                        case New:
                        case InvokeVirtual:
                        case InvokeSpecial:
                        case GetField:
                        case PutField:
                        case InvokeStatic:
                        case PushFloat:
                            int i = 0;
                            for (int operand: inst.getOperands()){
                                String constant = pool.getConstant(operand);
                                int newPosition = addConstant(constant);
                                inst.setOperand(i, newPosition);
                                i++;
                            }
                            break;
                    }
                }
            }
        }
    }


    public int getSize(){
        return constants.size();
    }


    public int addConstant(String constant){
        constants.add(constant);
        return getConstantPosition(constant);
    }

    public String getConstant(int position){
        return (new ArrayList<>(constants)).get(position);
    }

    public int getConstantPosition(String name){
        int i = 0;
        for (String constant:constants){
            if (constant.equals(name)) {
                return i;
            }
            i++;
        }

        return -1;
    }


}

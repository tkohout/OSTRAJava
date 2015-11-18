package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;

import java.util.Iterator;
import java.util.List;

import static cz.cvut.fit.ostrajava.Compiler.InstructionSet.*;

/**
 * Created by tomaskohout on 11/17/15.
 */
public class OSTRAJavaInterpreter {

    final String MAIN_CLASS_NAME = "ostrava";
    final String MAIN_METHOD_NAME = "rynek";


    List<ClassFile> compiledClassFiles;


    public OSTRAJavaInterpreter(List<ClassFile> compiledClassFiles){
        this.compiledClassFiles= compiledClassFiles;
    }


    public void run() throws InterpreterException {
        ClassFile mainClass = findMainClass(compiledClassFiles);
        Method mainMethod = mainClass.getMethod(MAIN_METHOD_NAME);

        if (mainMethod == null){
            throw new InterpreterException("Main method '" + MAIN_METHOD_NAME + "' is missing");
        }

        interpret(mainMethod);
    }

    public void interpret(Method method){
        Frame frame = new Frame(256, 0, 0, method);

        ByteCode byteCode = method.getByteCode();
        List<Instruction> instructions = byteCode.getInstructions();
        Iterator<Instruction> itr = instructions.iterator();

        while (itr.hasNext()){
            Instruction instruction = itr.next();
            List<String> operands = instruction.getOperands();
            int var;

            switch (instruction.getInstruction()) {
                case PushInteger:
                    frame.push(Integer.parseInt(operands.get(0)));
                    break;
                case StoreInteger:
                    var = frame.pop();
                    frame.storeVariable(Integer.parseInt(operands.get(0)), var);
                    break;
                case LoadInteger:
                    var = frame.loadVariable(Integer.parseInt(operands.get(0)));
                    frame.push(var);
                    break;
                case AddInteger:
                    int a = frame.pop();
                    int b = frame.pop();
                    frame.push(a + b);
                    break;
            }
        }

        int res = frame.loadVariable(1);


        return;
    }

    public ClassFile findMainClass(List<ClassFile> classFiles) throws InterpreterException {
        for (ClassFile classFile:classFiles){
            if (classFile.getClassName().toLowerCase().equals(MAIN_CLASS_NAME)) {
                return classFile;
            }
        }

        throw new InterpreterException("Main class not found (Name of the class has to be '" + MAIN_CLASS_NAME + "')");
    }


}

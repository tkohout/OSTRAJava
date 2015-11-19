package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Iterator;
import java.util.List;

import static cz.cvut.fit.ostrajava.Compiler.InstructionSet.*;

/**
 * Created by tomaskohout on 11/17/15.
 */
public class OSTRAJavaInterpreter {

    final String MAIN_CLASS_NAME = "ostrava";
    final String MAIN_METHOD_NAME = "rynek";
    final int FRAMES_NUMBER = 64;
    final int FRAME_STACK_SIZE = 256;


    List<ClassFile> compiledClassFiles;

    Stack stack;

    public OSTRAJavaInterpreter(List<ClassFile> compiledClassFiles){
        this.compiledClassFiles= compiledClassFiles;
        this.stack = new Stack(FRAMES_NUMBER, FRAME_STACK_SIZE);
    }


    public void run() throws InterpreterException {
        ClassFile mainClass = findMainClass(compiledClassFiles);
        Method mainMethod = mainClass.getMethod(MAIN_METHOD_NAME);

        if (mainMethod == null){
            throw new InterpreterException("Main method '" + MAIN_METHOD_NAME + "' is missing");
        }

        interpret(mainMethod);
    }

    public void interpret(Method method) throws InterpreterException{

        //Create new frame for method
        stack.newFrame(0, 0, method);

        ByteCode byteCode = method.getByteCode();
        List<Instruction> instructions = byteCode.getInstructions();

        int position = 0;
        int instruction_size = instructions.size();

        //Move from one instruction to next
        while (position >= 0 && position < instruction_size){
            Instruction instruction = instructions.get(position);
            position = executeInstruction(position, instruction, stack);
        }

        //int res = frame.loadVariable(1);


        return;
    }

    public int executeInstruction(int position, Instruction instruction, Stack stack) throws InterpreterException{

        switch (instruction.getInstruction()) {
            case PushInteger:
            case StoreInteger:
            case LoadInteger:
                return executeStackInstruction(position, instruction, stack);

            case AddInteger:
            case SubstractInteger:
            case MultiplyInteger:
            case DivideInteger:
            case ModuloInteger:
                return executeArithmeticInstruction(position, instruction, stack);

            case IfCompareEqualInteger:
            case IfCompareNotEqualInteger:
            case IfCompareLessThanInteger:
            case IfCompareLessThanOrEqualInteger:
            case IfCompareGreaterThanInteger:
            case IfCompareGreaterThanOrEqualInteger:
                return executeCompareInstruction(position, instruction, stack);
            case GoTo:
                return executeGoToInstruction(position, instruction, stack);
            case ReturnVoid:
            case ReturnReference:
            case ReturnInteger:
                return executeReturnInstruction(position, instruction, stack);
            default:
                throw new NotImplementedException();
        }


    }

    public int executeReturnInstruction(int position, Instruction instruction, Stack stack) throws InterpreterException{
        switch (instruction.getInstruction()) {
            case ReturnVoid:
                stack.deleteCurrentFrame();
            break;
            case ReturnInteger: {
                int var = stack.currentFrame().pop();
                //Remove current frame
                stack.deleteCurrentFrame();

                //Push it on the calling frame
                stack.currentFrame().push(var);
            }
            break;
            case ReturnReference: {
                //TODO: change to reference later

                int var = stack.currentFrame().pop();
                //Remove current frame
                stack.deleteCurrentFrame();

                //Push it on the calling frame
                stack.currentFrame().push(var);
            }
            break;
        }
        return -1;
    }

    public int executeStackInstruction(int position, Instruction instruction, Stack stack) throws InterpreterException{
        switch (instruction.getInstruction()) {
            case PushInteger: {
                stack.currentFrame().push(Integer.parseInt(instruction.getOperand(0)));
            }
            break;
            case StoreInteger: {
                int var = stack.currentFrame().pop();
                stack.currentFrame().storeVariable(Integer.parseInt(instruction.getOperand(0)), var);
            }
            break;
            case LoadInteger: {
                int var = stack.currentFrame().loadVariable(Integer.parseInt(instruction.getOperand(0)));
                stack.currentFrame().push(var);
            }
            break;
        }
        return position+1;
    }

    public int executeArithmeticInstruction(int position, Instruction instruction, Stack stack) throws InterpreterException{
        int b = stack.currentFrame().pop();
        int a = stack.currentFrame().pop();

        switch (instruction.getInstruction()) {

            case AddInteger:
                stack.currentFrame().push(a + b);
               break;
            case SubstractInteger:
                stack.currentFrame().push(a - b);
               break;
            case MultiplyInteger:
                stack.currentFrame().push(a * b);
               break;
            case DivideInteger:

                if (b == 0){
                    throw new InterpreterException("Division by zero");
                }
                stack.currentFrame().push(a / b);
               break;
            case ModuloInteger:
                stack.currentFrame().push(a % b);
               break;
        }

        return position+1;
    }

    public int executeCompareInstruction(int position, Instruction instruction, Stack stack) throws InterpreterException {
        int b = stack.currentFrame().pop();
        int a = stack.currentFrame().pop();

        int operand = Integer.parseInt(instruction.getOperand(0));

        boolean condition = false;

        switch (instruction.getInstruction()) {
            case IfCompareEqualInteger:
                condition = (a == b);
                break;
            case IfCompareNotEqualInteger:
                condition = (a != b);
                break;
            case IfCompareLessThanInteger:
                condition = (a < b);
                break;
            case IfCompareLessThanOrEqualInteger:
                condition = (a <= b);
                break;
            case IfCompareGreaterThanInteger:
                condition = (a > b);
                break;
            case IfCompareGreaterThanOrEqualInteger:
                condition = (a >= b);
                break;
        }

        return  (condition) ? operand : position+1;
    }

    public int executeGoToInstruction(int position, Instruction instruction, Stack stack) throws InterpreterException {
        int jumpTo = Integer.parseInt(instruction.getOperand(0));
        return  jumpTo;
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

package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tomaskohout on 11/17/15.
 */
public class OSTRAJavaInterpreter {

    final String MAIN_CLASS_NAME = "ostrava";
    final String MAIN_METHOD_NAME = "rynek";
    final int FRAMES_NUMBER = 64;
    final int FRAME_STACK_SIZE = 256;
    final int MAX_HEAP_OBJECTS = 256;

    final int END_RETURN_ADDRESS = -1;


    ClassPool classPool;
    Stack stack;
    Heap heap;
    Instructions instructions;


    public OSTRAJavaInterpreter(List<Class> compiledClasses) throws InterpreterException, LookupException {
        this.stack = new Stack(FRAMES_NUMBER, FRAME_STACK_SIZE);
        this.classPool = new ClassPool(compiledClasses);
        this.heap = new Heap(MAX_HEAP_OBJECTS);
        this.instructions = new Instructions(classPool);
    }


    public void run() throws InterpreterException {

        InterpretedClass mainClass = null;
        InterpretedMethod mainMethod = null;

        try {
            mainClass = classPool.lookupClass(MAIN_CLASS_NAME);
        } catch (LookupException e) {
            throw new InterpreterException("Main class not found. The name has to be '" + MAIN_CLASS_NAME + "'");
        }

        try {
            mainMethod = mainClass.lookupMethod(MAIN_METHOD_NAME);
        }catch (LookupException e) {
                throw new InterpreterException("Main method not found. The name has to be '" + MAIN_METHOD_NAME + "'");
        }

        int objectPointer = heap.alloc(mainClass);

        //Create new frame for main method
        stack.newFrame(END_RETURN_ADDRESS, objectPointer, mainMethod);

        //Find instructions for main method
        int mainMethodPosition = mainMethod.getInstructionPosition();
        interpret(mainMethodPosition);
    }

    public void interpret(int startingPosition) throws InterpreterException{
        instructions.goTo(startingPosition);

        Iterator<Instruction> itr = instructions.getIterator();

        //Move from one instruction to next
        while (itr.hasNext()){
            Instruction instruction = itr.next();

            executeInstruction(instruction, stack);
        }

        return;
    }

    public void executeInstruction(Instruction instruction, Stack stack) throws InterpreterException{

        switch (instruction.getInstruction()) {
            case PushInteger:
            case StoreInteger:
            case LoadInteger:
            case StoreReference:
            case LoadReference:
                executeStackInstruction(instruction, stack);
                break;
            case AddInteger:
            case SubstractInteger:
            case MultiplyInteger:
            case DivideInteger:
            case ModuloInteger:
                executeArithmeticInstruction(instruction, stack);
                break;
            case IfCompareEqualInteger:
            case IfCompareNotEqualInteger:
            case IfCompareLessThanInteger:
            case IfCompareLessThanOrEqualInteger:
            case IfCompareGreaterThanInteger:
            case IfCompareGreaterThanOrEqualInteger:
                executeCompareInstruction(instruction, stack);
                break;
            case GoTo:
                executeGoToInstruction(instruction, stack);
                break;
            case ReturnVoid:
            case ReturnReference:
            case ReturnInteger:
                executeReturnInstruction(instruction, stack);
                break;
            case InvokeVirtual:
                executeInvokeInstruction(instruction, stack);
                break;
            default:
                throw new NotImplementedException();
        }


    }

    public void executeInvokeInstruction(Instruction instruction, Stack stack) throws InterpreterException{
        switch (instruction.getInstruction()) {
            case InvokeVirtual:
                //The method name is directly in instruction
                String methodName = instruction.getOperand(0);

                //Get object the method is called on
                int objectRef = stack.currentFrame().pop();
                Object object = heap.load(objectRef);

                InterpretedClass objectClass = object.loadClass(classPool);

                try {
                    InterpretedMethod method = objectClass.lookupMethod(methodName);

                    //Return to one instruction after this
                    int returnAddress = instructions.getCurrentPosition() + 1;

                    stack.newFrame(returnAddress, objectRef, method);

                    //Go to the method bytecode start
                    instructions.goTo(method.getInstructionPosition());

                } catch (LookupException e) {
                    throw new InterpreterException("Calling non-existing method " + methodName);
                }


                break;
        }
    }

    public void executeReturnInstruction(Instruction instruction, Stack stack) throws InterpreterException{

        int returnAddress = stack.currentFrame().getReturnAddress();

        switch (instruction.getInstruction()) {
            case ReturnVoid:
                stack.deleteCurrentFrame();
            break;
            case ReturnInteger: {
                int var = stack.currentFrame().pop();

                //Remove current frame
                stack.deleteCurrentFrame();

                //Push return value on the calling frame
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

        instructions.goTo(returnAddress);
    }

    public void executeStackInstruction(Instruction instruction, Stack stack) throws InterpreterException{
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

            //TODO: so far it's the same
            case StoreReference: {
                int var = stack.currentFrame().pop();
                stack.currentFrame().storeVariable(Integer.parseInt(instruction.getOperand(0)), var);
            }
            break;
            case LoadReference: {
                int var = stack.currentFrame().loadVariable(Integer.parseInt(instruction.getOperand(0)));
                stack.currentFrame().push(var);
            }
            break;

        }
    }

    public void executeArithmeticInstruction(Instruction instruction, Stack stack) throws InterpreterException{
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
    }

    public void executeCompareInstruction(Instruction instruction, Stack stack) throws InterpreterException {
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

        if (condition){
            instructions.goTo(operand);
        }
    }

    public void executeGoToInstruction(Instruction instruction, Stack stack) throws InterpreterException {

        int jumpTo = Integer.parseInt(instruction.getOperand(0));
        instructions.goTo(jumpTo);
    }




}

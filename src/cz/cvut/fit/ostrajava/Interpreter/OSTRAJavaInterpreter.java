package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.GarbageCollector;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.GenerationCollector;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.MarkAndSweepCollector;
import cz.cvut.fit.ostrajava.Interpreter.Memory.*;
import cz.cvut.fit.ostrajava.Interpreter.Natives.NativeValue;
import cz.cvut.fit.ostrajava.Interpreter.Natives.Natives;
import cz.cvut.fit.ostrajava.Type.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Iterator;
import java.util.List;

/**
 * Created by tomaskohout on 11/17/15.
 */
public class OSTRAJavaInterpreter {

    final String MAIN_CLASS_NAME = "ostrava";
    final String MAIN_METHOD_NAME = "rynek";
    final int FRAMES_NUMBER = 128;
    final int FRAME_STACK_SIZE = 256;
    final int MAX_HEAP_OBJECTS = 100;

    final int END_RETURN_ADDRESS = -1;


    ClassPool classPool;
    ConstantPool constantPool;
    Stack stack;
    GenerationHeap heap;
    Instructions instructions;
    Natives natives;


    public OSTRAJavaInterpreter(List<Class> compiledClasses) throws InterpreterException, LookupException {
        this.stack = new Stack(FRAMES_NUMBER, FRAME_STACK_SIZE);
        this.classPool = new ClassPool(compiledClasses);

        //Eden:Tenure 1:9
        this.heap = new GenerationHeap((int)(MAX_HEAP_OBJECTS * 0.1), (int)(MAX_HEAP_OBJECTS * 0.9), stack);

        GenerationCollector gc = new GenerationCollector(stack, heap);
        this.heap.setGarbageCollector(gc);

        this.instructions = new Instructions(classPool);
        this.constantPool = new ConstantPool(classPool);
        this.natives = new Natives();
    }


    public void run() throws InterpreterException, HeapOverflow {

        InterpretedClass mainClass = null;
        Method mainMethod = null;

        try {
            mainClass = classPool.lookupClass(MAIN_CLASS_NAME);
        } catch (LookupException e) {
            throw new InterpreterException("Main class not found. The name has to be '" + MAIN_CLASS_NAME + "'");
        }

        try {
            mainMethod =  mainClass.lookupMethod(MAIN_METHOD_NAME, classPool);
        }catch (LookupException e) {
                throw new InterpreterException("Main method not found. The name has to be '" + MAIN_METHOD_NAME + "'");
        }

        StackValue objectPointer = heap.allocObject(mainClass);

        //Create new frame for main method
        stack.newFrame(END_RETURN_ADDRESS, objectPointer, mainMethod);

        //Find instructions for main method
        int mainMethodPosition = ((InterpretedMethod)mainMethod).getInstructionPosition();
        interpret(mainMethodPosition);
    }

    public void interpret(int startingPosition) throws InterpreterException, HeapOverflow {
        instructions.goTo(startingPosition);

        Iterator<Instruction> itr = instructions.getIterator();

        //Move from one instruction to next
        while (itr.hasNext()){
            Instruction instruction = itr.next();

            executeInstruction(instruction, stack);
        }

        return;
    }

    public void executeInstruction(Instruction instruction, Stack stack) throws InterpreterException, HeapOverflow {

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
            case InvokeStatic:
                executeInvokeInstruction(instruction, stack);
                break;
            case Duplicate:
                executeDuplicateInstruction(instruction, stack);
                break;
            case New:
                executeNewInstruction(instruction, stack);
                break;
            case GetField:
            case PutField:
                executeFieldInstruction(instruction, stack);
                break;
            case Breakpoint:
                //Place breakpoint here
                break;
            case PushConstant:
                executeStringInstruction(instruction, stack);
                break;
            case NewArray:
            case StoreIntegerArray:
            case LoadIntegerArray:
                executeArrayInstruction(instruction, stack);
                break;
            default:
                throw new NotImplementedException();
        }


    }

    public void executeArrayInstruction(Instruction instruction, Stack stack) throws InterpreterException, HeapOverflow {
        switch (instruction.getInstruction()) {
            case NewArray:
                int size = stack.currentFrame().pop().intValue();

                StackValue reference = heap.allocArray(size);
                stack.currentFrame().push(reference);
                break;
            case StoreIntegerArray: {
                int value = stack.currentFrame().pop().intValue();;
                int index = stack.currentFrame().pop().intValue();;
                StackValue arrayRef = stack.currentFrame().pop();

                PrimitiveArray array = heap.loadArray(arrayRef);
                array.set(index, value);
            }
                break;
            case LoadIntegerArray: {
                int index = stack.currentFrame().pop().intValue();;
                StackValue arrayRef = stack.currentFrame().pop();;

                PrimitiveArray array = heap.loadArray(arrayRef);

                //TODO: Right now it's always primitive
                StackValue value = new StackValue(array.get(index), StackValue.Type.Primitive);
                stack.currentFrame().push(value);
            }
                break;
        }
    }

    public void executeStringInstruction(Instruction instruction, Stack stack) throws InterpreterException, HeapOverflow {
        int constPosition = instruction.getOperand(0);
        String constant = constantPool.getConstant(constPosition);

        //Create array of chars and push it on stack
        StackValue reference = heap.allocArray(constant.length());

        PrimitiveArray charArray = heap.loadArray(reference);

        for (int i = 0; i < constant.length(); i++){
            charArray.set(i, constant.charAt(i));
        }

        stack.currentFrame().push(reference);
    }



    public void executeNewInstruction(Instruction instruction, Stack stack) throws InterpreterException, HeapOverflow {
                int constPosition = instruction.getOperand(0);
                String className = constantPool.getConstant(constPosition);
                try {
                    InterpretedClass objectClass = classPool.lookupClass(className);

                    StackValue reference = heap.allocObject(objectClass);
                    stack.currentFrame().push(reference);

                } catch (LookupException e) {
                    throw new InterpreterException("Trying to instantiate non-existent class '" + className + "'");
                }
    }

    public void executeDuplicateInstruction(Instruction instruction, Stack stack) throws InterpreterException {
        StackValue value = stack.currentFrame().pop();

        stack.currentFrame().push(value);
        stack.currentFrame().push(value);
    }

    public void executeFieldInstruction(Instruction instruction, Stack stack) throws InterpreterException {
        StackValue value = null;

        //If we are setting we must first pop the value
        if (instruction.getInstruction() == InstructionSet.PutField){
            value = stack.currentFrame().pop();
        }

        //Get object and find the field
        StackValue reference = stack.currentFrame().pop();

        int constPosition = instruction.getOperand(0);
        String fieldName = constantPool.getConstant(constPosition);

        cz.cvut.fit.ostrajava.Interpreter.Memory.Object object = heap.loadObject(reference);

        InterpretedClass objectClass = object.loadClass(classPool);
        try {
            int fieldPosition = objectClass.lookupField(fieldName);

            switch (instruction.getInstruction()) {
                case GetField:
                    stack.currentFrame().push(object.getField(fieldPosition));
                break;

                case PutField:
                    heap.addDirtyLink(reference, value);

                    object.setField(fieldPosition, value);
                    break;
            }

        } catch (LookupException e) {
            throw new InterpreterException("Trying to access non-existent field '" + fieldName + "'");
        }
    }

    public void executeInvokeInstruction(Instruction instruction, Stack stack) throws InterpreterException{


                int constPosition = instruction.getOperand(0);
                String methodDescriptor = constantPool.getConstant(constPosition);

                StackValue objectRef;

                String className = new Method(methodDescriptor).getClassName();

                //Method class was not found in compilation that means it's a native
                if (className == null){
                    invokeNative(methodDescriptor);
                    return;
                }


                try {
                    //Normal method
                    if (instruction.getInstruction() == InstructionSet.InvokeVirtual) {
                        //Get object the method is called on
                        objectRef = stack.currentFrame().pop();

                    //Static method
                    }else{
                        //Set This to null pointer
                        objectRef = new StackValue(0, StackValue.Type.Pointer);
                    }

                    InterpretedClass objectClass = classPool.lookupClass(className);

                    //Lookup real interpreted method
                    InterpretedMethod method = objectClass.lookupMethod(methodDescriptor, classPool);

                    //Return to next instruction
                    int returnAddress = instructions.getCurrentPosition() + 1;

                    //Pop arguments from the caller stack
                    int numberOfArgs = method.getArgs().size();
                    StackValue[] argValues = new StackValue[numberOfArgs];

                    for (int i = 0; i<numberOfArgs; i++){
                        argValues[i] = stack.currentFrame().pop();
                    }

                    stack.newFrame(returnAddress, objectRef, method);

                    //Store arguments as variables in callee stack
                    for (int i = 0; i<numberOfArgs; i++){
                        //Start with 1 index, 0 is reserved for This
                        stack.currentFrame().storeVariable(i+1, argValues[i]);
                    }

                    //Go to the method bytecode start
                    instructions.goTo(((InterpretedMethod) method).getInstructionPosition());

                } catch (LookupException e) {
                    throw new InterpreterException("Trying to call non-existent method '" + methodDescriptor +"'");
                }


    }

    public void invokeNative(String methodDescriptor) throws InterpreterException {
        if (!natives.nativeExist(methodDescriptor)){
            throw new InterpreterException("Trying to call non-existent method '" + methodDescriptor +"'");
        }

        //Pop obj reference from stack (we don't need it but need to pop it)
        StackValue objectRef = stack.currentFrame().pop();


        //Load approximate method from descriptor so we can count the arguments
        Method methodFromDescriptor = new Method(methodDescriptor);


        int numberOfArgs = methodFromDescriptor.getArgs().size();

        NativeValue[] argValues = new NativeValue[numberOfArgs];

        for (int i = 0; i<methodFromDescriptor.getArgs().size(); i++){
            Type type = methodFromDescriptor.getArgs().get(i);

           if (type instanceof ArrayType){

                StackValue ref = stack.currentFrame().pop();
                argValues[i] = new NativeValue(heap.loadArray(ref).getBytes());

           }else if (type instanceof NumberType || type instanceof CharType || type instanceof BooleanType) {

               int value = stack.currentFrame().pop().intValue();
               argValues[i] = new NativeValue(value);

            }else{
                throw new InterpreterException("Passing " + type + " in native functions is not supported");
            }

        }

        NativeValue returnValue = natives.invoke(methodDescriptor, argValues);

        if (returnValue != null){
            //TODO: Fix
            stack.currentFrame().pushBytes(new StackValue(returnValue.getBytes()));
        }
    }

    public void executeReturnInstruction(Instruction instruction, Stack stack) throws InterpreterException{

        int returnAddress = stack.currentFrame().getReturnAddress();

        switch (instruction.getInstruction()) {
            case ReturnVoid:
                stack.deleteCurrentFrame();
            break;
            case ReturnInteger: {
                StackValue var = stack.currentFrame().pop();

                //Remove current frame
                stack.deleteCurrentFrame();

                //Push return value on the calling frame
                stack.currentFrame().push(var);
            }
            break;
            case ReturnReference: {
                //TODO: change to reference later

                StackValue var = stack.currentFrame().pop();
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
                StackValue value = new StackValue(instruction.getOperand(0), StackValue.Type.Primitive);
                stack.currentFrame().push(value);
            }
            break;
            case StoreInteger: {
                StackValue var = stack.currentFrame().pop();
                stack.currentFrame().storeVariable(instruction.getOperand(0), var);
            }
            break;
            case LoadInteger: {
                StackValue var = stack.currentFrame().loadVariable(instruction.getOperand(0));
                stack.currentFrame().push(var);
            }
            break;

            case StoreReference: {

                StackValue var = stack.currentFrame().pop();

                //Convert int to reference
                StackValue reference = new StackValue(var.intValue(), StackValue.Type.Pointer);
                stack.currentFrame().storeVariable(instruction.getOperand(0), reference);
            }
            break;
            case LoadReference: {
                StackValue var = stack.currentFrame().loadVariable(instruction.getOperand(0));
                stack.currentFrame().push(var);
            }
            break;

        }
    }

    public void executeArithmeticInstruction(Instruction instruction, Stack stack) throws InterpreterException{
        int b = stack.currentFrame().pop().intValue();
        int a = stack.currentFrame().pop().intValue();
        int result = 0;

        switch (instruction.getInstruction()) {

            case AddInteger:
               result = a + b;
               break;
            case SubstractInteger:
                result = a - b;
               break;
            case MultiplyInteger:
                result = a * b;
               break;
            case DivideInteger:

                if (b == 0){
                    throw new InterpreterException("Division by zero");
                }
                result = a / b;
               break;
            case ModuloInteger:

                result = a % b;
               break;
        }
        StackValue value = new StackValue(result, StackValue.Type.Primitive);
        stack.currentFrame().push(value);
    }

    public void executeCompareInstruction(Instruction instruction, Stack stack) throws InterpreterException {
        int b = stack.currentFrame().pop().intValue();
        int a = stack.currentFrame().pop().intValue();

        int operand = instruction.getOperand(0);

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

        int jumpTo = instruction.getOperand(0);
        instructions.goTo(jumpTo);
    }




}

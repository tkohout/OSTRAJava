package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.GenerationCollector;
import cz.cvut.fit.ostrajava.Interpreter.Memory.*;
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
        this.natives = new Natives(this.heap, classPool);
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
            case PushFloat:
            case StoreInteger:
            case LoadInteger:
            case StoreReference:
            case LoadReference:
            case LoadFloat:
            case StoreFloat:
                executeStackInstruction(instruction, stack);
                break;
            case AddInteger:
            case SubtractInteger:
            case MultiplyInteger:
            case DivideInteger:
            case ModuloInteger:
                executeIntegerArithmeticInstruction(instruction, stack);
                break;
            case AddFloat:
            case SubtractFloat:
            case MultiplyFloat:
            case DivideFloat:
            case ModuloFloat:
                executeFloatArithmeticInstruction(instruction, stack);
                break;
            case IfCompareEqualInteger:
            case IfCompareNotEqualInteger:
            case IfCompareLessThanInteger:
            case IfCompareLessThanOrEqualInteger:
            case IfCompareGreaterThanInteger:
            case IfCompareGreaterThanOrEqualInteger:
                executeIntegerCompareInstruction(instruction, stack);
                break;
            case IfEqualZero:
            case IfNotEqualZero:
            case IfLessThanZero:
            case IfLessOrEqualThanZero:
            case IfGreaterThanZero:
            case IfGreaterOrEqualThanZero:
                executeZeroCompareInstruction(instruction, stack);
                break;
            case FloatCompare:
                executeFloatCompareInstruction(instruction, stack);
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
            case StoreReferenceArray:
            case LoadReferenceArray:
                executeArrayInstruction(instruction, stack);
                break;
            case FloatToInteger:
            case IntegerToFloat:
                executeConvertInstruction(instruction, stack);
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
            case StoreIntegerArray:
            case StoreReferenceArray: {
                StackValue value = stack.currentFrame().pop();;
                int index = stack.currentFrame().pop().intValue();;
                StackValue arrayRef = stack.currentFrame().pop();

                if (instruction.getInstruction() == InstructionSet.StoreReferenceArray) {
                    heap.addDirtyLink(arrayRef, value);
                }

                Array array = heap.loadArray(arrayRef);
                array.set(index, value);
            }
                break;
            case LoadIntegerArray:
            case LoadReferenceArray:{
                int index = stack.currentFrame().pop().intValue();;
                StackValue arrayRef = stack.currentFrame().pop();;

                Array array = heap.loadArray(arrayRef);
                StackValue value = array.get(index);
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

        Array charArray = heap.loadArray(reference);

        for (int i = 0; i < constant.length(); i++){
            StackValue charValue = new StackValue(constant.charAt(i), StackValue.Type.Primitive);
            charArray.set(i, charValue);
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

    public void executeInvokeInstruction(Instruction instruction, Stack stack) throws InterpreterException, HeapOverflow {


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

    public void invokeNative(String methodDescriptor) throws InterpreterException, HeapOverflow {
        if (!natives.nativeExist(methodDescriptor)){
            throw new InterpreterException("Trying to call non-existent method '" + methodDescriptor +"'");
        }

        //Pop obj reference from stack (we don't need it but need to pop it)
        StackValue objectRef = stack.currentFrame().pop();


        //Load approximate method from descriptor so we can count the arguments
        Method methodFromDescriptor = new Method(methodDescriptor);


        int numberOfArgs = methodFromDescriptor.getArgs().size();

        StackValue[] argValues = new StackValue[numberOfArgs];

        for (int i = 0; i<methodFromDescriptor.getArgs().size(); i++){
            Type type = methodFromDescriptor.getArgs().get(i);

            if (type instanceof ArrayType || type == Types.Number() || type == Types.Char() || type == Types.Boolean() || type == Types.Float()){

                StackValue value = stack.currentFrame().pop();
                argValues[i] = value;

           }else{
                throw new InterpreterException("Passing " + type + " in native functions is not supported");
            }

        }

        StackValue returnValue = natives.invoke(methodDescriptor, argValues);

        if (returnValue != null){
            stack.currentFrame().push(returnValue);
        }
    }

    public void executeReturnInstruction(Instruction instruction, Stack stack) throws InterpreterException{

        int returnAddress = stack.currentFrame().getReturnAddress();

        switch (instruction.getInstruction()) {
            case ReturnVoid:
                stack.deleteCurrentFrame();
            break;
            case ReturnInteger:
            case ReturnReference: {
                StackValue var = stack.currentFrame().pop();

                //Remove current frame
                stack.deleteCurrentFrame();

                //Push return value on the calling frame
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
            case PushFloat: {
                int constantIndex = instruction.getOperand(0);
                String floatString = constantPool.getConstant(constantIndex);
                StackValue value = new StackValue(floatString);
                stack.currentFrame().push(value);
            }
            break;
            case StoreInteger:
            case StoreFloat:{
                StackValue var = stack.currentFrame().pop();
                stack.currentFrame().storeVariable(instruction.getOperand(0), var);
            }
            break;
            case LoadInteger:
            case LoadFloat:
            case LoadReference:
            {
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


        }
    }

    public void executeIntegerArithmeticInstruction(Instruction instruction, Stack stack) throws InterpreterException{
        int b = stack.currentFrame().pop().intValue();
        int a = stack.currentFrame().pop().intValue();
        int result = 0;

        switch (instruction.getInstruction()) {

            case AddInteger:
               result = a + b;
               break;
            case SubtractInteger:
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

    public void executeFloatArithmeticInstruction(Instruction instruction, Stack stack) throws InterpreterException{
        float b = stack.currentFrame().pop().floatValue();
        float a = stack.currentFrame().pop().floatValue();
        float result = 0;

        switch (instruction.getInstruction()) {

            case AddFloat:
                result = a + b;
                break;
            case SubtractFloat:
                result = a - b;
                break;
            case MultiplyFloat:
                result = a * b;
                break;
            case DivideFloat:

                if (b == 0){
                    throw new InterpreterException("Division by zero");
                }
                result = a / b;
                break;
            case ModuloFloat:

                result = a % b;
                break;
        }
        StackValue value = new StackValue(result);
        stack.currentFrame().push(value);
    }

    public void executeIntegerCompareInstruction(Instruction instruction, Stack stack) throws InterpreterException {
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

    public void executeZeroCompareInstruction(Instruction instruction, Stack stack) throws InterpreterException {
        int a = stack.currentFrame().pop().intValue();

        int operand = instruction.getOperand(0);

        boolean condition = false;

        switch (instruction.getInstruction()) {
            case IfEqualZero:
                condition = (a == 0);
                break;
            case IfNotEqualZero:
                condition = (a != 0);
                break;
            case IfLessThanZero:
                condition = (a < 0);
                break;
            case IfLessOrEqualThanZero:
                condition = (a <= 0);
                break;
            case IfGreaterThanZero:
                condition = (a > 0);
                break;
            case IfGreaterOrEqualThanZero:
                condition = (a >= 0);
                break;
        }

        if (condition){
            instructions.goTo(operand);
        }
    }

    public void executeFloatCompareInstruction(Instruction instruction, Stack stack) throws InterpreterException {
        float b = stack.currentFrame().pop().floatValue();
        float a = stack.currentFrame().pop().floatValue();

        int result;
        if (a < b){
            result = -1;
        }else if (a > b){
            result = 1;
        }else{
            result = 0;
        }

        StackValue value = new StackValue(result, StackValue.Type.Primitive);
        stack.currentFrame().push(value);
    }

    public void executeConvertInstruction(Instruction instruction, Stack stack) throws InterpreterException {
        StackValue value = stack.currentFrame().pop();
        StackValue convertedValue = null;

        switch (instruction.getInstruction()) {
            case FloatToInteger:
                float floatValue = value.floatValue();
                convertedValue = new StackValue(Converter.floatToInt(floatValue), StackValue.Type.Primitive);

                break;
            case IntegerToFloat:
                int intValue = value.intValue();
                convertedValue = new StackValue(Converter.intToFloat(intValue));
                break;
        }

        stack.currentFrame().push(convertedValue);
    }

    public void executeGoToInstruction(Instruction instruction, Stack stack) throws InterpreterException {

        int jumpTo = instruction.getOperand(0);
        instructions.goTo(jumpTo);
    }




}

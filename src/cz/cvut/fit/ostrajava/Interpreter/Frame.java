package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.Method;

import java.nio.ByteBuffer;

/**
 * Created by tomaskohout on 11/17/15.
 */
public class Frame {
    final int RETURN_ADDRESS_SIZE = 4;
    final int LOCAL_VAR_BYTE_SIZE = 4;
    final int INT_SIZE = 4;

    protected ByteBuffer byteArray;
    protected int maxSize;
    protected int count;
    protected int localVariablesCount = 0;

    protected String methodName;


    public Frame(int size, int returnAddress, int thisReference, Method method) throws InterpreterException {
        byteArray = ByteBuffer.allocate(size);
        maxSize = size;
        count = 0;

        //Save return address
        push(returnAddress);

        //Push this as a first variable
        push(thisReference);

        //Arguments and this ref counted in locals
        localVariablesCount = method.getLocalVariablesCount();

        //For stack trace
        methodName = method.getName();

        count = getStackOffset();

    }


    public void push(int i) throws InterpreterException {
        if (count + INT_SIZE >= maxSize){
            throw new InterpreterException("Stack overflow");
        }
        setBytes(count, i);
        count += INT_SIZE;
    }

    public int pop() throws InterpreterException {
        if (count - INT_SIZE < getStackOffset()){
            throw new InterpreterException("Out of bounds");
        }
        count -= INT_SIZE;
        return getBytes(count);
    }

    public void setBytes(int from, int i){
        byteArray.putInt(from, i);
    }

    public int getBytes(int from){
        int index = from;
        int i = byteArray.getInt(index);
        return i;
    }

    public void storeVariable(int index, int value) throws InterpreterException {
        if (index > localVariablesCount - 1 ){
            throw new InterpreterException("Trying to store to non-existent variable");
        }
        setBytes(getVariablePosition(index), value);
    }

    public int loadVariable(int index) throws InterpreterException {
        if (index > localVariablesCount - 1 ){
            throw new InterpreterException("Trying to load non-existent variable");
        }
        return getBytes(getVariablePosition(index));
    }

    protected int getVariablePosition(int index){
        return RETURN_ADDRESS_SIZE + index * LOCAL_VAR_BYTE_SIZE;
    }

    protected int getReturnAddress(){
        return getBytes(0);
    }

    protected int getStackOffset() {
        return RETURN_ADDRESS_SIZE + localVariablesCount * LOCAL_VAR_BYTE_SIZE;
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getReturnAddress() + "\n");
        sb.append("-----------\n");

        //Show local variables
        for (int i=0; i < localVariablesCount; i++){
            int start = getVariablePosition(i);

            //TODO: right now just INT
            int var = getBytes(start);

            sb.append("Var " + i + ": " + var + "\n");
        }

        sb.append("-----------\n");

        //Show values on stack
        for (int j=getStackOffset(); j < count; j++){
            sb.append(byteArray.get(j)+ " ");
        }

        return sb.toString();
    }
}

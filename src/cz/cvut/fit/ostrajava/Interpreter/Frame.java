package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.Method;
import cz.cvut.fit.ostrajava.Type.NumberType;

import java.nio.ByteBuffer;

/**
 * Created by tomaskohout on 11/17/15.
 */
public class Frame {
    final int RETURN_ADDRESS_SIZE = NumberType.size;
    final int LOCAL_VAR_BYTE_SIZE = 4;

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
        overflowCheck(NumberType.size);
        setInt(count, i);
        count += NumberType.size;
    }

    public int pop() throws InterpreterException {
        underflowCheck(NumberType.size);
        count -= NumberType.size;
        return getInt(count);
    }

    public byte[] popBytes(int size) throws InterpreterException {
        underflowCheck(size);
        count -= size;
        byte[] bytes = new byte[size];

        for (int i = 0; i< size; i++){
            bytes[i] = byteArray.get(i+count);
        }

        return bytes;
    }

    public void pushBytes(byte[] bytes) throws InterpreterException {
        overflowCheck(bytes.length);
        byteArray.put(bytes, count, bytes.length);
        count += bytes.length;
    }

    public void setInt(int from, int i){
        byteArray.putInt(from, i);
    }

    public int getInt(int from){
        int index = from;
        int i = byteArray.getInt(index);
        return i;
    }

    public void storeVariable(int index, int value) throws InterpreterException {
        if (index > localVariablesCount - 1 ){
            throw new InterpreterException("Trying to store to non-existent variable");
        }
        setInt(getVariablePosition(index), value);
    }

    public int loadVariable(int index) throws InterpreterException {
        if (index > localVariablesCount - 1 ){
            throw new InterpreterException("Trying to load non-existent variable");
        }
        return getInt(getVariablePosition(index));
    }

    protected int getVariablePosition(int index){
        return RETURN_ADDRESS_SIZE + index * LOCAL_VAR_BYTE_SIZE;
    }

    protected int getReturnAddress(){
        return getInt(0);
    }

    protected int getStackOffset() {
        return RETURN_ADDRESS_SIZE + localVariablesCount * LOCAL_VAR_BYTE_SIZE;
    }

    public String getMethodName() {
        return methodName;
    }

    private void underflowCheck(int size){
        if (count - size < getStackOffset()){
            throw new IndexOutOfBoundsException();
        }
    }

    private void overflowCheck(int size){
        if (count + size >= maxSize){
            throw new StackOverflowError();
        }
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
            int var = getInt(start);

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

package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.Method;

import java.nio.ByteBuffer;

/**
 * Created by tomaskohout on 11/17/15.
 */
public class Frame {

    //TODO: All types have same size?
    final int LOCAL_VAR_BYTE_SIZE = 4;
    final int INT_SIZE = 4;



    protected byte[] byteArray;
    protected int pointer;
    protected int localVariablesCount = 0;


    public void push(int i){
        byte[] bytes = intToByteArray(i);
        pushBytes(bytes);
    }

    public int pop(){
        byte[] bytes = popBytes(INT_SIZE);
        int res = intFromByteArray(bytes);
        return res;
    }

    public void storeVariable(int index, int value) throws InterpreterException {
        if (index > localVariablesCount - 1 ){
            throw new InterpreterException("Trying to store to non-existent variable");
        }
        setBytes(getVariablePosition(index), INT_SIZE, intToByteArray(value));
    }

    public int loadVariable(int index) throws InterpreterException {
        if (index > localVariablesCount - 1 ){
            throw new InterpreterException("Trying to load non-existent variable");
        }
        return intFromByteArray(getBytes(getVariablePosition(index), INT_SIZE));
    }


    protected void pushBytes(byte[] bytes){

        setBytes(pointer, bytes.length, bytes);
        //TODO: Overflow check
        pointer += bytes.length;
    }

    protected byte[] getBytes(int from, int size){
        byte[] bytes = new byte[size];
        for (int i = 0; i<size; i++){
            bytes[i] = byteArray[from + i];
        }
        return bytes;
    }

    protected void setBytes(int from, int size, byte[] bytes){
        for (int i = 0; i<size; i++){
            byteArray[from + i]= bytes[i];
        }
    }

    protected byte[] popBytes(int size){
        byte[] bytes = getBytes(pointer-size, size);
        //TODO: Negative check
        pointer-=size;
        return bytes;
    }

    protected int getVariablePosition(int index){
        return index * LOCAL_VAR_BYTE_SIZE;
    }

    protected byte[] intToByteArray(int i){
        return ByteBuffer.allocate(INT_SIZE).putInt(i).array();
    }

    protected int intFromByteArray(byte[] bytes){
        return ByteBuffer.allocate(INT_SIZE).put(bytes).getInt(0);
    }



    public Frame(int size, int returnAddress, int thisReference, Method method){
        byteArray = new byte[size];

        pointer = 0;

        //Save return address
        //push(returnAddress);

        push(thisReference);

        localVariablesCount = method.getLocalVariablesCount();

        //Arguments are also counted there
        pointer = localVariablesCount * LOCAL_VAR_BYTE_SIZE;

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        //Show local variables
        for (int i=0; i < localVariablesCount; i++){
            int start = i*LOCAL_VAR_BYTE_SIZE;

            byte[] bytes = getBytes(start, LOCAL_VAR_BYTE_SIZE);

            //TODO: right now just INT
            int var = intFromByteArray(bytes);

            sb.append("Var " + i + ": " + var + "\n");
        }

        sb.append("-----------\n");

        //Show values on stack
        for (int j=localVariablesCount*LOCAL_VAR_BYTE_SIZE; j < pointer; j++){
            sb.append(byteArray[j] + " ");
        }

        return sb.toString();
    }
}

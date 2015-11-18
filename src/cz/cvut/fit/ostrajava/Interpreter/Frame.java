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


    public void push(int i){
        byte[] bytes = intToByteArray(i);
        pushBytes(bytes);
    }

    public int pop(){
        byte[] bytes = popBytes(INT_SIZE);
        int res = intFromByteArray(bytes);
        return res;
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

    protected void storeVariable(int index, int value){
        setBytes(getVariablePosition(index), INT_SIZE, intToByteArray(value));
    }

    protected int loadVariable(int index){
        return intFromByteArray(getBytes(getVariablePosition(index), INT_SIZE));
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
        push(returnAddress);

        //Arguments are also counted there
        pointer = method.getLocalVariablesCount() * LOCAL_VAR_BYTE_SIZE;

    }
}

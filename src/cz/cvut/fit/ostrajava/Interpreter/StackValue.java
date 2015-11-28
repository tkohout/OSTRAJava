package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Type.FloatType;
import cz.cvut.fit.ostrajava.Type.NumberType;
import cz.cvut.fit.ostrajava.Type.Type;

import java.nio.ByteBuffer;

/**
 * Created by tomaskohout on 11/28/15.
 */
public class StackValue extends ByteArrayWrapper {
    public enum Type{
        Primitive, Pointer
    }

    final int POINTER_LAST_BIT = 1;
    public static final int size = 4;



    public StackValue(byte[] bytes){
        this.byteArray = bytes;
    }

    public StackValue(int number, Type type){
        this(Converter.intToByteArray(number));
        setType(type);
    }

    public StackValue(float number){
        this(Converter.floatToByteArray(number));
        //Only int can be pointer
        setType(Type.Primitive);
    }


    //Shifts bytes to the left and adds flag if it's a pointer
    private void setType(Type type){
        if (byteArray.length != size){
            throw new IllegalArgumentException("Stack value has to be of size " + size);
        }

        int i = Converter.byteArrayToInt(byteArray);
        int lastBit = (type == Type.Pointer) ? POINTER_LAST_BIT : 0;

        int pointer = i << 1 | lastBit;
        this.byteArray = Converter.intToByteArray(pointer);
    }

    //Shifts bytes back
    private byte[] getValue(){
        int i = Converter.byteArrayToInt(byteArray);
        int value = i >> 1;
        return Converter.intToByteArray(value);
    }

    public boolean isPointer() {
        return (byteArray[byteArray.length - 1] & 0x01) == POINTER_LAST_BIT;
    }

    public boolean isNullPointer() {
        return intValue() == 0;
    }

    public byte[] getBytes(){
        return this.byteArray;
    }

    public char charValue(){
        return (char) intValue();
    }

    public int intValue(){
        return Converter.byteArrayToInt(getValue(), 0);
    }

    public float floatValue(){
        return Converter.byteArrayToFloat(getValue(), 0);
    }

    public boolean boolValue(){
        return intValue() == 0 ? false : true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isPointer()){
            sb.append("&");
        }
        //We don't know what it is, so even if it's a float write int
        sb.append(intValue());
        return sb.toString();
    }

}

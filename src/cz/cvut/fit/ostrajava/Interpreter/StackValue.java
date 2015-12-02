package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Type.FloatType;
import cz.cvut.fit.ostrajava.Type.NumberType;
import cz.cvut.fit.ostrajava.Type.Type;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by tomaskohout on 11/28/15.
 */
public class StackValue extends ByteArrayWrapper {
    public enum Type{
        Primitive, Pointer
    }

    final int POINTER_LAST_BIT = 1;
    final int FLOAT_BIAS = 127;
    final int REDUCED_FLOAT_BIAS = 63;
    final int MANTISA_SIZE = 23;


    public static final int size = 4;



    public StackValue(byte[] bytes){
        this.byteArray = bytes;
    }

    public StackValue(int number, Type type){
         this.byteArray = integerToInnerRepresentation(number, type);
    }


    public StackValue(String floatString){
        this(Converter.stringToFloat(floatString));
    }

    public StackValue(Float floatNumber){
        this.byteArray = floatToInnerRepresentation(floatNumber);
    }

    //Shifts integer by 1 so that the top bit signifies whether it's a pointer
    protected byte[] integerToInnerRepresentation(int i, Type type){
        int lastBit = (type == Type.Pointer) ? POINTER_LAST_BIT : 0;
        int pointer = i << 1 | lastBit;
        return Converter.intToByteArray(pointer);
    }

    //Shifts float by 1, but has to reduce exponent to make space in float
    protected byte[] floatToInnerRepresentation(float f){
        byte[] bytes = Converter.floatToByteArray(f);

        int i = Converter.byteArrayToInt(bytes);

        //last 23 bits
        int mantisa = (i & 0x7FFFFF);

        //8 bits after mantisa
        int exponent = (i & 0x7F800000) >> MANTISA_SIZE;

        //Make it unsigned
        exponent = exponent - FLOAT_BIAS + REDUCED_FLOAT_BIAS;

        //If the exponent is too big, throw exception
        if (exponent >= 128 || exponent < 0){
            throw new IllegalArgumentException("Float overflow");
        }
        exponent <<= 23;

        //Shift by 1
        int sign = (i & 0x80000000) >> 1;
        int result = sign | exponent | mantisa;

        return Converter.intToByteArray(result);
    }



    //Shifts bytes back
    public int innerRepresentationToIntValue(){
        int i = Converter.byteArrayToInt(byteArray);
        int value = i >> 1;
        return value;
    }

    public float innerRepresentationToFloat(byte[] bytes) {

        int i = Converter.byteArrayToInt(bytes);

        //23 bits
        int mantisa = (i & 0x7FFFFF);
        //7 bits after mantisa
        int exponent = (i & 0x3F800000) >> MANTISA_SIZE;
        //Change exponent back to the bias
        exponent = exponent - REDUCED_FLOAT_BIAS + FLOAT_BIAS;
        exponent <<= MANTISA_SIZE;

        int sign = (i & 0x80000000) << 1;

        int result = sign | exponent | mantisa;

        return Converter.byteArrayToFloat(Converter.intToByteArray(result));
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
        return innerRepresentationToIntValue();
    }

    public float floatValue(){
        return innerRepresentationToFloat(this.byteArray);
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

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof StackValue) {
            StackValue value = (StackValue) obj;
            return this.hashCode() == value.hashCode();
        }

        return false;
    }

    public int hashCode() {
        return Converter.byteArrayToInt(this.getBytes());
    }
}

package cz.cvut.fit.ostrajava.Interpreter.Natives;

import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Object;
import cz.cvut.fit.ostrajava.Type.FloatType;
import cz.cvut.fit.ostrajava.Type.NumberType;
import cz.cvut.fit.ostrajava.Type.Type;

import java.nio.ByteBuffer;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class NativeArgument {
    protected byte[] bytes;


    public NativeArgument(byte[] bytes){
        this.bytes = bytes;
    }


    public char charValue(){
        return (char) intValue();
    }

    public int intValue(){
        return byteArrayToInt(bytes, 0);
    }

    public float floatValue(){
        return byteArrayToFloat(bytes, 0);
    }

    public boolean boolValue(){
        return intValue() == 0 ? false : true;
    }

    public char[] charArray(){
        int[] intArray = intArray();
        char[] res = new char[intArray.length];
        for (int i = 0; i<intArray.length; i++){
            res[i] = (char) intArray[i];
        }

        return res;
    }

    public boolean[] boolArray(){
        int[] intArray = intArray();
        boolean[] res = new boolean[intArray.length];
        for (int i = 0; i<intArray.length; i++){
            res[i] =  (intArray[i] == 0) ? false : true;
        }

        return res;
    }

    public int[] intArray(){

        int intArrayLength = bytes.length / NumberType.size;
        int[] res = new int[intArrayLength];
        for (int i = 0; i < intArrayLength; i++){
            res[i] = byteArrayToInt(bytes, i * NumberType.size);
        }

        return res;
    }

    public float[] floatArray(){

        int floatArrayLength = bytes.length / FloatType.size;
        float[] res = new float[floatArrayLength];
        for (int i = 0; i < floatArrayLength; i++){
            res[i] = byteArrayToFloat(bytes, i * FloatType.size);
        }

        return res;
    }

    private float byteArrayToFloat(byte[] bytes, int from){
        return ByteBuffer.wrap(bytes, from, NumberType.size).getFloat();
    }

    private int byteArrayToInt(byte[] bytes, int from){
        return ByteBuffer.wrap(bytes, from, NumberType.size).getInt();

       //return bytes[from] << 24 | (bytes[from + 1] & 0xFF) << 16 | (bytes[from + 2] & 0xFF) << 8 | (bytes[from + 3] & 0xFF);
    }

}



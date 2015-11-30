package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Type.FloatType;
import cz.cvut.fit.ostrajava.Type.NumberType;

import java.nio.ByteBuffer;

/**
 * Created by tomaskohout on 11/28/15.
 */
public class Converter {
    public static float byteArrayToFloat(byte[] bytes, int from){
        return ByteBuffer.wrap(bytes, from, NumberType.size).getFloat();
    }

    public static float byteArrayToFloat(byte[] bytes) {
        return byteArrayToFloat(bytes, 0);
    }

    public static int byteArrayToInt(byte[] bytes) {
        return byteArrayToInt(bytes, 0);
    }

    public static int byteArrayToInt(byte[] bytes, int from){
        return ByteBuffer.wrap(bytes, from, NumberType.size).getInt();
    }

    public static float stringToFloat(String floatString){
        return Float.parseFloat(floatString);
    }

    public static float intToFloat(int i) {
        return (float)i;
    }

    public static int floatToInt(float f) {
        return (int)f;
    }

    public static byte[] intToByteArray(int i) {
        return ByteBuffer.allocate(NumberType.size).putInt(i).array();
    }


    public static byte[] floatToByteArray(float i) {
        return ByteBuffer.allocate(FloatType.size).putFloat(i).array();
    }

    public char[] byteArrayToCharArray(byte[] bytes){
        int[] intArray = byteArrayToIntArray(bytes);
        char[] res = new char[intArray.length];
        for (int i = 0; i<intArray.length; i++){
            res[i] = (char) intArray[i];
        }

        return res;
    }

    public boolean[] byteArrayToBoolArray(byte[] bytes){
        int[] intArray = byteArrayToIntArray(bytes);
        boolean[] res = new boolean[intArray.length];
        for (int i = 0; i<intArray.length; i++){
            res[i] =  (intArray[i] == 0) ? false : true;
        }

        return res;
    }

    public static int[] byteArrayToIntArray(byte[] bytes){

        int intArrayLength = bytes.length / NumberType.size;
        int[] res = new int[intArrayLength];
        for (int i = 0; i < intArrayLength; i++){
            res[i] = byteArrayToInt(bytes, i * NumberType.size);
        }

        return res;
    }

    public float[] byteArrayToFloatArray(byte[] bytes){

        int floatArrayLength = bytes.length / FloatType.size;
        float[] res = new float[floatArrayLength];
        for (int i = 0; i < floatArrayLength; i++){
            res[i] = byteArrayToFloat(bytes, i * FloatType.size);
        }

        return res;
    }
}

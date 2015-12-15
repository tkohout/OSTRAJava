package cz.cvut.fit.ostrajava.Interpreter;

/**
 * Created by tomaskohout on 11/28/15.
 */
public abstract class ByteArrayWrapper {
    protected byte[] byteArray;

    public void setBytes(int from, byte[] bytes){
        for (int i = 0; i< bytes.length; i++){
            byteArray[i+from] = bytes[i];
        }
    }

    public byte[] getBytes(int from){
        byte[] bytes = new byte[StackValue.size];

        for (int i = 0; i< StackValue.size; i++){
            bytes[i] = byteArray[i+from];
        }

        return bytes;
    }
}

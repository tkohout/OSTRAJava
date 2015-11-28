package cz.cvut.fit.ostrajava.Interpreter;

import java.nio.ByteBuffer;

/**
 * Created by tomaskohout on 11/21/15.
 */
public class Array extends HeapObject {

    final int ITEM_SIZE = 4;
    final int HEADER_SIZE = GC_STATE_SIZE;
    int capacity;



    public Array(int size) {
        this.capacity = size;
        byteArray = new byte[HEADER_SIZE + size * ITEM_SIZE];
    }

    public int get(int index){
        if (index > capacity - 1 ){
            throw new IndexOutOfBoundsException();
        }
        return Converter.byteArrayToInt(getBytes(HEADER_SIZE + index * ITEM_SIZE));
    }

    public void set(int index, int value){
        if (index > capacity - 1 ){
            throw new IndexOutOfBoundsException();
        }

        setBytes(HEADER_SIZE + index * ITEM_SIZE, Converter.intToByteArray(value));
    }

    public int getSize(){
        return this.capacity;
    }

    public byte[] getBytes(){
        byte[] bytes = new byte[getSize()*ITEM_SIZE];
        for (int i = 0; i<getSize()*ITEM_SIZE; i++){
            bytes[i] = byteArray[HEADER_SIZE+i];
        }

        return bytes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString() + "\n");

        for (int i = 0; i<capacity; i++){
            sb.append(get(i) + " ");
        }

        return sb.toString();
    }
}

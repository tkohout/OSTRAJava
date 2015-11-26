package cz.cvut.fit.ostrajava.Interpreter;

import java.nio.ByteBuffer;

/**
 * Created by tomaskohout on 11/21/15.
 */
public class Array extends HeapObject {
    ByteBuffer byteArray;
    final int ITEM_SIZE = 4;

    int capacity;

    public Array(int size) {
        this.capacity = size;
        byteArray = ByteBuffer.allocate(size * ITEM_SIZE);
    }

    public int get(int index){
        if (index > capacity - 1 ){
            throw new IndexOutOfBoundsException();
        }
        return byteArray.getInt( index * ITEM_SIZE);
    }

    public void set(int index, int value){
        if (index > capacity - 1 ){
            throw new IndexOutOfBoundsException();
        }

        byteArray.putInt(index * ITEM_SIZE, value);
    }

    public int getSize(){
        return this.capacity;
    }

    public byte[] getBytes(){
        return byteArray.array();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<capacity; i++){
            sb.append(get(i) + " ");
        }

        return sb.toString();
    }
}

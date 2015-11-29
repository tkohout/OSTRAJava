package cz.cvut.fit.ostrajava.Interpreter.Memory;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Interpreter.*;
import cz.cvut.fit.ostrajava.Interpreter.Memory.GarbageCollector.State;

import java.util.Set;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class Object extends HeapItem {

    final int OBJECT_FIELD_SIZE = 4;
    final int OBJECT_CLASS_ADDRESS_SIZE = 4;
    final int OBJECT_SIZE_TYPE = 4;

    final int OBJECT_HEADER_SIZE = GC_STATE_SIZE + OBJECT_CLASS_ADDRESS_SIZE + OBJECT_SIZE_TYPE;

    public Object(InterpretedClass objectClass) {


        Set<Field> fieldList = objectClass.getAllFields();
        int numberOfFields = fieldList.size();

        int size = OBJECT_HEADER_SIZE + OBJECT_FIELD_SIZE * numberOfFields;

        byteArray = new byte[size];

        setBytes(GC_STATE_SIZE, Converter.intToByteArray(objectClass.getClassPoolAddress()));

        this.setGCState(State.Dead);
    }

    protected int getClassAddress(){
        return Converter.byteArrayToInt(getBytes(GC_STATE_SIZE));
    }

    protected int getSize(){
        return Converter.byteArrayToInt(getBytes(GC_STATE_SIZE + OBJECT_CLASS_ADDRESS_SIZE));
    }

    protected int getFieldsSize(){
        return getSize() - OBJECT_HEADER_SIZE;
    }
    public int getFieldsNumber(){
        return getFieldsSize() / OBJECT_FIELD_SIZE;
    }


    public StackValue getField(int index){
        return new StackValue(getBytes(OBJECT_HEADER_SIZE + index * OBJECT_FIELD_SIZE));
    }

    public void setField(int index, StackValue value){
        setBytes(OBJECT_HEADER_SIZE + index * OBJECT_FIELD_SIZE, value.getBytes());
    }

    public InterpretedClass loadClass(ClassPool pool) throws InterpreterException {
        return pool.getClass(getClassAddress());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(super.toString() + "\n");
        sb.append("Class: " + getClassAddress() + "\n");
        sb.append("Size: " + getSize() + "\n");

        for (int i = 0; i<getFieldsNumber(); i++){
            sb.append(getField(i) + " ");
        }

        return sb.toString();
    }
}

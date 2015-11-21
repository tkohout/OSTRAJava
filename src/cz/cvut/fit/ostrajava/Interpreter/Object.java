package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class Object {

    final int OBJECT_FIELD_SIZE = 4;
    final int OBJECT_CLASS_ADDRESS_SIZE = 4;
    final int OBJECT_SIZE_TYPE = 4;

    final int OBJECT_HEADER_SIZE = OBJECT_CLASS_ADDRESS_SIZE + OBJECT_SIZE_TYPE;

    ByteBuffer byteArray;

    public Object(InterpretedClass objectClass) {

        Set<Field> fieldList = objectClass.getAllFields();
        int numberOfFields = fieldList.size();

        int size = OBJECT_HEADER_SIZE + OBJECT_FIELD_SIZE * numberOfFields;

        byteArray = ByteBuffer.allocate(size);
        byteArray.putInt(objectClass.getClassPoolAddress());

    }

    protected int getClassAddress(){
        return byteArray.getInt(0);
    }

    protected int getSize(){
        return byteArray.getInt(OBJECT_CLASS_ADDRESS_SIZE);
    }

    protected int getFieldsSize(){
        return getSize() - OBJECT_HEADER_SIZE;
    }

    //TODO: What if not int?
    protected int getField(int index){
        return byteArray.getInt(OBJECT_HEADER_SIZE + index * OBJECT_FIELD_SIZE);
    }

    protected void setField(int index, int value){
         byteArray.putInt(OBJECT_HEADER_SIZE + index * OBJECT_FIELD_SIZE, value);
    }

    public InterpretedClass loadClass(ClassPool pool) throws InterpreterException {
        return pool.getClass(getClassAddress());
    }
}

package cz.cvut.fit.ostrajava.Type;

public class ArrayType extends Type{

    Type element;

    ArrayType(Type type){
        element = type;
    }

    public Type getElement() {
        return element;
    }

    public java.lang.String toString() {
        return element + "[]";
    }
}

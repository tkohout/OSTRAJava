package cz.cvut.fit.ostrajava.Type;

public class Array extends Type{

    Type element;

    Array(Type type){
        element = type;
    }

    public Type getElement() {
        return element;
    }

    public java.lang.String toString() {
        return element + "[]";
    }
}

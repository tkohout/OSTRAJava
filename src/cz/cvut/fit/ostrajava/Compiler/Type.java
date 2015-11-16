package cz.cvut.fit.ostrajava.Compiler;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class Type {

    private static Type booleanSingleton;
    private static Type numberSingleton;
    private static Type floatSingleton;
    private static Type stringSingleton;
    private static Type voidSingleton;


    private enum Types {
        Boolean, Number, Float, String, Void, Reference;
    }

    private String className;
    private Types type;

    private Type(Types type) {
        this.type = type;
        this.className = "";
    }

    private Type(Types type, String className) {
        this.type = type;
        this.className = className;
    }


    public static Type Boolean(){
        if (booleanSingleton == null) {
            booleanSingleton = new Type(Types.Boolean);
        }

        return booleanSingleton;
    }

    public static Type Number(){
        if (numberSingleton == null) {
            numberSingleton = new Type(Types.Number);
        }

        return numberSingleton;
    }

    public static Type Float(){
        if (floatSingleton == null) {
            floatSingleton = new Type(Types.Float);
        }

        return floatSingleton;
    }

    public static Type String(){
        if (stringSingleton == null) {
            stringSingleton = new Type(Types.String);
        }

        return stringSingleton;
    }

    public static Type Void(){
        if (voidSingleton == null) {
            voidSingleton = new Type(Types.Void);
        }

        return voidSingleton;
    }

    public static Type Reference(String className){
        Type ref = new Type(Types.Reference, className);
        return ref;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isReference(){
        return type == Types.Reference;
    }

    @Override
    public String toString() {
        if (isReference()){
            return this.className;
        }else {
            return this.type.toString();
        }
    }
}

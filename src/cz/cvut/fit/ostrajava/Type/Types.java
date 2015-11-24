package cz.cvut.fit.ostrajava.Type;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by tomaskohout on 11/12/15.
 */

//TODO: Refactor this mess
public class Types {


    static Map<java.lang.String, Type> singletons;

    private static void addSingleton(Type type){
        singletons.put(type.toString(), type);
    }

    public static void init(){
        singletons = new HashMap<>();

        addSingleton(new Boolean());
        addSingleton(new Char());
        addSingleton(new Float());
        addSingleton(new Number());
        addSingleton(new String());
        addSingleton(new Void());
        addSingleton(new Array(Boolean()));
        addSingleton(new Array(Number()));
        addSingleton(new Array(Char()));
        addSingleton(new Array(Float()));
    }

    public static Type getSingleton(java.lang.String name){
        if (singletons == null){
            init();
        }

        Type type = singletons.get(name);
        return type;
    }

    public static Boolean Boolean(){
        return (Boolean)getSingleton(Boolean.name);
    }

    public static Number Number(){
        return (Number) getSingleton(Number.name);
    }

    public static Char Char(){
        return (Char) getSingleton(Char.name);
    }

    public static Float Float(){
        return (Float) getSingleton(Float.name);
    }

    public static String String(){
        return (String) getSingleton(String.name);
    }

    public static Void Void(){
        return (Void) getSingleton(Void.name);
    }

    public static Array NumberArray(){
        return Array(Number());
    }

    public static Array FloatArray(){
        return Array(Float());
    }

    public static Array CharArray(){
        return Array(Char());
    }

    public static Array BooleanArray(){
        return Array(Boolean());
    }

    public static Array Array(Type type){
        return (Array)getSingleton(type.toString() + "[]");
    }

    public static Type Reference(java.lang.String className){
        Type type = getSingleton(className);
        if (type == null){
            type = new Reference(className);
            addSingleton(type);
        }
        return type;
    }

    public static Type fromString(java.lang.String name){
        Type type = getSingleton(name);
        if (type == null){
            return Reference(name);
        }

        return type;
    }
}

import java.lang.Object;
import java.lang.System;

class Z{
    void bar(){
        System.out.println("Z:bar()");
    }
}

class A extends Z{

    void foo(){
        bar();
        super.bar();
    }
    void bar(){
        System.out.println("A:bar()");
    }

}

class B extends A{

    void foo(){
        super.foo();

    }
    void bar(){
        System.out.println("B:bar()");
    }

}


class Test{

    public static void main(String[] args){
        A a = new A();
        a = new B();

        a.foo();
    }

}
import java.lang.Object;
import java.lang.System;

class A{
    int i;

    A(){

    }

    A(int i){
        this.i = i;
    }

    void foo(X x, Z z){
        System.out.println("A:foo()");
    }

    void boo(){
        System.out.println("A:boo()");
    }
}

class B extends A{

    B(){
        this(5);
    }

    B(int i){
        super(i);
    }

    void foo(X x, Z z){
        System.out.println("B:foo()");
        super.foo(x, z);
        this.goo();

        super.boo();
        this.boo();
    }

    static void goo(){
        System.out.println("B:goo()");
    }
}

class X{

}

class Y{

}

class Z extends Y{

}

class Test{

    public static void main(String[] args){
        A a = new A();
        B b = new B();
        X x = new X();
        Z z = new Z();

        a.foo(x, z);
        b.foo(x, z);
    }

    public static void floatTest(){
        float f = 1;
        float g = (float)2.2;

        int i=1, j=2;

        float res = i*g;

        char b='a', d='c';
        if (b>d){
            System.out.println("Tada");
        }

        if (f == g){
            System.out.println("Tada");
        }

        if (f > g){
            System.out.println("Tada");
        }

        if (i > j){
            System.out.println("Tada");
        }
    }
}
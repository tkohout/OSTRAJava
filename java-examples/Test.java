import java.lang.Object;

class A{
    void foo(X x, Z z){
        System.out.println("A:foo()");
    }
}

class B extends A{
    void foo(X x, Y y){
        System.out.println("B:foo()");
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
}
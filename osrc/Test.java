import java.lang.Object;

class A{
    void foo(){
        System.out.println("A:foo()");
    }
}

class B extends A{
    void foo(){
        System.out.println("B:foo()");
    }
}

class Test{

    public static void main(String[] args){
        A a = new A();
        B b = new B();

        a.foo();

        a = b;

        a.foo();
    }
}
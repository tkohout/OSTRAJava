banik pyco

tryda Ostrava {

     rynek(){
        toz A a pyco
        toz B b pyco
        toz C c pyco

        a = zrob A() pyco
        b = zrob B() pyco
        c = zrob C() pyco

        a.foo() pyco
        b.foo() pyco
        c.foo() pyco

        a.bar() pyco
        b.bar() pyco
        c.bar() pyco

    }

}

tryda A{
    cyslo foo(){
        davaj 1 pyco
    }

    cyslo bar(){
        davaj 3 pyco
    }
}

tryda B fagan od A {
    cyslo foo(){
        davaj 2 pyco
    }
}

tryda C fagan od B {
    cyslo bar(){
        davaj 4 pyco
    }
}


fajront pyco
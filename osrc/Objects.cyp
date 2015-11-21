banik pyco

tryda Ostrava {

     rynek(){
        toz A a pyco
        toz B b pyco
        toz C c pyco

        a = zrob A() pyco
        b = zrob B() pyco
        c = zrob C() pyco

        a.b = b pyco
        a.b.neco = 5 pyco
        a.b.neco() pyco

        c.b = b pyco

        dechrobok pyco

        a.foo() pyco
        b.foo() pyco
        c.foo() pyco

        a.bar() pyco
        b.bar() pyco
        c.bar() pyco

    }

}

tryda A{
    toz B b pyco
    toz cyslo neco pyco

    cyslo foo(){
        davaj 1 pyco
    }

    cyslo bar(){
        davaj 3 pyco
    }
}

tryda B fagan od A {
    toz cyslo neco pyco

    cyslo neco(){
        dechrobok pyco
        davaj 27 pyco
    }

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
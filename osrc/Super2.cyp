/**
 * Created by tomaskohout on 12/13/15.
 */

banik pyco


tryda Z {
    bar(){
        Konzola.pravit("Z::bar()") pyco
    }
}

tryda A fagan od Z {

    foo(){
        bar() pyco
        forant.bar() pyco
    }

    bar(){
        Konzola.pravit("A::bar()") pyco
    }
}

tryda B fagan od A {
    bar(){
        Konzola.pravit("B::bar()") pyco
    }

    foo(){
        forant.foo() pyco
    }
}

tryda Ostrava {
    rynek(){
        toz A a = zrob A() pyco
        a = zrob B() pyco
        a.foo() pyco
    }
}

fajront pyco 

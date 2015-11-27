banik pyco

tryda Ostrava {

     rynek(){
        	toz A a pyco
        	toz B b pyco
        	toz X x pyco
        	toz Y y pyco
        	toz Z z pyco

        	a = zrob A() pyco
        	x = zrob X() pyco
        	y = zrob Y() pyco
        	a.foo(x, y) pyco

        	a = zrob B() pyco
        	x = zrob X() pyco
        	y = zrob Z() pyco
        	a.foo(x, y) pyco

        	b = zrob B() pyco
        	x = zrob X() pyco
        	y = zrob Z() pyco
        	b.foo(x, y) pyco

        	b = zrob B() pyco
        	x = zrob X() pyco
        	z = zrob Z() pyco
        	b.foo(x, z) pyco


    }

}

tryda X {
  print() {
	Konzola.pravit("   I'm X") pyco
  }
}

tryda Y {
  print() {
	Konzola.pravit("   I'm Y") pyco
  }
}

tryda Z fagan od Y {
  print() {
	Konzola.pravit("   I'm Z") pyco
  }
}


tryda A {
  foo(X x, Y y) {
	Konzola.pravit("In A::foo(X,Y)") pyco
	x.print() pyco
	y.print() pyco
  }
}

tryda B fagan od A {
  foo(X x, Z y) {
	Konzola.pravit("In B::foo(X,Z)") pyco
	x.print() pyco
	y.print() pyco
  }
}




fajront pyco
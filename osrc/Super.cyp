banik pyco

tryda Ostrava {

     rynek(){
        	toz A a pyco
        	toz B b pyco
        	toz X x pyco
        	toz Y y pyco
        	toz Z z pyco

        	a = zrob A() pyco
        	b = zrob B() pyco

        	x = zrob X() pyco
        	z = zrob Z() pyco

        	a.foo(x, z) pyco
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
  A(){
  	Konzola.pravit("A:construct") pyco
  }

  A(cyslo i){
    Konzola.pravit("A:construct(") pyco
    Konzola.pravit(i) pyco
    Konzola.pravit(")") pyco
  }

  foo(X x, Z z) {
	Konzola.pravit("In A::foo(X,Z)") pyco
	x.print() pyco
	z.print() pyco
  }
}

tryda B fagan od A {

	foo(X x, Z z) {
		Konzola.pravit("In B::foo(X,Z)") pyco
		x.print() pyco
		z.print() pyco

		forant.foo(x,z) pyco
	}
}




fajront pyco
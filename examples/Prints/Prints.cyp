banik pyco

tryda A {
    toz cyslo c pyco

    A(cyslo c){
        joch.c = c pyco
    }

    Dryst naDryst(){
        toz CysloCele cele = zrob CysloCele(joch.c) pyco
        davaj cele.naDryst() pyco
    }
}

tryda Ostrava{

    rynek(){
        toz Dryst dryst1 = "Nejaky" pyco
        toz Dryst dryst2 = " dryst" pyco

        toz cyslo i = 250 pyco
        toz chachar c = 'b' pyco
        toz bul b = fajne pyco
        toz A a = zrob A(1020) pyco

        Konzola.pravit(dryst1.chacharNa(2)) pyco
        Konzola.pravit(dryst1.pridaj(dryst2)) pyco

        Konzola.pravit(a) pyco

        //Mel by zavolat naDryst()
        Konzola.pravit(a) pyco
        Konzola.pravit(i) pyco
        Konzola.pravit(c) pyco
        Konzola.pravit(b) pyco
    }

}

fajront pyco
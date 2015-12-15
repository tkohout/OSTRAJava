banik pyco

tryda Ostrava {

     rynek(){
            //Pro tento priklad nastav HEAP_SIZE = 100

            //Zacatek - bude ulozen v tenure
            toz A old = zrob A() pyco


            toz A a = old pyco
            toz A temp pyco

            toz cyslo i = 0 pyco

            rubat(i < 88){
                temp = a pyco
                a = zrob A() pyco

                a.poradi = i+1 pyco

                //Dirty link
                temp.next = a pyco

                i = i + 1 pyco
            }

            old.print() pyco


            dechrobok pyco
    }

}

tryda A {
    toz A next pyco
    toz cyslo poradi pyco

    print(){
        Konzola.pravit(joch.poradi) pyco

        kaj (joch.next != chuj){
            joch.next.print() pyco
        }boinak{
            dechrobok pyco
        }
    }
}







fajront pyco
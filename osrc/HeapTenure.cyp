banik pyco

tryda Ostrava {

     rynek(){
            //Pro tento priklad nastav HEAP_SIZE = 100 (10 eden, 90 tenure)

            toz A a pyco
            toz A temp pyco

            toz cyslo i pyco
            toz cyslo j = 0 pyco

            //Vytvor 10 setu
            rubat(j < 1000){
                i = 0 pyco
                a = chuj pyco

                //Vytvor 89 objektu ktery zaplni tenure
                // 1 objekt je reference na main tridu
                rubat(i < 89){
                    temp = a pyco
                    a = zrob A() pyco
                    a.prev = temp pyco

                    i = i + 1 pyco
                }

                a.print(1) pyco


                j = j + 1 pyco
            }


            dechrobok pyco
    }

}

tryda A {
    toz A prev pyco

    print(cyslo i){
        kaj (joch.prev != chuj){
            Konzola.pravit(i) pyco
            joch.prev.print(i+1) pyco
        } boinak {
            Konzola.pravit(i) pyco
        }
    }
}







fajront pyco
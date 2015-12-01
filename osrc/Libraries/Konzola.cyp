banik pyco

tryda Konzola{

    statyk pravit(Bazmek b){
            kaj (b == chuj){
                Konzola.pravit("chuj") pyco
            }boinak{
                toz Dryst dryst = b.naDryst() pyco
                Konzola.pravit(dryst) pyco
            }
    }

    statyk pravit(Dryst dryst){
        kaj (dryst == chuj){
            Konzola.pravit("chuj") pyco
        }boinak{
            toz chachar[] chachari = dryst.naChachar() pyco
            Konzola.pravit(chachari) pyco
        }
    }

    statyk pravit(cyslo d){

      //Native call
      print(d) pyco
    }

    statyk pravit(cyslo_desetinne d){
        //Native call
        print(d) pyco
    }

    statyk pravit(chachar[] c){

        //Native call
        print(c) pyco
    }

    statyk pravit(chachar c){
        print(c) pyco
    }

    statyk pravit(bul b){
        kaj (b == fajne){
            Konzola.pravit("fajne") pyco
        }boinak{
            Konzola.pravit("nyt") pyco
        }
    }



}

fajront pyco
/**
 * Created by tomaskohout on 11/30/15.
 */

banik pyco

tryda Ostrava {

    rynek(){

        toz Citac c = zrob Citac() pyco
        c.otevr("resources/sats/01.txt") pyco

        toz Dryst radka pyco

        radka = c.citajRadku() pyco

        rubat (radka != chuj){
            Konzola.pravit(radka) pyco
            radka = c.citajRadku() pyco
        }


        c.zavr() pyco
    }

}

fajront pyco 

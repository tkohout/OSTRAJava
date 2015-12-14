/**
 * Created by tomaskohout on 11/30/15.
 */

banik pyco

tryda Ostrava {

    rynek(chachar[] vstup){
        kaj (vstup == chuj){
            Konzola.pravit("Chybi vstupni soubor") pyco
            davaj pyco
        }

        toz Parsovac p = zrob Parsovac() pyco
        toz CNF cnf = p.vyparsuj(zrob Dryst(vstup)) pyco
        Konzola.pravit("Vyparsovano") pyco
        Konzola.pravit("Zacinam resit") pyco

        toz Resic r = zrob Resic(cnf) pyco
        r.rubej() pyco
    }



}

fajront pyco 

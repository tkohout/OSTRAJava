/**
 * Created by tomaskohout on 11/30/15.
 */

banik pyco

tryda Ostrava {

    rynek(){
        toz Parsovac p = zrob Parsovac() pyco
        toz CNF cnf = p.vyparsuj("resources/sats/06.txt") pyco

        toz Resic r = zrob Resic(cnf) pyco
        r.rubej() pyco
    }



}

fajront pyco 

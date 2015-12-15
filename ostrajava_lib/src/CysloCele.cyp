/**
 * Created by tomaskohout on 11/26/15.
 */

banik pyco

tryda CysloCele {
    toz cyslo c pyco

    CysloCele(cyslo c){
        joch.c = c pyco
    }

    statyk cyslo preved(Dryst c){
        davaj charArrayToInt(c.naChachar()) pyco
    }

    Dryst naDryst(){
        toz chachar[] chachari = intToCharArray(joch.c) pyco
        davaj zrob Dryst(chachari) pyco
    }

    statyk natyv cyslo charArrayToInt(chachar[] c) pyco
    statyk natyv chachar[] intToCharArray(cyslo c) pyco
}

fajront pyco 

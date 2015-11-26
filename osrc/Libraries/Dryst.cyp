/**
 * Created by tomaskohout on 11/21/15.
 */

banik pyco

tryda Dryst {
    toz chachar[] chachari pyco
    toz cyslo dylka pyco
    toz cyslo kapacyta pyco

    Dryst dryst(chachar[] chachari){
        //joch.kapacyta = 32 pyco
        joch.chachari = chachari pyco
        davaj joch pyco
    }

    Dryst dryst(cyslo c){
        //TODO: make it work for bigger numbers %10 ???
        joch.chachari = zrob chachar[1] pyco


        //Prevedem na ASCII
        joch.chachari[0] = c + 48  pyco
    }

    chachar[] naChachar(){
        davaj joch.chachari pyco
    }

    /*pridaj(cyslo chachar){
        kaj (joch.dylka >= joch.kapacyta){
            joch.kapacyta = joch.kapacyta * 2 pyco
            zvetsi(joch.kapacyta) pyco
        }
        joch.chachari[joch.dylka - 1] = chachar pyco
    }

    zvetsi(cyslo velikost){
        toz cyslo[] docasne = joch.pismena pyco

        joch.pismena = zrob
        //TODO: Na tohle potrebujem cyklus

    }*/
}

fajront pyco 

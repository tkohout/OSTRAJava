/**
 * Created by tomaskohout on 11/26/15.
 */

banik pyco

tryda CysloCele {
    toz cyslo c pyco

    CysloCele(cyslo c){
        joch.c = c pyco
    }

    Dryst naDryst(){
        toz cyslo c = joch.c pyco

        toz bul hotovo = nyt pyco
        toz  cyslo dylka = logint(c, 10) + 1 pyco

        toz chachar[] chachari = zrob chachar[dylka] pyco

        toz cyslo i = dylka-1 pyco
        toz cyslo num, mod pyco

        rubat (i >= 0){
            mod = powint(10, i) pyco
            num = c / mod  pyco
            c = c % mod pyco

            //Prevedem na ASCII
            chachari[dylka-1-i] = num + 48 pyco
            i = i - 1 pyco
        }

        davaj zrob Dryst(chachari) pyco
    }
}

fajront pyco 

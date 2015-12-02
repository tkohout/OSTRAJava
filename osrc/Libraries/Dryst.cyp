/**
 * Created by tomaskohout on 11/21/15.
 */

banik pyco

tryda Dryst {
    toz chachar[] chachari pyco
    toz cyslo dylka pyco
    toz cyslo kapacyta pyco

    Dryst dryst(chachar[] chachari){
        joch.dylka = arraySize(chachari) pyco
        joch.chachari = chachari pyco
        davaj joch pyco
    }



    chachar[] naChachar(){
        davaj joch.chachari pyco
    }

    bul kantuje(Dryst d){
        toz cyslo i = 0 pyco

        kaj (joch.dylka != d.dylka){
            davaj nyt pyco
        }

        rubat(i < joch.dylka){
            kaj (joch.chachari[i] != d.chachari[i]){
                davaj nyt pyco
            }
            i = i + 1 pyco
        }

        davaj fajne pyco
    }

    Dryst[] rozdel(chachar znak){
        toz cyslo i = 0 pyco
        toz cyslo pocet = 0 pyco
        toz Dryst[] casti pyco

        //Napred spocitame
        rubat(i < joch.dylka){
            kaj (joch.chachari[i] == znak){
                pocet = pocet + 1 pyco
            }
            i = i + 1 pyco
        }

        casti = zrob Dryst[pocet+1] pyco

        i = 0 pyco
        pocet = 0 pyco

        //Buffer max velikosti stringu
        toz Bafr bafr = zrob Bafr(joch.dylka) pyco

        rubat(i < joch.dylka){
            kaj (joch.chachari[i] == znak){
                casti[pocet] = zrob Dryst(bafr.naChachar()) pyco
                pocet = pocet + 1 pyco
                //Reset
                bafr.vyglancovat() pyco
            } boinak {
                bafr.pridaj(joch.chachari[i]) pyco
            }
            i = i + 1 pyco
       }

       kaj (bafr.pocet > 0){
            casti[pocet] = zrob Dryst(bafr.naChachar()) pyco
       }

       davaj casti pyco
    }


}

fajront pyco 

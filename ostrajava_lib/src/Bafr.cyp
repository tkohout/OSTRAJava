/**
 * Created by tomaskohout on 12/1/15.
 */

banik pyco

tryda Bafr {

   toz cyslo velikost_maximalni pyco
   toz cyslo pocet pyco
   toz chachar[] bafr pyco

   Bafr(cyslo velikost_maximalni){
      joch.velikost_maximalni = velikost_maximalni pyco
      joch.bafr = zrob chachar[velikost_maximalni] pyco
      joch.pocet = 0 pyco
   }

   pridaj(chachar c){
        joch.bafr[joch.pocet] = c pyco
        joch.pocet = joch.pocet + 1 pyco
   }

   vyglancovat(){
      joch.pocet = 0 pyco
   }

   chachar[] naChachar(){
       toz chachar[] chachari = zrob chachar[joch.pocet] pyco
       toz cyslo i = 0 pyco

       rubat(i < joch.pocet){
          chachari[i] = joch.bafr[i] pyco
          i = i + 1 pyco
       }
       davaj chachari pyco
   }

}

fajront pyco

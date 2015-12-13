/**
 * Created by tomaskohout on 12/1/15.
 */

banik pyco

tryda Citac {

   toz cyslo readerAddress pyco

   otevr(Dryst jmeno){
      toz chachar[] chachari = jmeno.naChachar() pyco
      joch.readerAddress = openReader(chachari) pyco
   }

   Dryst citajRadku(){
      toz chachar[] radka = readLine(joch.readerAddress) pyco

      kaj (radka != chuj){
         davaj zrob Dryst(radka) pyco
      }

      davaj chuj pyco
   }

   zavr(){
      closeReader(joch.readerAddress) pyco
   }

   natyv cyslo openReader(chachar[] fileName) pyco
   natyv closeReader(cyslo addr) pyco
   natyv chachar[] readLine(cyslo addr) pyco
    
}

fajront pyco 

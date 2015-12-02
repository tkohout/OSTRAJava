/**
 * Created by tomaskohout on 12/1/15.
 */

banik pyco

tryda Parsovac {
    Klauzule[] vyparsuj(Dryst soubor){

        toz Citac c = zrob Citac() pyco
        c.otevr(soubor) pyco

        toz Dryst radka pyco
        toz Dryst[] casti pyco
        radka = c.citajRadku() pyco

        toz Klauzule[] klauzule pyco

        toz cyslo promennych = 0 pyco

        toz cyslo klauzuleIndex = 0 pyco

        rubat (radka != chuj){
            casti = radka.rozdel(' ') pyco

            toz cyslo j = 0 pyco
            toz cyslo pocet = arraySize(casti) pyco

            toz Dryst prvni = casti[0] pyco

            kaj (pocet != 0 aj prvni.kantuje("c") == nyt ){

                kaj (prvni.kantuje("p") == fajne ){
                    toz cyslo klauzuli = CysloCele.preved(casti[2]) pyco
                    promennych = CysloCele.preved(casti[3]) pyco

                    klauzule = zrob Klauzule[klauzuli] pyco

                } boinak {
                    toz Promenna[] promenne = zrob Promenna[pocet] pyco

                    rubat (j < pocet){
                        toz Dryst cast = casti[j] pyco

                        kaj (cast.kantuje("0") == fajne){
                            zdybat pyco
                        }

                        toz Promenna p = zrob Promenna(CysloCele.preved(casti[j])) pyco
                        promenne[j] = p pyco
                        j = j + 1 pyco
                    }
                    klauzule[klauzuleIndex] = zrob Klauzule(promenne) pyco
                    klauzuleIndex = klauzuleIndex + 1 pyco

                }
            }

            radka = c.citajRadku() pyco

        }
        c.zavr() pyco

        davaj klauzule pyco
    }
}

fajront pyco 

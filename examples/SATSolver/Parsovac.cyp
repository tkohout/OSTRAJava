/**
 * Created by tomaskohout on 12/1/15.
 */

banik pyco

tryda Parsovac {
    CNF vyparsuj(Dryst soubor){

        toz Citac c = zrob Citac() pyco
        c.otevr(soubor) pyco

        toz Dryst radka pyco
        toz Dryst[] casti pyco
        radka = c.citajRadku() pyco

        toz Klauzule[] klauzule pyco

        toz Dryst nula = "0" pyco
        toz Dryst komentar = "c" pyco
        toz Dryst meta = "p" pyco


        toz cyslo promennych = 0 pyco

        toz cyslo klauzuleIndex = 0 pyco

        rubat (radka != chuj){
            casti = radka.rozdel(' ') pyco

            toz cyslo j = 0 pyco
            toz cyslo pocet = Pole.velikost(casti) pyco

            toz Dryst prvni = casti[0] pyco

            kaj (pocet != 0 aj prvni.kantuje(komentar) == nyt ){

                kaj (prvni.kantuje(meta) == fajne ){
                    toz cyslo klauzuli = CysloCele.preved(casti[3]) pyco
                    promennych = CysloCele.preved(casti[2]) pyco

                    klauzule = zrob Klauzule[klauzuli] pyco

                } boinak {
                    toz cyslo[] promenne = zrob cyslo[promennych] pyco

                    rubat (j < pocet){
                        toz Dryst cast = casti[j] pyco

                        kaj (cast.kantuje(nula) == fajne){
                            zdybat pyco
                        }

                        toz cyslo p = CysloCele.preved(casti[j]) pyco
                        toz cyslo hodnota pyco
                        kaj (p < 0){
                            p = p * (0-1) pyco
                            hodnota = -1 pyco
                        } boinak {
                            hodnota = 1 pyco
                        }

                        promenne[p-1] = hodnota pyco

                        j = j + 1 pyco
                    }

                    klauzule[klauzuleIndex] = zrob Klauzule(promenne) pyco
                    klauzuleIndex = klauzuleIndex + 1 pyco

                }
            }

            radka = c.citajRadku() pyco

        }
        c.zavr() pyco

        toz CNF cnf = zrob CNF(klauzule, promennych, klauzuli) pyco

        davaj cnf pyco
    }
}

fajront pyco 

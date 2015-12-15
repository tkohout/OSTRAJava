/**
 * Created by tomaskohout on 12/2/15.
 */

banik pyco

tryda Reseni {
    toz cyslo pocetPromennych pyco
    toz cyslo[] ohodnoceni pyco



    Reseni(Reseni r){
        joch(r.pocetPromennych) pyco

        toz cyslo i = 0 pyco
        rubat(i<joch.pocetPromennych){
            joch.ohodnoceni[i] = r.ohodnoceni[i] pyco
            i = i + 1 pyco
        }
    }


    Reseni(cyslo pocetPromennych){
        joch.pocetPromennych = pocetPromennych pyco
        joch.ohodnoceni = zrob bul[pocetPromennych] pyco
    }

    nastavFajne(cyslo indexPromenne){
        joch.ohodnoceni[indexPromenne] = 1 pyco
    }

    nastavNyt(cyslo indexPromenne){
        joch.ohodnoceni[indexPromenne] = -1 pyco
    }

    cyslo daj(cyslo indexPromenne){
        davaj joch.ohodnoceni[indexPromenne] pyco
    }

    cyslo dajDalsi(){
        toz cyslo i = 0 pyco
        rubat(i<joch.pocetPromennych){
            toz cyslo h = joch.ohodnoceni[i] pyco

            kaj (h == 0){
                davaj i pyco
            }

            i = i + 1 pyco
        }

        davaj -1 pyco
    }

    //Vraci 1 - fajne, -1 nyt, 0 nerozhodnuto
    cyslo dajVysledek(Klauzule k){
        toz cyslo i = 0 pyco
        toz bul nenastaveno = nyt pyco

        rubat(i < joch.pocetPromennych){
            toz cyslo promenna = k.promenne[i] pyco
            toz cyslo ohodnoceni = daj(i) pyco

            kaj (promenna != 0){
                kaj (ohodnoceni != 0){

                    //Jak je jeden clen vyhodnocen fajne -  napr. x1' a ohodnoceni nyt a nebo x1 a ohodnoceni fajne
                    //Cela klauzule je fajne
                    kaj (promenna == ohodnoceni){
                        /*Konzola.pravit("Index/p/h") pyco
                        Konzola.pravit(i) pyco
                        Konzola.pravit(promenna) pyco
                        Konzola.pravit(ohodnoceni) pyco*/

                        davaj 1 pyco
                    }

                } boinak {
                    nenastaveno = fajne pyco
                }
            }

            i = i + 1 pyco
        }

        //Nezbyli nam nenastavene promenne, musi to byt nyt
        kaj (nenastaveno == nyt){
            davaj -1 pyco
        }

        //Nerozhodnoto
        davaj 0 pyco
    }


    Dryst naDryst(){
        toz chachar[] c = zrob chachar[joch.pocetPromennych] pyco
        toz cyslo i = 0 pyco
        rubat(i<joch.pocetPromennych){
            toz cyslo h = joch.ohodnoceni[i] pyco

            kaj (h == -1){
                c[i] = '0' pyco
            } kajtez (h == 1) {
                c[i] = '1' pyco
            } boinak {
                c[i] = '?' pyco
            }

            i = i + 1 pyco
        }

        davaj zrob Dryst(c) pyco
    }

}

fajront pyco 

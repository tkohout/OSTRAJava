/**
 * Created by tomaskohout on 12/2/15.
 */

banik pyco

tryda Resic {
    toz CNF cnf pyco

    Resic(CNF cnf){
        joch.cnf = cnf pyco
    }

    rubej(){
        toz bul res = vyres() pyco


        kaj (res == fajne){
            Konzola.pravit("Resitelne") pyco
        }boinak{
            Konzola.pravit("Neresitelne") pyco
        }

        dechrobok pyco

    }

    bul vyres(){
        toz Reseni reseni = zrob Reseni(joch.cnf.pocetPromennych) pyco

        toz bul resitelne = vyres(reseni) pyco

        davaj resitelne pyco
    }

    bul vyres(Reseni reseni){
        toz Reseni nove pyco
        toz cyslo vseci = ohodnotVseci(reseni) pyco

        //Vsechny klauzule jsou splnene
        kaj (vseci == 1){
            Konzola.pravit("Konecne reseni: ") pyco
            Konzola.pravit(reseni.naDryst()) pyco
            dechrobok pyco
           davaj fajne pyco
        //Aspon jedna klauzule je nesplnena
        } kajtez (vseci == -1){
           davaj nyt pyco
        }

        //Konzola.pravit(reseni.naDryst()) pyco


        //Zkusime najit ciste promenne
        toz Reseni ciste = najdiPromenneCiste(reseni) pyco

        kaj (ciste != chuj){
            //Konzola.pravit("Promenne ciste: ") pyco
            davaj vyres(ciste) pyco
        }

        //Zkusime najit unit clause
        toz cyslo promennaRazovita = najdiKlauzuliRazovitu(reseni) pyco

        kaj (promennaRazovita != 0){
            nove = zrob Reseni(reseni) pyco

            toz cyslo indexRazovity pyco
            kaj (promennaRazovita < 0){
                indexRazovity = (promennaRazovita * -1) -1 pyco
                nove.nastavNyt(indexRazovity) pyco
            } boinak {
                indexRazovity = promennaRazovita - 1 pyco
                nove.nastavFajne(indexRazovity) pyco
            }

            //Konzola.pravit("Klauzule razovita: ") pyco
            davaj vyres(nove) pyco
        }




        //Vezmeme dalsi v rade
        toz cyslo index = reseni.dajDalsi() pyco

        nove = zrob Reseni(reseni) pyco
        nove.nastavNyt(index) pyco

        kaj (vyres(nove) == fajne){
           dechrobok pyco
           davaj fajne pyco
        }

        //Zkusime nastavit fajne
        nove = zrob Reseni(reseni) pyco
        nove.nastavFajne(index) pyco

        davaj vyres(nove) pyco

    }

    cyslo ohodnotVseci(Reseni r){
        toz cyslo i = 0 pyco
        //Ohodnoceni vsech klauzuli

        rubat(i < joch.cnf.pocetKlauzuli){
            toz Klauzule k = joch.cnf.klauzule[i] pyco
            toz cyslo v = r.dajVysledek(k) pyco

            //Pokud je aspon jeden nyt, vsichni jsou nyt
            kaj ( v == -1 ) {
                davaj -1 pyco
            //Pokud jedna klauzule nerozhodnuta, vsichni jsou nerozhodnuti
            }kajtez (v == 0){
                davaj 0 pyco
            }

            i = i + 1 pyco
        }
        davaj 1 pyco
    }

    //Najde promenne ktere se v cnf vyskytuji pouze positivni, negativni a nebo vubec
    Reseni najdiPromenneCiste(Reseni r){
        toz cyslo i = 0 pyco
        toz cyslo[] kandydati = zrob cyslo[joch.cnf.pocetPromennych] pyco
        toz cyslo kandydat pyco
        toz cyslo vyluc_konstanta = -2 pyco


        rubat(i < joch.cnf.pocetKlauzuli){
            toz Klauzule k = joch.cnf.klauzule[i] pyco
            toz cyslo v = r.dajVysledek(k) pyco
            //Ignorujeme jiz vyresene klauzule
            kaj (v != 1){
                toz cyslo j = 0 pyco

                rubat(j < k.pocet){
                    toz cyslo p = k.promenne[j] pyco
                    kandydat = kandydati[j] pyco

                    //Pokud promenna neni stejna jako nektera predchozi a je definovana v klauzuli
                    //Pokud uz nebyla vyloucena
                    //Pokud uz neni v reseni


                    kaj (kandydat != vyluc_konstanta aj p != kandydat aj p != 0){
                        //Pokud jeste nebyl definovan
                        kaj (kandydat == 0){
                            kandydati[j] = p pyco
                        //Pokud uz byl, vyluc
                        }boinak{
                            kandydati[j] = vyluc_konstanta pyco
                        }
                    }

                    j = j + 1 pyco
                }
            }
            i = i + 1 pyco
        }

        i = 0 pyco

        toz Reseni nove = zrob Reseni(r) pyco

        toz bul nalezeno = nyt pyco

        rubat (i < joch.cnf.pocetPromennych){

            toz cyslo ohodnoceni = r.daj(i) pyco

            kaj (ohodnoceni == 0){
                kandydat = kandydati[i] pyco

                //Vsechny vyskyty promenne jsou fajne
                kaj (kandydat == 1){
                    nove.nastavFajne(i) pyco
                    nalezeno = fajne pyco
                //Vsechny vyskyty promenne jsou nyt
                }kajtez (kandydat == -1){
                    nove.nastavNyt(i) pyco
                    nalezeno = fajne pyco
                //Promenna se nikdy nevyskytla, tak ji nastavime na nyt
                }kajtez (kandydat == 0){
                    nove.nastavNyt(i) pyco
                    nalezeno = fajne pyco
                }
            }
            i = i + 1 pyco
        }

        kaj (nalezeno == fajne){
            davaj nove pyco
        }

        davaj chuj pyco
    }

    //Unit clause
    cyslo najdiKlauzuliRazovitu(Reseni r){
        toz cyslo i = 0 pyco



        rubat(i < joch.cnf.pocetKlauzuli){
            toz Klauzule k = joch.cnf.klauzule[i] pyco

            toz cyslo v = r.dajVysledek(k) pyco

            //Klauzule neni rozhodnuta
            kaj (v == 0){
                toz cyslo j = 0 pyco
                toz cyslo nevyhodnocenyIndex = -1 pyco
                rubat(j < k.pocet){

                    toz cyslo promenna = k.promenne[j] pyco
                    toz cyslo ohodnoceni = r.daj(j) pyco
                    kaj (promenna != 0){

                        //Promenna jeste neni rozhodnuta
                        kaj (ohodnoceni == 0){


                            kaj (nevyhodnocenyIndex == -1){
                                nevyhodnocenyIndex = j pyco
                            }boinak{
                                //Vic jak jeden nevyhodnoceny index, neni to unit clause
                                nevyhodnocenyIndex = -1 pyco
                                zdybat pyco
                            }
                        }
                    }

                    j = j + 1 pyco
                }

                kaj (nevyhodnocenyIndex != -1){
                    promenna = k.promenne[nevyhodnocenyIndex] pyco

                    //Index zacina od 1
                    nevyhodnocenyIndex = nevyhodnocenyIndex + 1 pyco

                    //Vrat zaporny index pro negaci
                    kaj (promenna < 0){
                        davaj nevyhodnocenyIndex * (0 - 1) pyco
                    //kladny index pro normalni promennou
                    }boinak{
                        davaj nevyhodnocenyIndex pyco
                    }
                }
            }

            i = i + 1 pyco
        }

        davaj 0 pyco
    }



}

fajront pyco 

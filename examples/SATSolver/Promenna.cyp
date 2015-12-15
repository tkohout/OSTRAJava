/**
 * Created by tomaskohout on 12/1/15.
 */

banik pyco

tryda Promenna {
    toz cyslo index pyco
    toz bul negace pyco

    Promenna(cyslo index){
        joch.negace = nyt pyco
        joch.index = index pyco

        kaj (index < 0){
            joch.index = index * (0-1) pyco
            joch.negace = fajne pyco
        }
    }

    Dryst naDryst(){
        toz CysloCele c = zrob CysloCele(joch.index) pyco
        davaj c.naDryst() pyco
    }


}

fajront pyco 

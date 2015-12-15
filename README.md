# OSTRAJava #

OSTRAJava má za cíl pozvednout kvalitu (nejen ostravského) programování. Na rozdíl od Javy vychází přímo z ostravského nářečí, tedy jazyka který obyčejní lidé používají. Typický ostravský horník pak nebude mít problém přejít od těžby rudy ke klávesnici. Jazyk tak mimo jiné řeší i problém nezaměstnanosti na moravsko-slezsku.

## Syntaxe ##

### Hello world ###
Zde je typický Hello world program v OSTRAJavě.
```
#!java
banik pyco

tryda Ostrava{
    rynek(){
        Konzola.pravit("Toz vitaj") pyco
    }
} 

fajront pyco

```

* Všechny soubory OSTRAJavy začínají klíčovým slovem **banik**. 
* Všechny soubory OSTRAJavy končí klíčovým slovem **fajront**, označující konec směny.
* Každý statement programu musí být ukončen klíčovým slovem **pyco**. Nahrazuje tedy v javě používaný středník.
* Hlavní třída programu se vždy jmenuje **Ostrava**(jak jinak) a hlavní metoda se vždy jmenuje **rynek**
* Pozn.: OSTRAJava je case in-sensitive a nedoporučuje se používat diakritiku, jinými slovy s **malym a kratkym**

### Typy ###
OSTRAJava obsahuje 4 primitivní typy:

* **cyslo** - integer
* **bul** - boolean
* **chachar** - char
* **cyslo_desetinne** - float

Hodnoty:

* *fajne* - true
* *nyt* - false
* *chuj* - null

Základní knihovna OSTRAJavy také obsahuje tridy

* **Dryst** - ekvivalent String.  
* **Bazmek** - vsechny tridy dedi implicitne z teto tridy. Obsahuje metody naDryst() a kantuje(Bazmek b), ekvivalent pro toString() a equals(Object o)
* **Citac** - cteni ze souboru
* **Konzola** - vypis do konzole

### Příklad 2 ###
```
#!java
banik pyco

tryda Priklad2 {
   nacti(Dryst nazevSouboru){

        toz Citac c = zrob Citac() pyco
        c.otevr(nazevSouboru) pyco

        toz Dryst radka pyco
        radka = c.citajRadku() pyco

        rubat (radka != chuj){
            kaj (radka.kantuje("neco")){
                 dlabat pyco
            } kajtez (radka.kantuje("neco jineho")){
                // ...
            } boinak {
               zdybat pyco
            }

            radka = c.citajRadku() pyco
        }
        c.zavr() pyco
    }
}

fajront pyco 

```

Na výše uvedém příkladu se napřed vytvoří nová instance třídy *Citac* pomocí klíčového slova **zrob**. Deklarace nové proměnné začíná klíčovým slovem **toz**.

Poté se pomocí while cyklu (klíčové slovo **rubat**) načtou jednotlive řádky. 

Pomocí if statementu (**kaj** - **kajtez** - **boinak**) se pak zpracuje vstup.

Rubat cyklus se dá přeřušit pomocí **zdybat** (break) a nebo přeskočit jeden cyklus pomocí **dlabat** (continue)

### Dědičnost ###

```
#!java
banik pyco

tryda Obdelnik{
   toz cyslo dylka pyco
   toz cyslo vyska pyco

   Obdelnik(cyslo dylka, cyslo vyska){
      joch.dylka = dylka pyco
      joch.vyska = vyska pyco
   }
}

tryda Stverec fagan od Obdelnik{
    Stverec(cyslo velikost){
       forant(velikost, velikost) pyco
    }
} 

tryda Ostrava{
   rynek(){
      toz Stverec s = zrob Stverec(5) pyco 
   }
}

fajront pyco

```

OSTRAJava je samozřejmě jazyk objektový. 
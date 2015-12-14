# OSTRAJava #

OSTRAJava má za cíl pozvednout kvalitu (nejen ostravského) programování. Na rozdíl od Javy vychází přímo z ostravského nářečí, tedy jazyka který obyčejní lidé používají. Typický ostravský horník pak nebude mít problém přejít od těžby rudy ke klávesnici. Jazyk tak mimo jiné řeší i problém nezaměstnanosti na moravsko-slezsku.

## Syntaxe ##

### Základy ###
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

### Třídy ###

```
#!java
banik pyco

tryda Obrazec{
   toz cyslo dylka pyco
   toz cyslo vyska pyco

   Obrazec(cyslo dylka, cyslo vyska){
      joch.dylka = dylka pyco
      joch.vyska = vyska pyco
   }
}

tryda Stverec fagan od Obrazec{
    Stverec(cyslo velikost){
       forant(velikost, velikost) pyco
    }
} 

fajront pyco

```



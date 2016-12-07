# OSTRAJava #

OSTRAJava má za cíl pozvednout kvalitu (nejen ostravského) programování. Na rozdíl od Javy vychází OSTRAJava přímo z mluveného jazyka (ostravského nářečí), tedy z něčeho čemu obyčejní lidé rozumějí. Typický ostravský horník pak nebude mít problém přejít od těžby uhlí ke klávesnici. Jazyk tak mimo jiné řeší i problém nezaměstnanosti na moravsko-slezsku.

## Syntaxe ##

### Příklad 1 - Hello world ###
Zde je typický Hello world program v OSTRAJavě.

```java
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
* Každý statement programu musí být ukončen klíčovým slovem **pyco**. Nahrazuje tedy v Javě používaný středník.
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

Základní knihovna OSTRAJavy také obsahuje třídy

* **Dryst** - ekvivalent String.  
* **Bazmek** - vsechny tridy dedi implicitne z teto tridy. Obsahuje metody naDryst() a kantuje(Bazmek b), ekvivalent pro toString() a equals(Object o)
* **Citac** - cteni ze souboru
* **Konzola** - vypis do konzole
* **Bafr** - jednoduchý buffer na čtení chacharů

### Deklarace ###
Deklaraci je nutno začít klíčovým slovem **toz**

```java
toz cyslo i = 1 pyco
```


### Podmínky ###
Pro vytvoření podmínky použijeme konstrukt **kaj**  - **kajtez** - **boinak**

Jednotlivé výrazy můžeme spojovat pomocí boolenovského **ci** (or) a **aj** (and)

```java
toz bul a pyco
toz bul b pyco

//...

kaj (a == fajne ci b == fajne){
   // ...
} kajtez (a == nyt aj b == fajne){
   // ...
} boinak {
   // ...
}

```

### Cykly ###
Cyklus má OSTRAJava pouze jeden. Pomocí **rubat** vytvoříme while.

Rubat cyklus se dá přeřušit pomocí **zdybat** (break) a nebo přeskočit jeden cyklus pomocí **dlabat** (continue)

```java

toz cyslo i = 0 pyco

rubat (i < 5){
    kaj (i == 4){
      zdybat pyco 
   }
   //...
   i = i+1 pyco
}

``` 

### Příklad 2 ###
Následuje jednoduchý příklad na čtení ze souboru

```java
banik pyco

tryda Priklad2 {
   nacti(Dryst nazevSouboru){

        toz Citac c = zrob Citac() pyco
        c.otevr(nazevSouboru) pyco

        toz Dryst radka pyco
        radka = c.citajRadku() pyco

        rubat (radka != chuj){
            // ...
            radka = c.citajRadku() pyco
        }
        c.zavr() pyco
    }
}

fajront pyco 

```

### Dědičnost ###

```java
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

OSTRAJava je samozřejmě jazyk objektový. Pokud chceme uvést třídu z které chceme dědit použijeme konstrukt **fagan od**.

**joch** slouží jako reference na vlastní instanci tedy ekvivalent this

**forant** tedy předák na šichtě slouží jako reference na rodiče, tedy ekvivalent super

nový objekt vytvoříme pomocí **zrob**

Pokud bychom chtěli vytvořit pole uděláme to následovně

```java

toz cyslo[] pole = zrob cyslo[5] pyco

```

### Metody ###
Navratový typ metody se píše stejně jako v javě před název metody. Pro metodu s prázdným návratovým typem není třeba psát nic. 

Pro navrácení hodnoty je použito klíčové slovo **davaj**

```java
tryda Buu{
   cyslo fuu(Dryst text){
       //...
       davaj text.dylka pyco
   }
}
```

## Implementace ##

Implementováno momentálně je:

* Compiler
* * Pokročilá typová kontrola (přiřazení objektů, návratové hodnoty funkcí, fieldy)
* * Kontrola deklarace a inicializace proměnných
* * Kontrola volání metod (static, non-static)
* * Kompilace do .tryda classfilu

* Interpreter
* * Paměť pomocí pole objektů
* * Generační garbage collector
* * Nativní volání
* * Statické volání
* * Primitiva (integer, float) na stacku a pointer s 1 na posledním bitu
* * Aritmetické operace pro integer a float
* * Pole (primitiv i referencí)
* * Dynamický lookup metod
* * Overload metody

### Kompilace a spuštění ###

#### Závislosti ####
Pro kompilaci OSTRAJava překladače je potřeba:
 * Java překladač (JDK) >= 1.7 (http://www.oracle.com/technetwork/java/javase/downloads/index.html)
 * Maven (https://maven.apache.org/)

Pro spuštění zkompilovaných binárek postačí Java interpret (JRE) >= 1.7
(http://www.oracle.com/technetwork/java/javase/downloads/index.html)


#### Kompilace OSTRAJava překladače a interpretru ####

V root složce 

```
mvn clean
mvn install

```

Nastavení práv pro skripty

```
chmod u+x ostrajavac
chmod u+x ostrajava
```


#### Kompilace OSTRAJava programů ####

Program SATSolver je ve složce examples/SATSolver/

```
./ostrajavac examples/SATSolver/ -d compiled/

```

#### Spuštění OSTRAJava programů ####
```
./ostrajava -h 1024 -f 256 -s 128 compiled/ resources/sats/01.txt

```

### Autor ###

Tomáš Kohout

FIT ČVUT

(www.whostolemyunicorn.com)

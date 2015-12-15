package JavaTesting;

import javax.sound.midi.Soundbank;
import java.util.*;

public class  Main {

    public static void testOperators(){
        int i = 0;
        int j = 0;

        i = j + i * 40 + 5 - 3;



        //System.out.println(Thread.currentThread().getStackTrace());
        Thread.dumpStack();
        //System.out.println(i);
        return;
    }

    public static void testConditions(){
        int i = 0;
        int j = 0;
        boolean f = (j < i && (i > j || i == 4));
    }

    public static void testNew(){
        /*Test obj;
        obj = new Test();
        obj.foo(1);*/


        //obj = new ArrayList<>();
        /*Object obj2 = new Object();

        obj = obj2;*/
    }

    public static void tesRef(){
        int i = 10,j = 15;

        i = j;
    }

    public void foo(int i){};
    public void boo(){};


    public static void main(String[] args) throws Exception
    {
        testOperators();
    }


}
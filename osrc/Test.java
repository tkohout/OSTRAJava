import java.util.*;

public class  Test {

    public void testOperators(){
        int i = 0;
        int j = 0;

        i = j + i * 40 + 5 - 3;
    }

    public void testConditions(){
        int i = 0;
        int j = 0;
        boolean f = (j < i && (i > j || i == 4));
    }

    public void testNew(){
        List<String> obj;
        //obj = new ArrayList<>();
        /*Object obj2 = new Object();

        obj = obj2;*/
    }

    public void tesRef(){
        int i = 10,j = 15;

        i = j;
    }

    public void foo(){};
    public void boo(){};


}
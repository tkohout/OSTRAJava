public class  Test {

    public void testOperators(){
        int i = 0;
        int j = 0;

        i = j + i * 40 + 5 - 3;
    }

    public void testConditions(){
        int i = 0;
        int j = 0;

        if (j < i || (i > j && i == 4) || 1>i){
            foo();
        }else{
            boo();
        }
    }


    public void foo(){};
    public void boo(){};


}
package cz.cvut.fit.ostrajava.Interpreter.Natives.IO.File;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tomaskohout on 12/1/15.
 */
public class Readers {
    private static Readers instance = null;
    Map<Integer,BufferedReader> readers;
    int lastAddress = 0;

    protected Readers() {
        readers = new HashMap();
    }
    public static Readers getInstance() {
        if(instance == null) {
            instance = new Readers();
        }
        return instance;
    }

    public int add(BufferedReader reader){
        int address = lastAddress;
        readers.put(address, reader);
        lastAddress++;

        return address;
    }

    public BufferedReader get(int i){
        return readers.get(i);
    }


    public void remove(int i){
        readers.remove(i);
    }

}

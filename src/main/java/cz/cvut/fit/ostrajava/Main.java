
package cz.cvut.fit.ostrajava;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;
import cz.cvut.fit.ostrajava.Interpreter.ClassPool;
import cz.cvut.fit.ostrajava.Interpreter.OSTRAJavaInterpreter;
import cz.cvut.fit.ostrajava.Parser.*;


public class Main {

    public static void main(String[] args) throws Exception
    {

        String command =args[0];
        //Remove the command
        String[] commandArgs = Arrays.copyOfRange(args, 1, args.length);

        if (command.equals("run")){
            Run.exec(commandArgs);
        }else if (command.equals("compile")){
            Compile.exec(commandArgs);
        }else{
            System.out.println("use either 'run' or 'compile' command");
        }
    }




}

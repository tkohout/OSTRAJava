
package cz.cvut.fit.ostrajava;

import java.io.*;
import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Parser.*;


public class Main {

    public static void main(String[] args) throws Exception
    {
        Reader fr = null;
        if (args.length == 2) {
            fr = new InputStreamReader(new FileInputStream(new File(args[0])), args[1]);
        }else if (args.length == 1) {
            fr = new InputStreamReader(new FileInputStream(new File(args[0])));
        }else {
            System.out.println("Include filename in the arguments");
            return;
        }

        OSTRAJavaParser jp = new OSTRAJavaParser(fr);

        try {

            jp.CompilationUnit();

            //Parse
            ASTCompilationUnit node = (ASTCompilationUnit)jp.rootNode();
            node.dump("");

            //Compile
            OSTRAJavaCompiler compiler = new OSTRAJavaCompiler(node);
            compiler.compile();


            System.out.println("OSTRAJava Parser:  Java program parsed successfully.");
        } catch (ParseException e) {
            System.out.println("OSTRAJava Parser:  Encountered errors during parsing.");
            throw e;
        }

    }
}

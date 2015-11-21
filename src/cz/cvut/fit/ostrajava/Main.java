
package cz.cvut.fit.ostrajava;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import cz.cvut.fit.ostrajava.Compiler.*;
import cz.cvut.fit.ostrajava.Compiler.Class;
import cz.cvut.fit.ostrajava.Interpreter.OSTRAJavaInterpreter;
import cz.cvut.fit.ostrajava.Parser.*;


public class Main {

    public static void main(String[] args) throws Exception
    {
        Reader fr = null;

        if (args.length == 0) {
            System.out.println("Include filename in the arguments");
            return;
        }

        OSTRAJavaParser jp = null;

        List<Class> classList = new ArrayList<>();

        for (String fileName: args) {

            fr = new InputStreamReader(new FileInputStream(new File(fileName)));

            if (jp == null){
                jp = new OSTRAJavaParser(fr);
            }else{
                jp.ReInit(fr);
            }

            try {

                //Parse
                jp.CompilationUnit();
                ASTCompilationUnit node = (ASTCompilationUnit) jp.rootNode();

                //Compile
                OSTRAJavaCompiler compiler = new OSTRAJavaCompiler(node);
                classList.addAll(compiler.compile());

                OSTRAJavaInterpreter interpreter = new OSTRAJavaInterpreter(classList);
                interpreter.run();
            } catch (ParseException e) {
                System.out.println("Parsing exception in file " + fileName);
                throw e;
            }/* catch (CompilerException e) {
                System.out.println("Compiler exception in file " + fileName);
                throw e;
            }*/


        }

    }
}

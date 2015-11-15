package cz.cvut.fit.ostrajava.Compiler;


import cz.cvut.fit.ostrajava.Parser.ASTCompilationUnit;
import cz.cvut.fit.ostrajava.Parser.OSTRAJavaParser;
import cz.cvut.fit.ostrajava.Parser.ParseException;
import org.junit.Test;
import sun.misc.IOUtils;

import java.io.*;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by tomaskohout on 11/14/15.
 */
public class OSTRAJavaCompilerTest {

    @Test

    public void testCompiler() throws Exception {
        String code = methodLayout("");

        ByteCode byteCode = compileSingleMethod(code);
        //assertEquals("Bytecode should be empty", byteCode.toString(), "");
    }


    @Test
    public void testPlusMinus() throws Exception {

        String code = "toz number i = 1 pyco i = i + 1 - 2 pyco";

        ByteCode byteCode = compileSingleMethod(methodLayout(code));
        assertEquals(byteCode.toString(),
                "0: ipush 1 \n" +
                "1: istore 0 \n" +
                "2: iload 0 \n" +
                "3: ipush 1 \n" +
                "4: iadd \n" +
                "5: ipush 2 \n" +
                "6: isub \n" +
                "7: istore 0 \n");
    }

    @Test
    public void testMultiplication() throws Exception {

        String code = "toz number i = 1 pyco i = i / 5 * 3 pyco";

        ByteCode byteCode = compileSingleMethod(methodLayout(code));
        assertEquals(byteCode.toString(),
                "0: ipush 1 \n" +
                "1: istore 0 \n" +
                "2: iload 0 \n" +
                "3: ipush 5 \n" +
                "4: idiv \n" +
                "5: ipush 3 \n" +
                "6: imul \n" +
                "7: istore 0 \n");
        }

    protected String methodLayout(String code){
        return "banik pyco " +
                "tryda Ostrava {" +
                "fraj rynek(){ " +
                code +
                " }" +
                "}" +
                "fajront pyco";
    }

    protected ByteCode compileSingleMethod(String code) throws CompilerException, ParseException {
        StringReader stringReader = new StringReader(code);

        OSTRAJavaParser jp = new OSTRAJavaParser(stringReader);

        jp.CompilationUnit();

        //Parse
        ASTCompilationUnit node = (ASTCompilationUnit)jp.rootNode();
        node.dump("");

        //Compile
        OSTRAJavaCompiler compiler = new OSTRAJavaCompiler(node);
        List<ClassFile> classfiles = compiler.compile();

        assertTrue(classfiles.size() == 1);

        ClassFile clazz = classfiles.get(0);

        assertTrue(clazz.getMethods().size() == 1);

        Method method = clazz.getMethods().get(0);

        return method.getByteCode();
    }

}
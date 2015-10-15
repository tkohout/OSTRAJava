
import java.io.*;

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
        jp.CompilationUnit();
    }
}

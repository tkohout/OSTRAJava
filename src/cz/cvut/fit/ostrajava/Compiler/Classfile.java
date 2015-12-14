package cz.cvut.fit.ostrajava.Compiler;

import java.io.*;

/**
 * Created by tomaskohout on 12/13/15.
 */
public class Classfile {

    private static final int CONSTANTS_LINE = 1;
    private static final int CLASS_LINE = 1;
    private static final int SUPERCLASS_LINE = 1;
    private static final int FIELDS_LINE = 1;
    private static final int METHODS_LINE = 1;
    private static final int LOCALS_LINE = 1;
    private static final int INSTRUCTIONS_LINE = 1;
    private static final int METHOD_NATIVE_LINE = 1;

    public static File toFile(Class clazz, String fileName) throws IOException {
        File file = new File(fileName);
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(0xCA);
        bw.write(0xFE);
        bw.write(0xBA);
        bw.write(0xBA);
        bw.newLine();


        bw.write(clazz.constantPool.getSize() + "");
        bw.newLine();

        for (String constant: clazz.constantPool.constants){
            bw.write(constant);
            bw.newLine();
        }

        bw.write(clazz.getClassName());
        bw.newLine();
        bw.write(clazz.getSuperName() == null ? "" : clazz.getSuperName());
        bw.newLine();

        bw.write(clazz.getFields().size()+"");
        bw.newLine();


        for (Field field: clazz.getFields()){
            bw.write(field.toString());
            bw.newLine();
        }


        bw.write(clazz.getMethods().size()+"");
        bw.newLine();

        for (Method method: clazz.getMethods()){
            bw.write(method.getDescriptor());
            bw.newLine();
            bw.write(method.getLocalVariablesCount() + "");
            bw.newLine();
            bw.write((method.isNativeMethod() ? 1 : 0) + "");
            bw.newLine();
            bw.write(method.getByteCode().getInstructions().size() + "");
            bw.newLine();
            bw.write(method.getByteCode().toString());
        }

        bw.close();
        return file;
    }


    public static Class fromFile(File file) throws IOException {
        ConstantPool constantPool = new ConstantPool();
        Class clazz = new Class();
        clazz.constantPool = constantPool;

        BufferedReader br = new BufferedReader(new FileReader(file));

        String line;

        int line_counter = 0;
        int constants_size = -1;
        int fields_size = -1;
        int methods_size = -1;
        int instructions_size = -1;
        int method_start = 0;
        int method_count = 0;

        Method method = null;
        ByteCode byteCode = null;

        //I admit this is not my best code ever
        while ((line = br.readLine()) != null) {

            if (line_counter == 0){
                if (line.equals(String.format("%d%d%d%d", 0xCA, 0XFE, 0xBA, 0xBA))){
                    throw new IllegalArgumentException("File is not a OSTRAJava classfile");
                }
            }else{
                if (line_counter == CONSTANTS_LINE){
                    constants_size = Integer.parseInt(line);
                }else{
                    int constants_end = CONSTANTS_LINE + constants_size;
                    if (line_counter > CONSTANTS_LINE && line_counter <= CONSTANTS_LINE + constants_size){
                        constantPool.addConstant(line);
                    }else{
                        if (line_counter == constants_end + CLASS_LINE){
                            clazz.setClassName(line);
                        }else{
                            int classnames_end = constants_end + CLASS_LINE + SUPERCLASS_LINE;

                            if (line_counter == classnames_end){
                                clazz.setSuperName(line);
                            }else{
                                if (line_counter == classnames_end + FIELDS_LINE){
                                    fields_size = Integer.parseInt(line);
                                }else{
                                    int fields_end = classnames_end + FIELDS_LINE + fields_size;

                                    if (line_counter > classnames_end + FIELDS_LINE && line_counter <=  fields_end){
                                        Field field = new Field(line);
                                        clazz.addField(field);
                                    }else {
                                        if (line_counter == fields_end + METHODS_LINE){
                                            methods_size = Integer.parseInt(line);
                                            method_start = fields_end + METHODS_LINE + 1;
                                        }else{
                                            if (line_counter == method_start){
                                                method = new Method(line);
                                                clazz.addMethod(method);
                                                method_count++;
                                            }else {
                                                if (line_counter == method_start + LOCALS_LINE) {
                                                    method.setLocalVariablesCount(Integer.parseInt(line));
                                                }else{
                                                    if (line_counter == method_start + LOCALS_LINE + METHOD_NATIVE_LINE) {
                                                        int isNative = Integer.parseInt(line);
                                                        if (isNative == 1) {
                                                            method.addFlag(Method.MethodFlag.Native);
                                                        }
                                                    }else {
                                                        if (line_counter == method_start + LOCALS_LINE + METHOD_NATIVE_LINE + INSTRUCTIONS_LINE) {
                                                            instructions_size = Integer.parseInt(line);
                                                            byteCode = new ByteCode();
                                                            method.setByteCode(byteCode);
                                                        } else {
                                                            if (line_counter > method_start + LOCALS_LINE+ METHOD_NATIVE_LINE + INSTRUCTIONS_LINE && line_counter <= method_start + LOCALS_LINE + METHOD_NATIVE_LINE + INSTRUCTIONS_LINE + instructions_size) {
                                                                byteCode.addInstruction(new Instruction(line));
                                                            } else {
                                                                if (method_count >= methods_size) {
                                                                    throw new IllegalArgumentException("End expected in the classfile provided");
                                                                } else {
                                                                    method = new Method(line);
                                                                    clazz.addMethod(method);
                                                                    method_start = method_start + LOCALS_LINE + METHOD_NATIVE_LINE + INSTRUCTIONS_LINE + instructions_size + 1;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }


            line_counter++;
        }



        br.close();



        return clazz;

    }
}

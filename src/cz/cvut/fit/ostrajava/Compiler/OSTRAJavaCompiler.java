package cz.cvut.fit.ostrajava.Compiler;

import cz.cvut.fit.ostrajava.Parser.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class OSTRAJavaCompiler {
    protected SimpleNode node;

    public OSTRAJavaCompiler(ASTCompilationUnit node){
        this.node = node;
    }

    public void compile() throws CompilerException {
        if (node.jjtGetNumChildren() == 0){
            throw new CompilerException("No classes to compile");
        }

        int i = 0;

        do {
            node = (SimpleNode)node.jjtGetChild(i);
            if (node instanceof ASTClass){
                compileClass((ASTClass)node);
            }

            i++;
        }while(i < node.jjtGetNumChildren());

    }


    protected ClassFile compileClass(ASTClass node) throws CompilerException {

        ClassFile classFile = new ClassFile(node.getName(), node.getExtending());
        List<Field> fields = new ArrayList<Field>();

        for (int i=0; i<node.jjtGetNumChildren(); i++){
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTFieldDeclaration){
                fields.addAll(fieldDeclaration((ASTFieldDeclaration)child));
            }else if (child instanceof ASTMethodDeclaration){
                Method method = methodDeclaration((ASTMethodDeclaration)child);
                classFile.addMethod(method);
            }
        }

        classFile.setFields(fields);
        return classFile;
    }

    protected List<Field> fieldDeclaration(ASTFieldDeclaration node) throws CompilerException {
        String type;

        //First child is Type
        type = type((ASTType)node.jjtGetChild(0));
        List<Field> fields = new ArrayList<Field>();

        //Second and others (There can be more fields declared) are names
        for (int i=1; i<node.jjtGetNumChildren(); i++) {
            ASTVariable nameNode = (ASTVariable) node.jjtGetChild(i);

            Field field = new Field(nameNode.jjtGetValue().toString(), type);
            fields.add(field);
        }

        return fields;
    }

    protected String type(ASTType node) throws CompilerException {
        String typeString;

        Node type = node.jjtGetChild(0);

        if (type instanceof ASTBool){
            typeString = "bool";
        }else if (type instanceof  ASTNumber){
            typeString = "number";
        }else if (type instanceof  ASTString){
            typeString = "string";
        }else if (type instanceof  ASTName){
            typeString = (String)((ASTName) type).jjtGetValue();
        }else{
            throw new CompilerException("Unexpected type of field " + type );
        }

        return typeString;
    }

    protected Method methodDeclaration(ASTMethodDeclaration node) throws CompilerException {
        String returnType = "void", name = null;
        List<String> args = null;

        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTResultType){
                ASTResultType resultType = ((ASTResultType) child);
                if (resultType.jjtGetNumChildren() != 0){
                    returnType = type((ASTType)resultType.jjtGetChild(0));
                }
            }else if (child instanceof ASTMethod){
                name = ((ASTMethod) child).jjtGetValue().toString();

                ASTFormalParameters params = (ASTFormalParameters)child.jjtGetChild(0);
                args = formalParameters(params);
            }else if (child instanceof ASTBlock){
                methodBlock((ASTBlock) child);
            }
        }

        if (name == null){
            throw new CompilerException("Missing method name in " + node );
        }

        Method method = new Method(name, args, returnType);
        return method;
    }

    protected List<String> formalParameters(ASTFormalParameters node) throws CompilerException {
        List<String> args = new ArrayList<String>();

        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            ASTFormalParameter param = (ASTFormalParameter)node.jjtGetChild(i);
            String type = type((ASTType) param.jjtGetChild(0));
            //we don't care about the name
            //String name =  ((ASTVariable)param.jjtGetChild(1)).jjtGetValue().toString();

            args.add(type);
        }

        return args;
    }

    protected ByteCode methodBlock(ASTBlock node) throws CompilerException {
        List<String> args = null;

        ByteCode byteCode = new ByteCode();

        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTLocalVariableDeclaration){
                byteCode = localVariableDeclaration((ASTLocalVariableDeclaration) child, byteCode);
            }else if (child instanceof ASTStatement){
                statement((ASTStatement) child, byteCode);
            }
        }

        return byteCode;
    }

    protected ByteCode statement(ASTStatement node, ByteCode byteCode) throws CompilerException {

        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            System.out.println();
            /*if (child instanceof ASTLocalVariableDeclaration){
                byteCode = localVariableDeclaration((ASTLocalVariableDeclaration) child, byteCode);
            }else if (child instanceof ASTStatement){
                statement((ASTStatement) child);
            }*/
        }

        return byteCode;
    }


    protected ByteCode localVariableDeclaration(ASTLocalVariableDeclaration node, ByteCode byteCode) throws CompilerException {


        //First child is Type
        Node typeNode = node.jjtGetChild(0).jjtGetChild(0);

        //Second and others (There can be more fields declared) are names
        for (int i=1; i<node.jjtGetNumChildren(); i++) {
            ASTVariableDeclarator declarator = (ASTVariableDeclarator) node.jjtGetChild(i);
            String name = ((ASTVariable) declarator.jjtGetChild(0)).jjtGetValue().toString();

            String valueString = null;
            SimpleNode value = null;

            if (declarator.jjtGetNumChildren() > 1) {
                value = (SimpleNode) declarator.jjtGetChild(1);
                valueString = value.jjtGetValue().toString();
            }

            if (typeNode instanceof ASTNumber){
                if (value != null){
                    if (!(value instanceof ASTNumberLiteral)){
                        throw new CompilerException("Assigning Non-Number literal to a Number type variable");
                    }
                }else{
                    //Default value for integer
                    valueString = "0";
                }

                int position = byteCode.addLocalVariable(name, valueString);

                if (position == -1){
                    throw new CompilerException("Variable '" + name + "' has been already declared");
                }

                byteCode.addInstruction(new Instruction(InstructionSet.PushInteger, valueString));
                byteCode.addInstruction(new Instruction(InstructionSet.StoreInteger, Integer.toString(position)));
            }else{
                throw new NotImplementedException();
            }

        }

        return byteCode;
    }

}

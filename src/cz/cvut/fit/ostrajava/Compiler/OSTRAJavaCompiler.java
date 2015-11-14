package cz.cvut.fit.ostrajava.Compiler;

import com.sun.media.jfxmedia.effects.EqualizerBand;
import com.sun.tools.corba.se.idl.constExpr.Not;
import cz.cvut.fit.ostrajava.Parser.*;
import jdk.nashorn.internal.ir.Block;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Iterator;
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
        ByteCode byteCode = new ByteCode();
        //TODO: So far it's the same, if future it will probably need to load arguments etc...
        return block(node, byteCode);
    }

    protected ByteCode block(ASTBlock node, ByteCode byteCode) throws CompilerException {
        List<String> args = null;

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

            //Everything written with a 'pyco' in the end
            if (child instanceof ASTStatementExpression){
                Node statementExpression = ((ASTStatementExpression)child);
                if (statementExpression.jjtGetChild(0) instanceof ASTAssignment){
                    byteCode = assignment((ASTAssignment) statementExpression.jjtGetChild(0), byteCode);
                }

            }else if (child instanceof ASTBlock){


            }else if (child instanceof ASTIfStatement){
                byteCode = ifStatement((ASTIfStatement)child, byteCode);
            }else if (child instanceof ASTPrintStatement){


            }else if (child instanceof ASTReturnStatement){


            }
            /*if (child instanceof ASTLocalVariableDeclaration){
                byteCode = localVariableDeclaration((ASTLocalVariableDeclaration) child, byteCode);
            }else if (child instanceof ASTStatement){
                statement((ASTStatement) child);
            }*/
        }

        return byteCode;
    }

    protected ByteCode assignment(ASTAssignment node, ByteCode byteCode) throws CompilerException {
        //Assignee -> AssignmentPrefix -> Left
        Node left = node.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);

        //We have to go recursively to the bottom of the tree to find the real expression
        Node right = simplifyExpression(node.jjtGetChild(1));

        //We are assigning to a variable
        if (left instanceof ASTName) {

            //Creates bytecode for the expression
            expression(right, byteCode);

            //TODO: check if we assigning to compatible types
            /*if (right instanceof ASTNumberLiteral){
            }*/

            String name = ((ASTName) left).jjtGetValue().toString();
            int position = byteCode.getPositionOfLocalVariable(name);

            if (position == -1){
                throw new CompilerException("Trying to assign to an undeclared variable '" + name + "'");
            }

            byteCode.addInstruction(new Instruction(InstructionSet.StoreInteger, Integer.toString(position)));
        }else{
            throw new NotImplementedException();
        }


        return byteCode;
    }

    protected ByteCode ifStatement(ASTIfStatement node, ByteCode byteCode) throws CompilerException {

        List<Instruction> gotoInstructions = new ArrayList<>();

        //The conditions
        for (int i=0; i<node.jjtGetNumChildren(); i+=2) {
            //Else Statement
            if (i == node.jjtGetNumChildren()-1){
                ASTBlock block = (ASTBlock) node.jjtGetChild(i);
                block(block, byteCode);
            //If or else-if statement
            }else{
                boolean b = 1 > 2;
                Node child = simplifyExpression(node.jjtGetChild(i));

                //If-expression skip block instructions
                List<Instruction> endBlockInstructions = ifExpression(child, byteCode);

                ASTBlock block = (ASTBlock) node.jjtGetChild(i+1);
                block(block, byteCode);

                //This creates goto instruction on the end of the block which leads to the end of branching
                Instruction gotoInstruction = byteCode.addInstruction(new Instruction(InstructionSet.GoTo, "?"));
                gotoInstructions.add(gotoInstruction);

                //Change the compare instr. so it points to the end of the block
                for (Instruction ebi: endBlockInstructions) {
                    ebi.setOperand(0, Integer.toString(byteCode.getLastInstructionPosition() + 1));
                }
            }

        }

        //Go through all goto instruction and set them to the end
        for (Instruction i : gotoInstructions){
            i.setOperand(0, Integer.toString(byteCode.getLastInstructionPosition() + 1));
        }
        return byteCode;
    }

    //Traverse through the expression tree and simplify it so that the expression children are the immediate children
    protected Node simplifyExpression(Node node) throws  CompilerException{
        if (node.jjtGetNumChildren() == 1){
            return simplifyExpression(node.jjtGetChild(0));
        }else if (node.jjtGetNumChildren() > 1){
            //Go recursively through the children
            for (int i=0; i<node.jjtGetNumChildren(); i++) {
                Node child = simplifyExpression(node.jjtGetChild(i));
                //Replace the old expression
                node.jjtAddChild(child, i);
            }
        }
        //Return node if there are no more children
        return node;
    }

    protected ByteCode expression(Node node, ByteCode byteCode) throws CompilerException{
        if (node.jjtGetNumChildren() > 1) {
            if (node instanceof ASTConditionalOrExpression || node instanceof ASTConditionalAndExpression || node instanceof ASTRelationalExpression || node instanceof ASTEqualityExpression) {
                ifExpression(node, byteCode);
            }else if (node instanceof ASTAdditiveExpression || node instanceof ASTMultiplicativeExpression) {
                arithmeticExpression(node, byteCode);
            }
        }else if (node instanceof ASTName){
            String name = ((ASTName) node).jjtGetValue().toString();
            int position = byteCode.getPositionOfLocalVariable(name);

            if (position == -1){
                throw new CompilerException("Variable '" + name + "' is not declared");
            }

            byteCode.addInstruction(new Instruction(InstructionSet.LoadInteger, Integer.toString(position)));
        }else if (node instanceof ASTNumberLiteral){
            String value = ((ASTNumberLiteral) node).jjtGetValue().toString();
            byteCode.addInstruction(new Instruction(InstructionSet.PushInteger, value));
        }else{
            throw new NotImplementedException();
        }

        return byteCode;
    }

    /*protected List<Instruction> multipleExpression(Node node, ByteCode byteCode) throws CompilerException{
        List<Instruction> instructions = new ArrayList<>();

        Node first = node.jjtGetChild(0);
        expression(first, byteCode);

        for (int i=1; i<node.jjtGetNumChildren(); i+=2) {
            Node operator = node.jjtGetChild(i);
            Node child = node.jjtGetChild(i+1);

            expression(child, byteCode);

            Instruction instruction = null;

            if (node instanceof ASTConditionalOrExpression) {

            }else if (node instanceof ASTEqualityExpression) {
                if (operator instanceof ASTEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareEqualInteger, "?");
                }else{
                    instruction = new Instruction(InstructionSet.IfCompareNotEqualInteger, "?");
                }
            }else if (node instanceof ASTRelationalExpression) {

                    if (operator instanceof ASTGreaterThanOperator){
                        instruction = new Instruction(InstructionSet.IfCompareGreaterThanInteger, "?");
                    }else if (operator instanceof ASTGreaterThanOrEqualOperator){
                        instruction = new Instruction(InstructionSet.IfCompareGreaterThanOrEqualInteger, "?");
                    }else if (operator instanceof ASTLessThanOperator){
                        instruction = new Instruction(InstructionSet.IfCompareLessThanInteger, "?");
                    }else if (operator instanceof ASTLessThanOrEqualOperator){
                        instruction = new Instruction(InstructionSet.IfCompareLessThanOrEqualInteger, "?");
                    }


            }else if (node instanceof ASTAdditiveExpression) {
                if (operator instanceof ASTPlusOperator) {
                    instruction = new Instruction(InstructionSet.AddInteger);
                }else{
                    instruction = new Instruction(InstructionSet.SubstractInteger);
                }
            }else if (node instanceof ASTMultiplicativeExpression) {
                if (operator instanceof ASTMultiplyOperator) {
                    instruction = new Instruction(InstructionSet.MultiplyInteger);
                }else if (operator instanceof  ASTDivideOperator){
                    instruction = new Instruction(InstructionSet.DivideInteger);
                }else if (operator instanceof ASTModuloOperator){
                    instruction = new Instruction(InstructionSet.ModuloInteger);
                }
            }else{
                throw new NotImplementedException();
            }

            instructions.add(instruction);
            byteCode.addInstruction(instruction);
        }

        return instructions;
    }*/

    protected List<Instruction> ifExpression(Node node, ByteCode byteCode) throws CompilerException {
        if (node instanceof ASTEqualityExpression || node instanceof ASTRelationalExpression){
            return compareExpression(node, byteCode);
        }else if (node instanceof ASTConditionalOrExpression){
            return orExpression((ASTConditionalOrExpression)node, byteCode);
        }else if (node instanceof  ASTConditionalAndExpression){
            return andExpression((ASTConditionalAndExpression)node, byteCode);
        }else{
            throw new NotImplementedException();
        }
    }

    protected List<Instruction> compareExpression(Node node, ByteCode byteCode) throws CompilerException {
        List<Instruction> instructions = new ArrayList<>();

        Node first = node.jjtGetChild(0);
        expression(first, byteCode);

        for (int i = 1; i < node.jjtGetNumChildren(); i += 2) {
            Node operator = node.jjtGetChild(i);
            Node child = node.jjtGetChild(i + 1);

            expression(child, byteCode);

            Instruction instruction = null;

            if (node instanceof ASTEqualityExpression) {
                if (operator instanceof ASTEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareEqualInteger, "?");
                } else {
                    instruction = new Instruction(InstructionSet.IfCompareNotEqualInteger, "?");
                }
            } else if (node instanceof ASTRelationalExpression) {

                if (operator instanceof ASTGreaterThanOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareGreaterThanInteger, "?");
                } else if (operator instanceof ASTGreaterThanOrEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareGreaterThanOrEqualInteger, "?");
                } else if (operator instanceof ASTLessThanOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareLessThanInteger, "?");
                } else if (operator instanceof ASTLessThanOrEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareLessThanOrEqualInteger, "?");
                }
            }

            if (instruction == null){
                throw new NotImplementedException();
            }

            instructions.add(instruction);
            byteCode.addInstruction(instruction);
        }

        return instructions;
    }

    protected List<Instruction> boolExpression(Node node, ByteCode byteCode) throws CompilerException {
        List<Instruction> toBlockInstructions = new ArrayList<>();
        boolean lastChildAnd = false;
        //Instruction that will skip the execution block if passed
        List<Instruction> endBlockInstruction = new ArrayList<>();

        for (int i = 0; i < node.jjtGetNumChildren(); i += 2) {
            Node child = node.jjtGetChild(i );
            List<Instruction> childInstructions = ifExpression(child, byteCode);


            if (node instanceof ASTConditionalOrExpression) {
                //In nested AND expression
                if (child instanceof ASTConditionalAndExpression) {

                    //It's the last -> send all instructions to the end of the block
                    if (i == node.jjtGetNumChildren() - 1) {
                        lastChildAnd = true;
                        endBlockInstruction.addAll(childInstructions);
                        //It's not the last -> send all instructions to the next condition
                    } else {
                        Iterator<Instruction> itr = childInstructions.iterator();

                        while (itr.hasNext()) {
                            Instruction instruction = itr.next();

                            if (itr.hasNext()) {
                                instruction.setOperand(0, Integer.toString(byteCode.getLastInstructionPosition() + 1));
                            } else {
                                toBlockInstructions.add(instruction);
                                instruction.invert();
                            }
                        }
                    }

                } else {
                    toBlockInstructions.addAll(childInstructions);
                }
            }else if (node instanceof ASTConditionalAndExpression) {

                //In nested AND expression
                if (child instanceof ASTConditionalOrExpression) {

                    //It's the last
                    if (i == node.jjtGetNumChildren() - 1) {
                        lastChildAnd = true;
                        endBlockInstruction.addAll(childInstructions);

                    } else {
                        endBlockInstruction.addAll(childInstructions);
                    }

                } else {
                    toBlockInstructions.addAll(childInstructions);
                }
            }
        }

        Iterator<Instruction> itr = toBlockInstructions.iterator();




        while(itr.hasNext()) {
            Instruction instruction = itr.next();
            if (node instanceof ASTConditionalOrExpression) {
                if (itr.hasNext() || lastChildAnd) {
                    instruction.setOperand(0, Integer.toString(byteCode.getLastInstructionPosition()+1));
                }else{
                    instruction.invert();
                    endBlockInstruction.add(instruction);
                }
            } else if (node instanceof ASTConditionalAndExpression) {
                instruction.invert();
                endBlockInstruction.add(instruction);
            }
        }

        //byteCode.addInstruction(new Instruction(InstructionSet.GoTo, "?"));
        //instructions.add(instruction);
        //byteCode.addInstruction(instruction);

        return endBlockInstruction;
    }


    protected List<Node> mergeConditionals(Node node) {
        List<Node> merged = new ArrayList<>();

        for (int i = 0; i < node.jjtGetNumChildren(); i += 1) {
            Node child = node.jjtGetChild(i );

            if ((node instanceof ASTConditionalOrExpression && child instanceof ASTConditionalOrExpression )
                    || (node instanceof ASTConditionalAndExpression && child instanceof ASTConditionalAndExpression ) ){
                merged.addAll(mergeConditionals(child));
            }else{
                merged.add(child);
            }
        }

        return merged;
    }

    protected List<Instruction> orExpression(ASTConditionalOrExpression node, ByteCode byteCode) throws CompilerException {

        //Instructions that should go to execution block if passed
        List<Instruction> toBlockInstructions = new ArrayList<>();

        //Indicates whether last child is an nested AND
        boolean lastChildAnd = false;

        //Instruction that will skip the execution block if passed
        List<Instruction> endBlockInstruction = new ArrayList<>();

        //Merge together same conditionals for easier computation (e.g. ( x or ( y or z) )
        List<Node> children = mergeConditionals(node);

        for (int i = 0; i < children.size(); i += 2) {
            Node child = children.get(i);

            List<Instruction> childInstructions = ifExpression(child, byteCode);

                //In nested AND expression
                if (child instanceof ASTConditionalAndExpression) {

                    //It's the last, every instruction leads to the end
                    if (i == node.jjtGetNumChildren() - 1) {
                        lastChildAnd = true;
                        endBlockInstruction.addAll(childInstructions);

                    //It's not the last, every instruction goes to next condition. Last instruction goes to block if passed
                    } else {
                        Iterator<Instruction> itr = childInstructions.iterator();

                        while (itr.hasNext()) {
                            Instruction instruction = itr.next();

                            if (itr.hasNext()) {
                                instruction.setOperand(0, Integer.toString(byteCode.getLastInstructionPosition() + 1));
                            } else {
                                toBlockInstructions.add(instruction);
                                instruction.invert();
                            }
                        }
                    }

                //It's simple condition
                } else {
                    toBlockInstructions.addAll(childInstructions);
                }
        }

        Iterator<Instruction> itr = toBlockInstructions.iterator();

        while(itr.hasNext()) {
            Instruction instruction = itr.next();

            //Not last or the AND is the last
            if (itr.hasNext() || lastChildAnd) {
                //Go to the code
                instruction.setOperand(0, Integer.toString(byteCode.getLastInstructionPosition() + 1));
            } else {
                //Invert last instruction and send it to the end
                instruction.invert();
                endBlockInstruction.add(instruction);
            }

        }

        return endBlockInstruction;
    }

    protected List<Instruction> andExpression(ASTConditionalAndExpression node, ByteCode byteCode) throws CompilerException {
        List<Instruction> instructions = new ArrayList<>();

        //Instruction that will skip the execution block if passed
        List<Instruction> endBlockInstruction = new ArrayList<>();

        //Merge together same conditionals for easier computation (e.g. ( x and ( y and z) )
        List<Node> children = mergeConditionals(node);

        for (int i = 0; i < children.size(); i += 2) {
            Node child = children.get(i);
            List<Instruction> childInstructions = ifExpression(child, byteCode);

                //In nested AND expression
                if (child instanceof ASTConditionalOrExpression) {
                    endBlockInstruction.addAll(childInstructions);
                } else {
                    instructions.addAll(childInstructions);
                }

        }

        Iterator<Instruction> itr = instructions.iterator();

        while(itr.hasNext()) {
            Instruction instruction = itr.next();

            //Invert instruction and send it to end block
            instruction.invert();
            endBlockInstruction.add(instruction);
        }

        return endBlockInstruction;
    }

    protected void arithmeticExpression(Node node, ByteCode byteCode) throws CompilerException {
        Node first = node.jjtGetChild(0);
        expression(first, byteCode);

        for (int i = 1; i < node.jjtGetNumChildren(); i += 2) {
            Node operator = node.jjtGetChild(i);
            Node child = node.jjtGetChild(i + 1);

            expression(child, byteCode);

            Instruction instruction = null;

            if (node instanceof ASTAdditiveExpression) {
                if (operator instanceof ASTPlusOperator) {
                    instruction = new Instruction(InstructionSet.AddInteger);
                } else {
                    instruction = new Instruction(InstructionSet.SubstractInteger);
                }
            } else if (node instanceof ASTMultiplicativeExpression) {
                if (operator instanceof ASTMultiplyOperator) {
                    instruction = new Instruction(InstructionSet.MultiplyInteger);
                } else if (operator instanceof ASTDivideOperator) {
                    instruction = new Instruction(InstructionSet.DivideInteger);
                } else if (operator instanceof ASTModuloOperator) {
                    instruction = new Instruction(InstructionSet.ModuloInteger);
                }
            }

            if (instruction == null) {
                throw new NotImplementedException();
            }

            byteCode.addInstruction(instruction);
        }

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

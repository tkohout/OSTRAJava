package cz.cvut.fit.ostrajava.Compiler;

import cz.cvut.fit.ostrajava.Parser.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class OSTRAJavaCompiler {
    final String THIS_VARIABLE = "joch";

    protected SimpleNode node;

    public OSTRAJavaCompiler(ASTCompilationUnit node){
        this.node = node;
    }

    public List<ClassFile> compile() throws CompilerException {
        if (node.jjtGetNumChildren() == 0){
            throw new CompilerException("No classes to compile");
        }

        int i = 0;
        List<ClassFile> classFiles = new ArrayList<>();
        do {
            node = (SimpleNode)node.jjtGetChild(i);
            if (node instanceof ASTClass){
                classFiles.add(compileClass((ASTClass)node));
            }

            i++;
        }while(i < node.jjtGetNumChildren());

        return classFiles;
    }


    protected ClassFile compileClass(ASTClass node) throws CompilerException {

        ClassFile classFile = new ClassFile(node.getName(), node.getExtending());
        List<Field> fields = new ArrayList<Field>();

        for (int i=0; i<node.jjtGetNumChildren(); i++){
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTFieldDeclaration){
                fields.addAll(fieldDeclaration((ASTFieldDeclaration)child));
            }else if (child instanceof ASTMethodDeclaration){
                Method method = methodDeclaration((ASTMethodDeclaration)child, node.getName());
                classFile.addMethod(method);
            }
        }

        classFile.setFields(fields);
        return classFile;
    }

    protected List<Field> fieldDeclaration(ASTFieldDeclaration node) throws CompilerException {
        Type type;

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

    protected Type type(ASTType node) throws CompilerException {
        Type type;

        Node typeNode = node.jjtGetChild(0);

        if (typeNode instanceof ASTBool){
            type = Type.Boolean();
        }else if (typeNode instanceof  ASTNumber){
            type = Type.Number();
        }else if (typeNode instanceof  ASTString){
            type = Type.String();
        }else if (typeNode instanceof  ASTName){
            String className = (String)((ASTName) typeNode).jjtGetValue();
            type = Type.Reference(className);
        }else{
            throw new CompilerException("Unexpected type of field " + typeNode );
        }

        return type;
    }

    protected Method methodDeclaration(ASTMethodDeclaration node, String className) throws CompilerException {
        Type returnType = Type.Void();
        String name = null;
        List<Type> args = new ArrayList<>();
        ByteCode byteCode = new ByteCode();
        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTResultType){
                ASTResultType resultType = ((ASTResultType) child);
                if (resultType.jjtGetNumChildren() != 0){
                    returnType = type((ASTType)resultType.jjtGetChild(0));
                }
            }else if (child instanceof ASTMethod){


                name = ((ASTMethod) child).jjtGetValue().toString();

                //Add This as first argument
                byteCode.addLocalVariable(THIS_VARIABLE, Type.Reference(className));

                //Add the rest of arguments
                ASTFormalParameters params = (ASTFormalParameters)child.jjtGetChild(0);
                args = formalParameters(params, byteCode);
            }else if (child instanceof ASTBlock){
                try{
                    methodBlock((ASTBlock) child, byteCode);
                //Check if the return type matches
                } catch (ReturnException e) {
                    Node value = e.getValue();
                    if (value == null){
                        if (returnType != Type.Void()){
                            throw new CompilerException("Method '" + name + "' must return non-void value");
                        }
                    }else{
                        try {
                            typeCheck(returnType, value, byteCode);
                        }catch(TypeException te){
                            throw new CompilerException("Returning incompatible type in method '" + name + "': " + te.getMessage());
                        }
                    }
                }
            }
        }

        if (name == null){
            throw new CompilerException("Missing method name in " + node );
        }

        Method method = new Method(name, args, returnType);

        //TODO: Variables should get out of bytecode
        method.setLocalVariablesCount(byteCode.getNumberOfLocalVariables());
        method.setByteCode(byteCode);

        return method;
    }

    protected List<Type> formalParameters(ASTFormalParameters node, ByteCode byteCode) throws CompilerException {
        List<Type> args = new ArrayList<>();

        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            ASTFormalParameter param = (ASTFormalParameter)node.jjtGetChild(i);
            Type type = type((ASTType) param.jjtGetChild(0));

            String name = ((ASTVariable) param.jjtGetChild(1)).jjtGetValue().toString();

            args.add(type);
            byteCode.addLocalVariable(name, type);

            //TODO: This might be a problem
            //We conclude that the arguments are initialized
            Variable var = byteCode.getLocalVariable(name);
            var.setInitialized(true);
        }

        return args;
    }

    protected void methodBlock(ASTBlock node, ByteCode byteCode) throws CompilerException,ReturnException {
        block(node, byteCode);
        //On the end of a method is always empty return
        throw new ReturnException(null);
    }

    protected void block(ASTBlock node, ByteCode byteCode) throws CompilerException,ReturnException {
        List<String> args = null;

            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                Node child = node.jjtGetChild(i);

                if (child instanceof ASTLocalVariableDeclaration) {
                    localVariableDeclaration((ASTLocalVariableDeclaration) child, byteCode);
                } else if (child instanceof ASTStatement) {
                    statement((ASTStatement) child, byteCode);
                }
            }
    }

    protected void statement(ASTStatement node, ByteCode byteCode) throws CompilerException, ReturnException {
        Node child = node.jjtGetChild(0);

            //Everything written with a 'pyco' in the end
            if (child instanceof ASTStatementExpression){
                statementExpression((ASTStatementExpression) child, byteCode);
            }else if (child instanceof ASTIfStatement){
                ifStatement((ASTIfStatement)child, byteCode);
            }else if (child instanceof ASTBlock){
                throw new NotImplementedException();
            }else if (child instanceof ASTPrintStatement){
                throw new NotImplementedException();
            }else if (child instanceof ASTReturnStatement){
                returnStatement((ASTReturnStatement) child, byteCode);
            }else{
                throw new NotImplementedException();
            }
    }

    protected void statementExpression(ASTStatementExpression node, ByteCode byteCode) throws CompilerException {
        Node child = node.jjtGetChild(0);
        //It's an assignment
        if (child instanceof ASTAssignment){
            Node assignmentNode = (ASTAssignment) node.jjtGetChild(0);

            //Assignee -> AssignmentPrefix -> Left
            Node left = assignmentNode.jjtGetChild(0).jjtGetChild(0).jjtGetChild(0);

            Node right = assignmentNode.jjtGetChild(1);

            assignment(left, right, byteCode);
            //It's a call
        }else if (isCallExpression(child)) {
            call(node.jjtGetChild(0), byteCode);
        }else{
            throw new NotImplementedException();
        }
    }

    protected void returnStatement(ASTReturnStatement node, ByteCode byteCode) throws CompilerException,ReturnException {
        Node child = null;
        if (node.jjtGetNumChildren() == 1){
            child = simplifyExpression(node.jjtGetChild(0));
            List<Instruction> ifInstructions = expression(child, byteCode);

            if (isConditionalExpression(child)) {
                convertConditionalExpressionToBoolean(child, ifInstructions, byteCode);
            }
        }

        throw new ReturnException(child);
    }

    protected void assignment(Node left, Node right, ByteCode byteCode) throws CompilerException {

        right = simplifyExpression(right);

        //We are assigning to a variable
        if (isVariable(left)) {

            String name = ((ASTName) left).jjtGetValue().toString();
            int position = byteCode.getPositionOfLocalVariable(name);

            if (position == -1){
                throw new CompilerException("Trying to assign to an undeclared variable '" + name + "'");
            }

            //Initialize the variable
            Variable var = byteCode.getLocalVariable(name);


            Type type = byteCode.getTypeOfLocalVariable(name);

            try {
                typeCheck(type, right, byteCode);

                //Run expression
                List<Instruction> ifInstructions = expression(right, byteCode);

                //If the expression is a condition we have to evaluate it
                if (isConditionalExpression(right)){
                    //Converts cond expression to actual boolean instruction
                    convertConditionalExpressionToBoolean(right, ifInstructions, byteCode);
                }

                storeVariable(var, byteCode);

            } catch (TypeException e) {
                throw new CompilerException("Type error on '" + name + "': " + e.getMessage());
            }

        }else{
            throw new NotImplementedException();
        }
    }

    protected void storeVariable(Variable var, ByteCode byteCode){
        int variableIndex = byteCode.getPositionOfLocalVariable(var.getName());
        Type type = var.getType();

        InstructionSet instruction;

        if (type.isReference()){
            instruction = InstructionSet.StoreReference;
        }else if (type == Type.Number()){
            instruction = InstructionSet.StoreInteger;
        }else if (type == Type.Boolean()){
            instruction = InstructionSet.StoreInteger;
        }else{
            throw new NotImplementedException();
        }

        byteCode.addInstruction(new Instruction(instruction, Integer.toString(variableIndex)));

        var.setInitialized(true);
    }

    protected void loadVariable(Variable var, ByteCode byteCode){
        int variableIndex = byteCode.getPositionOfLocalVariable(var.getName());
        Type type = var.getType();

        InstructionSet instruction;

        if (type.isReference()){
            instruction = InstructionSet.LoadReference;
        }else if (type == Type.Number()){
            instruction = InstructionSet.LoadInteger;
        }else if (type == Type.Boolean()){
            instruction = InstructionSet.LoadInteger;
        }else{
            throw new NotImplementedException();
        }

        byteCode.addInstruction(new Instruction(instruction, Integer.toString(variableIndex)));
    }

    protected void typeCheck(Type type, Node value, ByteCode byteCode) throws TypeException, CompilerException{

        if (isConditionalExpression(value) || isBooleanLiteral(value)){
            if (type != Type.Boolean()){
                throw new TypeException("Trying to assign Boolean to type " + type);
            }
            //TODO: Additive might be also for Strings in future
        }else if (isNumberLiteral(value) || isAdditiveExpression(value) || isMultiplicativeExpression(value)){
            if (type != Type.Number()){
                throw new TypeException("Trying to assign Number to type " + type);
            }
        }else if (isVariable(value)){
            String rightName = ((ASTName)value).jjtGetValue().toString();

            int rightPosition = byteCode.getPositionOfLocalVariable(rightName);

            if (rightPosition == -1){
                throw new CompilerException("Variable '" + rightName + "' is undeclared");
            }

            Type rightType = byteCode.getTypeOfLocalVariable(rightName);

            if (rightType != type){
                //Don't type control when it's both references (They can inherit from each other, check it in runtime)
                if (!type.isReference() || !rightType.isReference()) {
                    throw new TypeException("Trying to assign variable '" + rightName + "' of type " + rightType + " to type " + type);
                }
            }

        }else if (isAllocationExpression(value) || isThis(value) || isSuper(value)) {
            if (!type.isReference()) {
                throw new TypeException("Trying to assign Reference to type " + type);
            }
        }else if (isCallExpression(value)){
            //TODO: we don't know return type of the method, how to check it? In runtime probably
        }else{
            throw new NotImplementedException();
        }
    }

    protected void call(Node node, ByteCode bytecode) throws CompilerException{
        if (node.jjtGetNumChildren() <= 1){
            throw new CompilerException("Expected function call");
        }

        node = simplifyExpression(node);

        //Can either be object / object field / method name
        List<Node> objects = new ArrayList<>();

        //PrimaryPrefix -> This / Super / Name
        Node prefix = (Node) node.jjtGetChild(0);

        //It is simple method call
        if (node.jjtGetNumChildren() == 2){
            //Add method name
            objects.add(prefix);

            //Set prefix as This
            prefix = new ASTThis(prefix.getId());
        }

        if (prefix instanceof ASTName || prefix instanceof ASTThis) {
            expression(prefix, bytecode);
        }else{
            throw new NotImplementedException();
        }


        for (int i=1; i<node.jjtGetNumChildren(); i++) {
            Node child = (Node) node.jjtGetChild(i);

            //it's arguments
            if (child instanceof ASTArguments){

                if (objects.size() == 0){
                    throw new CompilerException("Unexpected argument list");
                }

                //Get last object (which is actually method name) and remove from the list
                Node method = objects.get(objects.size() - 1);
                objects.remove(objects.size() - 1);

                String methodName = "";

                if (method instanceof ASTName) {
                    methodName = ((ASTName) method).jjtGetValue().toString();
                }else{
                    throw new CompilerException("Unexpected call of this() or super() in non-constructor method");
                }

                //Suffix -> Arguments
                arguments(child, bytecode);

                bytecode.addInstruction(new Instruction(InstructionSet.InvokeVirtual, methodName));
                //it's object or object in field
            }else if (child instanceof ASTName){
                objects.add(child);
            }
        }

        //If there are fields on the object
        //TODO: Make it work with fields e.g.: this.foo.goo.boo()
        if (objects.size() > 1){
            throw new NotImplementedException();
        }


        return;
    }



    protected void arguments(Node node, ByteCode byteCode) throws CompilerException {
        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            child = simplifyExpression(child);
            List<Instruction> instructions = expression(child, byteCode);

            //Make value from cond-expression
            if (isConditionalExpression(child)){
                convertConditionalExpressionToBoolean(child, instructions, byteCode);
            }
        }
    }



    protected void ifStatement(ASTIfStatement node, ByteCode byteCode) throws CompilerException,ReturnException {

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
                evaluateIfExpression(child, byteCode.getLastInstruction());


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

    }


    protected void evaluateIfExpression(Node node, Instruction lastInstruction) throws CompilerException {
        //When it's single if expression we have to invert last member
        if (isRelationalExpression(node) || isEqualityExpression(node)){
            lastInstruction.invert();
        }
    }

    protected void convertConditionalExpressionToBoolean(Node node, List<Instruction> ifInstructions, ByteCode byteCode) throws  CompilerException{

        //Negate the last if
        evaluateIfExpression(node, byteCode.getLastInstruction());

        //If we success set variable to TRUE
        byteCode.addInstruction(new Instruction(InstructionSet.PushInteger, "1"));
        byteCode.addInstruction(new Instruction(InstructionSet.GoTo, Integer.toString(byteCode.getLastInstructionPosition()+3)));
        //Else set it to false
        byteCode.addInstruction(new Instruction(InstructionSet.PushInteger, "0"));

        //Set end block instructions to go to false
        for (Instruction instr: ifInstructions) {
            instr.setOperand(0, Integer.toString(byteCode.getLastInstructionPosition()));
        }

    }

    //Traverse through the expression tree and simplify it so that the expression children are the immediate children
    protected Node simplifyExpression(Node node) throws  CompilerException{
        //If it's arguments (even with one member, we want to keep it)
        if (node instanceof ASTArguments){
            return node;
        }else if (node.jjtGetNumChildren() == 1){
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

    protected List<Instruction> expression(Node node, ByteCode byteCode) throws CompilerException{
        if (isConditionalExpression(node)) {
            return ifExpression(node, byteCode);
        }else if (isArithmeticExpression(node)) {
            arithmeticExpression(node, byteCode);
        }else if (isAllocationExpression(node)) {
            allocationExpression((ASTAllocationExpression)node, byteCode);
        }else if (isVariable(node)) {
            variable((ASTName) node, byteCode);
        }else if (isThis(node)){
            int position = byteCode.getPositionOfLocalVariable(THIS_VARIABLE);
            byteCode.addInstruction(new Instruction(InstructionSet.LoadReference,Integer.toString(position)));
        }else if (isNumberLiteral(node)) {
            String value = ((ASTNumberLiteral) node).jjtGetValue().toString();
            byteCode.addInstruction(new Instruction(InstructionSet.PushInteger, value));
        }else if (isCallExpression(node)){
            call(node, byteCode);
        }else{
            throw new NotImplementedException();
        }

        return null;
    }

    protected void variable(ASTName node, ByteCode byteCode) throws CompilerException{
        String name =  node.jjtGetValue().toString();
        int position = byteCode.getPositionOfLocalVariable(name);

        if (position == -1) {
            throw new CompilerException("Variable '" + name + "' is not declared");
        }

        Variable var = byteCode.getLocalVariable(name);
        Type type = byteCode.getTypeOfLocalVariable(name);

        if (!var.isInitialized()){
            throw new CompilerException("Variable '" + name + "' is not initialized");
        }

        loadVariable(var, byteCode);
    }

    protected void allocationExpression(ASTAllocationExpression node, ByteCode byteCode) throws CompilerException {
        String name = ((ASTName)node.jjtGetChild(0)).jjtGetValue().toString();

        //TODO: add invocation of constructor

        byteCode.addInstruction(new Instruction(InstructionSet.New, name));
    }

    protected List<Instruction> ifExpression(Node node, ByteCode byteCode) throws CompilerException {
        if (isEqualityExpression(node)|| isRelationalExpression(node)){
            return compareExpression(node, byteCode);
        }else if (isOrExpression(node)) {
            return orExpression((ASTConditionalOrExpression)node, byteCode);
        }else if (isAndExpression(node)){
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

            if (isEqualityExpression(node)) {
                if (operator instanceof ASTEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareEqualInteger, "?");
                } else {
                    instruction = new Instruction(InstructionSet.IfCompareNotEqualInteger, "?");
                }
            } else if (isRelationalExpression(node)) {

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

    protected List<Node> mergeConditionals(Node node) {
        List<Node> merged = new ArrayList<>();

        for (int i = 0; i < node.jjtGetNumChildren(); i += 1) {
            Node child = node.jjtGetChild(i );

            if ((isOrExpression(node) && isOrExpression(child)) || (isAndExpression(node) && isAndExpression(child) ) ){
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
            if (isAndExpression(child)) {

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
            if (isOrExpression(child)){
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

            if (isAdditiveExpression(node)) {
                if (operator instanceof ASTPlusOperator) {
                    instruction = new Instruction(InstructionSet.AddInteger);
                } else {
                    instruction = new Instruction(InstructionSet.SubstractInteger);
                }
            } else if (isMultiplicativeExpression(node)) {
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


    protected void localVariableDeclaration(ASTLocalVariableDeclaration node, ByteCode byteCode) throws CompilerException {
        //First child is Type
        Type type = type((ASTType) node.jjtGetChild(0));

        //Second and others (There can be more fields declared) are names
        for (int i=1; i<node.jjtGetNumChildren(); i++) {
            ASTVariableDeclarator declarator = (ASTVariableDeclarator) node.jjtGetChild(i);

            ASTName nameNode = ((ASTName) declarator.jjtGetChild(0));
            String name = nameNode.jjtGetValue().toString();

            String valueString = null;
            SimpleNode value = null;

            int position = byteCode.addLocalVariable(name, type);

            if (position == -1) {
                throw new CompilerException("Variable '" + name + "' has been already declared");
            }

            //We also assigned value
            if (declarator.jjtGetNumChildren() > 1) {
                value = (SimpleNode) declarator.jjtGetChild(1);
                assignment(nameNode, value, byteCode);
            }
        }
    }

    boolean isConditionalExpression(Node node){
        return isEqualityExpression(node) || isRelationalExpression(node) || isOrExpression(node) || isAndExpression(node);
    }

    boolean isOrExpression(Node node){
        return node instanceof ASTConditionalOrExpression;
    }

    boolean isAndExpression(Node node){
        return node instanceof ASTConditionalAndExpression;
    }

    boolean isEqualityExpression(Node node){
        return node instanceof ASTEqualityExpression;
    }

    boolean isRelationalExpression(Node node){
        return node instanceof ASTRelationalExpression;
    }

    boolean isArithmeticExpression(Node node){
        return isAdditiveExpression(node) || isMultiplicativeExpression(node);
    }

    boolean isAdditiveExpression(Node node){
        return node instanceof ASTAdditiveExpression;
    }
    boolean isMultiplicativeExpression(Node node){
        return node instanceof ASTMultiplicativeExpression;
    }

    boolean isVariable(Node node){
        return node instanceof ASTName;
    }

    boolean isLiteral(Node node){
        return isNumberLiteral(node) || isBooleanLiteral(node);
    }

    boolean isNumberLiteral(Node node){
        return node instanceof ASTNumberLiteral;
    }

    boolean isBooleanLiteral(Node node){
        return node instanceof ASTBooleanLiteral;
    }

    boolean isAllocationExpression(Node node){
        return node instanceof ASTAllocationExpression;
    }

    boolean isCallExpression(Node node){
        return node instanceof ASTPrimaryExpression;
    }

    boolean isThis(Node node){
        return node instanceof ASTThis;
    }

    boolean isSuper(Node node){
        return node instanceof ASTSuper;
    }
}

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

    public List<Class> compile() throws CompilerException {
        if (node.jjtGetNumChildren() == 0){
            throw new CompilerException("No classes to compile");
        }

        int i = 0;
        List<Class> aClasses = new ArrayList<>();
        do {
            Node child = node.jjtGetChild(i);
            if (child instanceof ASTClass){
                aClasses.add(compileClass((ASTClass)child));
            }

            i++;
        }while(i < node.jjtGetNumChildren());

        return aClasses;
    }


    protected Class compileClass(ASTClass node) throws CompilerException {

        String extending = node.getExtending();

        Class aClass = new Class(node.getName().toLowerCase(), (extending != null) ? extending.toLowerCase() : null);
        List<Field> fields = new ArrayList<Field>();

        for (int i=0; i<node.jjtGetNumChildren(); i++){
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTFieldDeclaration){
                fields.addAll(fieldDeclaration((ASTFieldDeclaration)child));
            }else if (child instanceof ASTMethodDeclaration){
                Method method = methodDeclaration((ASTMethodDeclaration)child, node.getName());
                aClass.addMethod(method);
            }
        }

        aClass.setFields(fields);
        return aClass;
    }

    protected List<Field> fieldDeclaration(ASTFieldDeclaration node) throws CompilerException {
        Type type;

        //First child is Type
        type = type((ASTType)node.jjtGetChild(0));
        List<Field> fields = new ArrayList<Field>();

        //Second and others (There can be more fields declared) are names
        for (int i=1; i<node.jjtGetNumChildren(); i++) {
            ASTVariable nameNode = (ASTVariable) node.jjtGetChild(i);

            Field field = new Field(nameNode.jjtGetValue().toString().toLowerCase(), type);
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


                name = ((ASTMethod) child).jjtGetValue().toString().toLowerCase();

                //Add This as first argument
                byteCode.addLocalVariable(THIS_VARIABLE, Type.Reference(className));

                //Add the rest of arguments
                ASTFormalParameters params = (ASTFormalParameters)child.jjtGetChild(0);
                args = formalParameters(params, byteCode);
            }else if (child instanceof ASTBlock){
                try{
                    methodBlock((ASTBlock) child, name,returnType, byteCode);
                //Check if the return type matches
                } catch (ReturnException e) {
                    Node value = e.getValue();
                    if (value == null){
                        if (returnType != Type.Void()){
                            throw new CompilerException("Method '" + name + "' must return non-void value");
                        }

                        byteCode.addInstruction(new Instruction(InstructionSet.ReturnVoid));
                    }else{
                        try {
                            Type valueType = getTypeForExpression(value, byteCode);
                            typeCheck(returnType, valueType);

                            if (returnType == Type.Number() || returnType == Type.Boolean()){
                                byteCode.addInstruction(new Instruction(InstructionSet.ReturnInteger));
                            }else if (returnType.isReference()){
                                byteCode.addInstruction(new Instruction(InstructionSet.ReturnReference));
                            }else{
                                throw new NotImplementedException();
                            }
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

        //TODO: Variables should getBytes out of bytecode
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

    protected void methodBlock(ASTBlock node, String name, Type returnType, ByteCode byteCode) throws CompilerException,ReturnException {
        try{
            block(node, byteCode);
            //On the end of a method is always empty return
            returnStatement(null, byteCode);
        } catch (ReturnException e) {
            Node value = e.getValue();
            if (value == null){
                if (returnType != Type.Void()){
                    throw new CompilerException("Method '" + name + "' must return non-void value");
                }

                byteCode.addInstruction(new Instruction(InstructionSet.ReturnVoid));
            }else{
                try {
                    Type valueType = getTypeForExpression(value, byteCode);
                    typeCheck(returnType, valueType);

                    if (returnType == Type.Number() || returnType == Type.Boolean()){
                        byteCode.addInstruction(new Instruction(InstructionSet.ReturnInteger));
                    }else if (returnType.isReference()){
                        byteCode.addInstruction(new Instruction(InstructionSet.ReturnReference));
                    }else{
                        throw new NotImplementedException();
                    }
                }catch(TypeException te){
                    throw new CompilerException("Returning incompatible type in method '" + name + "': " + te.getMessage());
                }
            }
        }
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
                ifStatement((ASTIfStatement) child, byteCode);
            }else if (child instanceof ASTBlock){
                throw new NotImplementedException();
            }else if (child instanceof ASTPrintStatement){
                throw new NotImplementedException();
            }else if (child instanceof ASTReturnStatement) {
                returnStatement(child, byteCode);
            }else if (child instanceof ASTDebugStatement) {
                    debugStatement(child, byteCode);
            }else{
                throw new NotImplementedException();
            }
    }

    protected void statementExpression(ASTStatementExpression node, ByteCode byteCode) throws CompilerException {
        Node child = node.jjtGetChild(0);

        //It's an variableAssignment
        if (child instanceof ASTAssignment){
            Node assignmentNode = (ASTAssignment) node.jjtGetChild(0);

            //Assignee
            Node left = assignmentNode.jjtGetChild(0);

            //Expression
            Node right = assignmentNode.jjtGetChild(1);

            if (left.jjtGetNumChildren() == 1){
                variableAssignment(left.jjtGetChild(0), right, byteCode);
            }else{
                fieldAssignment(left, right, byteCode);
            }

        //It's a call
        }else if (child instanceof ASTPrimaryExpression) {
            call(node.jjtGetChild(0), byteCode);
        }else{
            throw new NotImplementedException();
        }
    }

    protected void returnStatement(Node child, ByteCode byteCode) throws CompilerException,ReturnException {
        if (child != null){
            child = simplifyExpression(child);
            List<Instruction> ifInstructions = expression(child, byteCode);

            if (isConditionalExpression(child)) {
                convertConditionalExpressionToBoolean(child, ifInstructions, byteCode);
            }
        }

        throw new ReturnException(child);
    }

    protected void debugStatement(Node child, ByteCode byteCode) throws CompilerException,ReturnException {
        byteCode.addInstruction(new Instruction(InstructionSet.Breakpoint));
    }

    protected void fieldAssignment(Node left, Node right, ByteCode byteCode) throws CompilerException {
        right = simplifyExpression(right);

        int childNumber = left.jjtGetNumChildren();

        if (childNumber > 1){
            List<Node> objects = new ArrayList<>();

            for (int i = 0; i < left.jjtGetNumChildren(); i++){
                Node child = left.jjtGetChild(i);

                //First is always object variable
                if (i == 0) {
                    //TODO: Can be also this or super
                    String name = ((ASTName) child).jjtGetValue().toString();
                    Type type = byteCode.getTypeOfLocalVariable(name);
                    if (type.isReference()) {
                        variable((ASTName) child, byteCode);
                    } else {
                        throw new CompilerException("Trying to set field on non-object '" + name + "'");
                    }

                //Middle is normal field
                }else if (i < childNumber - 1){
                    getField((ASTName)child, byteCode);
                //Last is field we want to set
                }else{
                    //Run expression
                    List<Instruction> ifInstructions = expression(right, byteCode);

                    //If the expression is a condition we have to evaluate it
                    if (isConditionalExpression(right)){
                        //Converts cond expression to actual boolean instruction
                        convertConditionalExpressionToBoolean(right, ifInstructions, byteCode);
                    }

                    putField((ASTName)child, byteCode);
                }
            }


        }else{
            throw new CompilerException("Expected field assignment");
        }
    }

    protected void variableAssignment(Node left, Node right, ByteCode byteCode) throws CompilerException {

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

                Type rightType = getTypeForExpression(right, byteCode);
                typeCheck(type, rightType);

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

    protected Type getTypeForExpression(Node value, ByteCode byteCode) throws CompilerException{
        if (isConditionalExpression(value) || isBooleanLiteral(value)){
            return Type.Boolean();
            //TODO: Additive might be also for Strings in future
        }else if (isNumberLiteral(value) || isAdditiveExpression(value) || isMultiplicativeExpression(value)){
            return Type.Number();
        }else if (isVariable(value)){
            String rightName = ((ASTName)value).jjtGetValue().toString();

            int rightPosition = byteCode.getPositionOfLocalVariable(rightName);
            if (rightPosition == -1){
                throw new CompilerException("Variable '" + rightName + "' is undeclared");
            }
            return byteCode.getTypeOfLocalVariable(rightName);
        }else if (isAllocationExpression(value) || isThis(value) || isSuper(value)) {
            //Any reference (Might need fixing)
            return Type.Reference("");
        }else if (isCallExpression(value)){
            return null;
            //TODO: we don't know return type of the method
        }else if (isFieldExpression(value)){
            return null;
            //TODO: we don't know type of field
        }else{
            throw new NotImplementedException();
        }
    }

    protected void typeCheck(Type type, Type valueType) throws TypeException, CompilerException{
        //We are not able to determine the type (Method call)
        if (valueType == null){
            return;
        }

        //Don't type control when it's both references (They can inherit from each other)
        if (type.isReference() && valueType.isReference()) {
            return;
        }

        if (valueType != type){
            throw new TypeException("Trying to assign '" + valueType  + "' to '" + type + "')");
        }
    }

    protected void call(Node node, ByteCode bytecode) throws CompilerException{
        node = simplifyExpression(node);

        if (!isCallExpression(node) || node.jjtGetNumChildren() <= 1){
            throw new CompilerException("Expected method call");
        }

        // This / Super / Name
        Node caller = (Node) node.jjtGetChild(0);

        //Method name is one before last
        ASTName method = (ASTName)node.jjtGetChild(node.jjtGetNumChildren() - 2);
        String methodName = method.jjtGetValue().toString().toLowerCase();

        //Arguments are last
        ASTArguments args = (ASTArguments)node.jjtGetChild(node.jjtGetNumChildren() - 1);

        //Push arguments onto stack in reverse order
        arguments(args, bytecode);

        //Evaluate the caller
        //It is simple method call, it has to be called on This
        if (node.jjtGetNumChildren() == 2){
            thisReference(bytecode);
        }else{
            //TODO: Super
            variable((ASTName) caller, bytecode);
        }

        //Load fields (if any)
        for (int i=1; i<node.jjtGetNumChildren()-2; i++) {
            ASTName field = (ASTName) node.jjtGetChild(i);
            getField(field, bytecode);
        }


        bytecode.addInstruction(new Instruction(InstructionSet.InvokeVirtual, methodName));

        return;
    }

    protected void fields(Node node, ByteCode bytecode) throws CompilerException{
        Node first = node.jjtGetChild(0);

        if (isVariable(first) || isThis(first) || isSuper(first)) {
            //TODO: type check
            expression(node.jjtGetChild(0), bytecode);

            //Load fields (if any)
            for (int i=1; i<node.jjtGetNumChildren(); i++) {
                ASTName field = (ASTName) node.jjtGetChild(i);
                getField(field, bytecode);
            }
        }else{
            throw new CompilerException("Expected variable, this or super");
        }
    }



    protected void arguments(Node node, ByteCode byteCode) throws CompilerException {
        //Put arguments on stack in reverse order
        for (int i=node.jjtGetNumChildren()-1; i>=0; i--) {
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

        //Go through all goto instruction and setBytes them to the end
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

        //If we success setBytes variable to TRUE
        byteCode.addInstruction(new Instruction(InstructionSet.PushInteger, "1"));
        byteCode.addInstruction(new Instruction(InstructionSet.GoTo, Integer.toString(byteCode.getLastInstructionPosition()+3)));
        //Else setBytes it to false
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
            thisReference(byteCode);
        }else if (isNumberLiteral(node)) {
            String value = ((ASTNumberLiteral) node).jjtGetValue().toString();
            byteCode.addInstruction(new Instruction(InstructionSet.PushInteger, value));
        }else if (isCallExpression(node)){
            call(node, byteCode);
        }else if (isFieldExpression(node)){
            fields(node, byteCode);
        }else{
            throw new NotImplementedException();
        }

        return null;
    }

    protected void thisReference(ByteCode byteCode){
        int position = byteCode.getPositionOfLocalVariable(THIS_VARIABLE);
        byteCode.addInstruction(new Instruction(InstructionSet.LoadReference, Integer.toString(position)));
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

    protected void getField(ASTName node, ByteCode byteCode) throws CompilerException {
        String name =  node.jjtGetValue().toString().toLowerCase();
        byteCode.addInstruction(new Instruction(InstructionSet.GetField, name));
    }

    protected void putField(ASTName node, ByteCode byteCode) throws CompilerException {
        String name =  node.jjtGetValue().toString().toLowerCase();
        byteCode.addInstruction(new Instruction(InstructionSet.PutField, name));
    }

    protected void allocationExpression(ASTAllocationExpression node, ByteCode byteCode) throws CompilerException {
        String name = ((ASTName)node.jjtGetChild(0)).jjtGetValue().toString().toLowerCase();

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
                variableAssignment(nameNode, value, byteCode);
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
        if (node instanceof ASTPrimaryExpression){
            Node last = node.jjtGetChild(node.jjtGetNumChildren() -1);
            return last instanceof ASTArguments;
        }

        return false;
    }

    boolean isFieldExpression(Node node){
        return  (node instanceof ASTPrimaryExpression) && !isCallExpression(node);
    }

    boolean isThis(Node node){
        return node instanceof ASTThis;
    }

    boolean isSuper(Node node){
        return node instanceof ASTSuper;
    }
}

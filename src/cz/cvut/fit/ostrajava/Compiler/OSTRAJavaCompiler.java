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
    protected ConstantPool constantPool;

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

        constantPool = new ConstantPool();

        String extending = node.getExtending();

        Class aClass = new Class(node.getName().toLowerCase(), (extending != null) ? extending.toLowerCase() : null);
        List<Field> fields = new ArrayList<>();

        for (int i=0; i<node.jjtGetNumChildren(); i++){
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTFieldDeclaration){
                fields.addAll(fieldDeclaration((ASTFieldDeclaration)child));
            }else if (child instanceof ASTMethodDeclaration){
                Method method = methodDeclaration((ASTMethodDeclaration)child, node.getName());
                aClass.addMethod(method);
            }
        }

        aClass.setConstantPool(constantPool);
        aClass.setFields(fields);
        return aClass;
    }

    protected List<Field> fieldDeclaration(ASTFieldDeclaration node) throws CompilerException {
        Type type;

        //First child is Type
        type = type((ASTType)node.jjtGetChild(0));
        List<Field> fields = new ArrayList<>();

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
        MethodCompilation compilation = new MethodCompilation();
        compilation.setByteCode(byteCode);

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
                compilation.addLocalVariable(THIS_VARIABLE, Type.Reference(className));

                //Add the rest of arguments
                ASTFormalParameters params = (ASTFormalParameters)child.jjtGetChild(0);
                args = formalParameters(params, compilation);
            }else if (child instanceof ASTBlock){
                try{
                    methodBlock((ASTBlock) child, name,returnType, compilation);
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
                            Type valueType = getTypeForExpression(value, compilation);
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

        method.setLocalVariablesCount(compilation.getNumberOfLocalVariables());
        method.setByteCode(byteCode);

        return method;
    }

    protected List<Type> formalParameters(ASTFormalParameters node, MethodCompilation compilation) throws CompilerException {
        List<Type> args = new ArrayList<>();

        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            ASTFormalParameter param = (ASTFormalParameter)node.jjtGetChild(i);
            Type type = type((ASTType) param.jjtGetChild(0));

            String name = ((ASTVariable) param.jjtGetChild(1)).jjtGetValue().toString();

            args.add(type);
            compilation.addLocalVariable(name, type);

            //We conclude that the arguments are initialized
            Variable var = compilation.getLocalVariable(name);
            var.setInitialized(true);
        }

        return args;
    }

    protected void methodBlock(ASTBlock node, String name, Type returnType, MethodCompilation compilation) throws CompilerException,ReturnException {
        try{
            block(node, compilation);
            //On the end of a method is always empty return
            returnStatement(null, compilation);
        } catch (ReturnException e) {
            Node value = e.getValue();
            if (value == null){
                if (returnType != Type.Void()){
                    throw new CompilerException("Method '" + name + "' must return non-void value");
                }

                compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnVoid));
            }else{
                try {
                    Type valueType = getTypeForExpression(value, compilation);
                    typeCheck(returnType, valueType);

                    if (returnType == Type.Number() || returnType == Type.Boolean()){
                        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnInteger));
                    }else if (returnType.isReference()){
                        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnReference));
                    }else{
                        throw new NotImplementedException();
                    }
                }catch(TypeException te){
                    throw new CompilerException("Returning incompatible type in method '" + name + "': " + te.getMessage());
                }
            }
        }
    }

    protected void block(ASTBlock node, MethodCompilation compilation) throws CompilerException,ReturnException {
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                Node child = node.jjtGetChild(i);
                if (child instanceof ASTLocalVariableDeclaration) {
                    localVariableDeclaration((ASTLocalVariableDeclaration) child, compilation);
                } else if (child instanceof ASTStatement) {
                    statement((ASTStatement) child, compilation);
                }
            }
    }

    protected void statement(ASTStatement node, MethodCompilation compilation) throws CompilerException, ReturnException {
        Node child = node.jjtGetChild(0);

            //Everything written with a 'pyco' in the end
            if (child instanceof ASTStatementExpression){
                statementExpression((ASTStatementExpression) child, compilation);
            }else if (child instanceof ASTIfStatement){
                ifStatement((ASTIfStatement) child, compilation);
            }else if (child instanceof ASTBlock){
                throw new NotImplementedException();
            }else if (child instanceof ASTPrintStatement){
                throw new NotImplementedException();
            }else if (child instanceof ASTReturnStatement) {
                returnStatement(child, compilation);
            }else if (child instanceof ASTDebugStatement) {
                    debugStatement(compilation);
            }else{
                throw new NotImplementedException();
            }
    }

    protected void statementExpression(ASTStatementExpression node, MethodCompilation compilation) throws CompilerException {
        Node child = node.jjtGetChild(0);

        //It's an variableAssignment
        if (child instanceof ASTAssignment){
            Node assignmentNode = node.jjtGetChild(0);

            //Assignee
            Node left = assignmentNode.jjtGetChild(0);

            //Expression
            Node right = assignmentNode.jjtGetChild(1);

            if (left.jjtGetNumChildren() == 1){
                variableAssignment(left.jjtGetChild(0), right, compilation);
            }else{
                fieldAssignment(left, right, compilation);
            }

        //It's a call
        }else if (child instanceof ASTPrimaryExpression) {
            call(node.jjtGetChild(0), compilation);
        }else{
            throw new NotImplementedException();
        }
    }

    protected void returnStatement(Node child, MethodCompilation compilation) throws CompilerException,ReturnException {
        if (child != null){
            child = simplifyExpression(child);
            List<Instruction> ifInstructions = expression(child, compilation);

            if (isConditionalExpression(child)) {
                convertConditionalExpressionToBoolean(child, ifInstructions, compilation);
            }
        }

        throw new ReturnException(child);
    }

    protected void debugStatement(MethodCompilation compilation) throws CompilerException,ReturnException {
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.Breakpoint));
    }

    protected void fieldAssignment(Node left, Node right, MethodCompilation compilation) throws CompilerException {
        right = simplifyExpression(right);

        int childNumber = left.jjtGetNumChildren();

        if (childNumber > 1){

            for (int i = 0; i < left.jjtGetNumChildren(); i++){
                Node child = left.jjtGetChild(i);

                //First is always object variable
                if (i == 0) {
                    //TODO: Can be also this or super
                    String name = ((ASTName) child).jjtGetValue().toString();
                    Type type = compilation.getTypeOfLocalVariable(name);
                    if (type.isReference()) {
                        variable((ASTName) child, compilation);
                    } else {
                        throw new CompilerException("Trying to set field on non-object '" + name + "'");
                    }

                //Middle is normal field
                }else if (i < childNumber - 1){
                    getField((ASTName)child, compilation);
                //Last is field we want to set
                }else{
                    //Run expression
                    List<Instruction> ifInstructions = expression(right, compilation);

                    //If the expression is a condition we have to evaluate it
                    if (isConditionalExpression(right)){
                        //Converts cond expression to actual boolean instruction
                        convertConditionalExpressionToBoolean(right, ifInstructions, compilation);
                    }

                    putField((ASTName)child, compilation);
                }
            }


        }else{
            throw new CompilerException("Expected field assignment");
        }
    }

    protected void variableAssignment(Node left, Node right, MethodCompilation compilation) throws CompilerException {

        right = simplifyExpression(right);

        //We are assigning to a variable
        if (isVariable(left)) {

            String name = ((ASTName) left).jjtGetValue().toString();
            int position = compilation.getPositionOfLocalVariable(name);

            if (position == -1){
                throw new CompilerException("Trying to assign to an undeclared variable '" + name + "'");
            }

            //Initialize the variable
            Variable var = compilation.getLocalVariable(name);


            Type type = compilation.getTypeOfLocalVariable(name);

            try {

                Type rightType = getTypeForExpression(right, compilation);
                typeCheck(type, rightType);

                //Run expression
                List<Instruction> ifInstructions = expression(right, compilation);

                //If the expression is a condition we have to evaluate it
                if (isConditionalExpression(right)){
                    //Converts cond expression to actual boolean instruction
                    convertConditionalExpressionToBoolean(right, ifInstructions, compilation);
                }

                storeVariable(var, compilation);

            } catch (TypeException e) {
                throw new CompilerException("Type error on '" + name + "': " + e.getMessage());
            }

        }else{
            throw new NotImplementedException();
        }
    }

    protected void storeVariable(Variable var, MethodCompilation compilation){
        int variableIndex = compilation.getPositionOfLocalVariable(var.getName());
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

        compilation.getByteCode().addInstruction(new Instruction(instruction, variableIndex));

        var.setInitialized(true);
    }

    protected void loadVariable(Variable var, MethodCompilation compilation){
        int variableIndex = compilation.getPositionOfLocalVariable(var.getName());
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

        compilation.getByteCode().addInstruction(new Instruction(instruction, variableIndex));
    }

    protected Type getTypeForExpression(Node value, MethodCompilation compilation) throws CompilerException{
        if (isConditionalExpression(value) || isBooleanLiteral(value)){
            return Type.Boolean();
            //TODO: Additive might be also for Strings in future
        }else if (isNumberLiteral(value) || isAdditiveExpression(value) || isMultiplicativeExpression(value)){
            return Type.Number();
        }else if (isVariable(value)){
            String rightName = ((ASTName)value).jjtGetValue().toString();

            int rightPosition = compilation.getPositionOfLocalVariable(rightName);
            if (rightPosition == -1){
                throw new CompilerException("Variable '" + rightName + "' is undeclared");
            }
            return compilation.getTypeOfLocalVariable(rightName);
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

    protected void call(Node node, MethodCompilation compilation) throws CompilerException{
        node = simplifyExpression(node);

        if (!isCallExpression(node) || node.jjtGetNumChildren() <= 1){
            throw new CompilerException("Expected method call");
        }

        // This / Super / Name
        Node caller = node.jjtGetChild(0);

        //Method name is one before last
        ASTName method = (ASTName)node.jjtGetChild(node.jjtGetNumChildren() - 2);
        String methodName = method.jjtGetValue().toString().toLowerCase();

        //Arguments are last
        ASTArguments args = (ASTArguments)node.jjtGetChild(node.jjtGetNumChildren() - 1);

        //Push arguments onto stack in reverse order
        arguments(args, compilation);

        //Evaluate the caller
        //It is simple method call, it has to be called on This
        if (node.jjtGetNumChildren() == 2){
            thisReference(compilation);
        }else{
            //TODO: Super
            variable((ASTName) caller, compilation);
        }

        //Load fields (if any)
        for (int i=1; i<node.jjtGetNumChildren()-2; i++) {
            ASTName field = (ASTName) node.jjtGetChild(i);
            getField(field, compilation);
        }

        int index = constantPool.addConstant(methodName);
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.InvokeVirtual, index));
    }

    protected void fields(Node node, MethodCompilation compilation) throws CompilerException{
        Node first = node.jjtGetChild(0);

        if (isVariable(first) || isThis(first) || isSuper(first)) {
            //TODO: type check
            expression(node.jjtGetChild(0), compilation);

            //Load fields (if any)
            for (int i=1; i<node.jjtGetNumChildren(); i++) {
                ASTName field = (ASTName) node.jjtGetChild(i);
                getField(field, compilation);
            }
        }else{
            throw new CompilerException("Expected variable, this or super");
        }
    }



    protected void arguments(Node node, MethodCompilation compilation) throws CompilerException {
        //Put arguments on stack in reverse order
        for (int i=node.jjtGetNumChildren()-1; i>=0; i--) {
            Node child = node.jjtGetChild(i);
            child = simplifyExpression(child);
            List<Instruction> instructions = expression(child, compilation);

            //Make value from cond-expression
            if (isConditionalExpression(child)){
                convertConditionalExpressionToBoolean(child, instructions, compilation);
            }
        }
    }



    protected void ifStatement(ASTIfStatement node, MethodCompilation compilation) throws CompilerException,ReturnException {

        List<Instruction> gotoInstructions = new ArrayList<>();

        //The conditions
        for (int i=0; i<node.jjtGetNumChildren(); i+=2) {
            //Else Statement
            if (i == node.jjtGetNumChildren()-1){
                ASTBlock block = (ASTBlock) node.jjtGetChild(i);
                block(block, compilation);
                //If or else-if statement
            }else{
                Node child = simplifyExpression(node.jjtGetChild(i));

                //If-expression skip block instructions
                List<Instruction> endBlockInstructions = ifExpression(child, compilation);
                evaluateIfExpression(child, compilation.getByteCode().getLastInstruction());


                ASTBlock block = (ASTBlock) node.jjtGetChild(i+1);
                block(block, compilation);

                //This creates goto instruction on the end of the block which leads to the end of branching
                Instruction gotoInstruction = compilation.getByteCode().addInstruction(new Instruction(InstructionSet.GoTo, -1));
                gotoInstructions.add(gotoInstruction);

                //Change the compare instr. so it points to the end of the block
                for (Instruction ebi: endBlockInstructions) {
                    ebi.setOperand(0, compilation.getByteCode().getLastInstructionPosition() + 1);
                }
            }

        }

        //Go through all goto instruction and setBytes them to the end
        for (Instruction i : gotoInstructions){
            i.setOperand(0, compilation.getByteCode().getLastInstructionPosition() + 1);
        }

    }


    protected void evaluateIfExpression(Node node, Instruction lastInstruction) throws CompilerException {
        //When it's single if expression we have to invert last member
        if (isRelationalExpression(node) || isEqualityExpression(node)){
            lastInstruction.invert();
        }
    }

    protected void convertConditionalExpressionToBoolean(Node node, List<Instruction> ifInstructions, MethodCompilation compilation) throws  CompilerException{

        //Negate the last if
        evaluateIfExpression(node, compilation.getByteCode().getLastInstruction());

        //If we success setBytes variable to TRUE
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, 1));
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.GoTo, compilation.getByteCode().getLastInstructionPosition()+3));
        //Else setBytes it to false
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, 0));

        //Set end block instructions to go to false
        for (Instruction instr: ifInstructions) {
            instr.setOperand(0, compilation.getByteCode().getLastInstructionPosition());
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

    protected List<Instruction> expression(Node node, MethodCompilation compilation) throws CompilerException{
        if (isConditionalExpression(node)) {
            return ifExpression(node, compilation);
        }else if (isArithmeticExpression(node)) {
            arithmeticExpression(node, compilation);
        }else if (isAllocationExpression(node)) {
            allocationExpression((ASTAllocationExpression)node, compilation);
        }else if (isVariable(node)) {
            variable((ASTName) node, compilation);
        }else if (isThis(node)){
            thisReference(compilation);
        }else if (isNumberLiteral(node)) {
            String value = ((ASTNumberLiteral) node).jjtGetValue().toString();
            compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, Integer.parseInt(value)));
        }else if (isCallExpression(node)){
            call(node, compilation);
        }else if (isFieldExpression(node)){
            fields(node, compilation);
        }else{
            throw new NotImplementedException();
        }

        return null;
    }

    protected void thisReference(MethodCompilation compilation){
        int position = compilation.getPositionOfLocalVariable(THIS_VARIABLE);
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.LoadReference, position));
    }

    protected void variable(ASTName node, MethodCompilation compilation) throws CompilerException{
        String name =  node.jjtGetValue().toString();
        int position = compilation.getPositionOfLocalVariable(name);

        if (position == -1) {
            throw new CompilerException("Variable '" + name + "' is not declared");
        }

        Variable var = compilation.getLocalVariable(name);

        if (!var.isInitialized()){
            throw new CompilerException("Variable '" + name + "' is not initialized");
        }

        loadVariable(var, compilation);
    }

    protected void getField(ASTName node, MethodCompilation compilation) throws CompilerException {
        String name =  node.jjtGetValue().toString().toLowerCase();
        int index = constantPool.addConstant(name);
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.GetField, index));
    }

    protected void putField(ASTName node, MethodCompilation compilation) throws CompilerException {
        String name =  node.jjtGetValue().toString().toLowerCase();
        int index = constantPool.addConstant(name);
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PutField, index));
    }

    protected void allocationExpression(ASTAllocationExpression node, MethodCompilation compilation) throws CompilerException {
        String name = ((ASTName)node.jjtGetChild(0)).jjtGetValue().toString().toLowerCase();

        //TODO: add invocation of constructor

        int index = constantPool.addConstant(name);
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.New, index));
    }

    protected List<Instruction> ifExpression(Node node, MethodCompilation compilation) throws CompilerException {
        if (isEqualityExpression(node)|| isRelationalExpression(node)){
            return compareExpression(node, compilation);
        }else if (isOrExpression(node)) {
            return orExpression((ASTConditionalOrExpression)node, compilation);
        }else if (isAndExpression(node)){
            return andExpression((ASTConditionalAndExpression)node, compilation);
        }else{
            throw new NotImplementedException();
        }
    }

    protected List<Instruction> compareExpression(Node node, MethodCompilation compilation) throws CompilerException {
        List<Instruction> instructions = new ArrayList<>();

        Node first = node.jjtGetChild(0);
        expression(first, compilation);

        for (int i = 1; i < node.jjtGetNumChildren(); i += 2) {
            Node operator = node.jjtGetChild(i);
            Node child = node.jjtGetChild(i + 1);

            expression(child, compilation);

            Instruction instruction = null;

            if (isEqualityExpression(node)) {
                if (operator instanceof ASTEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareEqualInteger, -1);
                } else {
                    instruction = new Instruction(InstructionSet.IfCompareNotEqualInteger, -1);
                }
            } else if (isRelationalExpression(node)) {

                if (operator instanceof ASTGreaterThanOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareGreaterThanInteger, -1);
                } else if (operator instanceof ASTGreaterThanOrEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareGreaterThanOrEqualInteger, -1);
                } else if (operator instanceof ASTLessThanOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareLessThanInteger, -1);
                } else if (operator instanceof ASTLessThanOrEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareLessThanOrEqualInteger, -1);
                }
            }

            if (instruction == null){
                throw new NotImplementedException();
            }

            instructions.add(instruction);
            compilation.getByteCode().addInstruction(instruction);
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

    protected List<Instruction> orExpression(ASTConditionalOrExpression node, MethodCompilation compilation) throws CompilerException {

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

            List<Instruction> childInstructions = ifExpression(child, compilation);

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
                            instruction.setOperand(0, compilation.getByteCode().getLastInstructionPosition() + 1);
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
                instruction.setOperand(0, compilation.getByteCode().getLastInstructionPosition() + 1);
            } else {
                //Invert last instruction and send it to the end
                instruction.invert();
                endBlockInstruction.add(instruction);
            }

        }

        return endBlockInstruction;
    }

    protected List<Instruction> andExpression(ASTConditionalAndExpression node, MethodCompilation compilation) throws CompilerException {
        List<Instruction> instructions = new ArrayList<>();

        //Instruction that will skip the execution block if passed
        List<Instruction> endBlockInstruction = new ArrayList<>();

        //Merge together same conditionals for easier computation (e.g. ( x and ( y and z) )
        List<Node> children = mergeConditionals(node);

        for (int i = 0; i < children.size(); i += 2) {
            Node child = children.get(i);
            List<Instruction> childInstructions = ifExpression(child, compilation);

            //In nested AND expression
            if (isOrExpression(child)){
                endBlockInstruction.addAll(childInstructions);
            } else {
                instructions.addAll(childInstructions);
            }

        }

        for (Instruction instruction : instructions) {
            //Invert instruction and send it to end block
            instruction.invert();
            endBlockInstruction.add(instruction);
        }

        return endBlockInstruction;
    }

    protected void arithmeticExpression(Node node, MethodCompilation compilation) throws CompilerException {
        Node first = node.jjtGetChild(0);
        expression(first, compilation);

        for (int i = 1; i < node.jjtGetNumChildren(); i += 2) {
            Node operator = node.jjtGetChild(i);
            Node child = node.jjtGetChild(i + 1);

            expression(child, compilation);

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

            compilation.getByteCode().addInstruction(instruction);
        }

    }


    protected void localVariableDeclaration(ASTLocalVariableDeclaration node, MethodCompilation compilation) throws CompilerException {
        //First child is Type
        Type type = type((ASTType) node.jjtGetChild(0));

        //Second and others (There can be more fields declared) are names
        for (int i=1; i<node.jjtGetNumChildren(); i++) {
            ASTVariableDeclarator declarator = (ASTVariableDeclarator) node.jjtGetChild(i);

            ASTName nameNode = ((ASTName) declarator.jjtGetChild(0));
            String name = nameNode.jjtGetValue().toString();

            int position = compilation.addLocalVariable(name, type);

            if (position == -1) {
                throw new CompilerException("Variable '" + name + "' has been already declared");
            }

            //We also assigned value
            if (declarator.jjtGetNumChildren() > 1) {
                Node value =  declarator.jjtGetChild(1);
                variableAssignment(nameNode, value, compilation);
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

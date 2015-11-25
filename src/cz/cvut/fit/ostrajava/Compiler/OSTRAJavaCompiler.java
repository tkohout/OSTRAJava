package cz.cvut.fit.ostrajava.Compiler;

import com.sun.tools.corba.se.idl.constExpr.Not;
import cz.cvut.fit.ostrajava.Interpreter.ClassPool;
import cz.cvut.fit.ostrajava.Interpreter.LookupException;
import cz.cvut.fit.ostrajava.Parser.*;
import cz.cvut.fit.ostrajava.Type.Array;
import cz.cvut.fit.ostrajava.Type.Reference;
import cz.cvut.fit.ostrajava.Type.Type;
import cz.cvut.fit.ostrajava.Type.Types;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.lang.model.type.PrimitiveType;
import java.util.*;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class OSTRAJavaCompiler {
    enum Mode {
        PRECOMPILE, COMPILE
    }

    final String THIS_VARIABLE = "joch";
    final String STRING_CLASS = Types.String().toString();

    protected ConstantPool constantPool;
    protected ClassPool classPool;
    protected Class currentClass;
    protected Mode mode;



    //In precompilation we just go trough declarations
    public List<Class> precompile(Node node) throws CompilerException {
        this.mode = Mode.PRECOMPILE;
        reset();
        return run(node);
    }

    //In compilation we go through all bytecode
    public List<Class> compile(Node node, ClassPool classPool) throws CompilerException {
        this.mode = Mode.COMPILE;
        reset();
        this.classPool = classPool;
        return run(node);
    }


    protected void reset(){
        this.constantPool = null;
        this.classPool = null;
        this.currentClass = null;
    }


    protected List<Class> run(Node node) throws CompilerException {
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
        String className = node.getName().toLowerCase();
        String extending = node.getExtending();

        Class aClass = null;

        if (mode == Mode.COMPILE){
            try {
                aClass = this.classPool.lookupClass(className);
            } catch (LookupException e) {
                e.printStackTrace();
            }

            this.constantPool = new ConstantPool();
            aClass.setConstantPool(this.constantPool);
        }else{
            aClass = new Class();
            aClass.setClassName(className);
            aClass.setSuperName((extending != null) ? extending.toLowerCase() : null);
        }

        currentClass = aClass;
        List<Field> fields = new ArrayList<>();

        for (int i=0; i<node.jjtGetNumChildren(); i++){
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTFieldDeclaration){

                fields.addAll(fieldDeclaration((ASTFieldDeclaration) child));

            }else if (child instanceof ASTMethodDeclaration){
                methodDeclaration((ASTMethodDeclaration)child, aClass);
            }
        }

        //Set the fields only once in precompilation
        if (mode == Mode.PRECOMPILE) {
            aClass.setFields(fields);
        }
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
            type = Types.Boolean();
        }else if (typeNode instanceof  ASTNumber){
            type = Types.Number();
        }else if (typeNode instanceof  ASTChar){
            type = Types.Char();
        }else if (typeNode instanceof  ASTString){
            type = Types.String();
        }else if (typeNode instanceof  ASTName) {
            String className = (String) ((ASTName) typeNode).jjtGetValue();
            type = Types.Reference(className);
        }else{
            throw new CompilerException("Unexpected type of field " + typeNode );
        }

        //name[]
        if (node.jjtGetNumChildren() == 2) {
            type = Types.Array(type);
        }

        return type;
    }

    protected void methodDeclaration(ASTMethodDeclaration node, Class aClass) throws CompilerException {
        Type returnType = Types.Void();
        String name = null;
        List<Type> args = new ArrayList<>();
        ByteCode byteCode = new ByteCode();
        MethodCompilation compilation = new MethodCompilation();
        compilation.setByteCode(byteCode);
        boolean isStatic = false;

        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);

            if (child instanceof ASTStatic) {
                isStatic = true;
            }else if (child instanceof ASTResultType){
                ASTResultType resultType = ((ASTResultType) child);
                if (resultType.jjtGetNumChildren() != 0){
                    returnType = type((ASTType)resultType.jjtGetChild(0));
                }
            }else if (child instanceof ASTMethod){

                name = ((ASTMethod) child).jjtGetValue().toString().toLowerCase();

                //Add This as first argument
                compilation.addLocalVariable(THIS_VARIABLE, Types.Reference(aClass.getClassName()));

                //Add the rest of arguments
                ASTFormalParameters params = (ASTFormalParameters)child.jjtGetChild(0);
                args = formalParameters(params, compilation);
            }else if (child instanceof ASTBlock){
                if (mode == Mode.COMPILE) {
                    methodBlock((ASTBlock) child, name, returnType, compilation);
                }
            }
        }

        if (name == null){
            throw new CompilerException("Missing method name in " + node );
        }

        Method method = new Method(name, args, returnType);


        if (mode == Mode.COMPILE) {
            //Find already declared method
            for (Method m : aClass.getMethods()) {
                if (m.getDescriptor().equals(method.getDescriptor())) {
                    method = m;
                    break;
                }
            }
        }else{
            aClass.addMethod(method);
        }

        method.setStaticMethod(isStatic);
        method.setLocalVariablesCount(compilation.getNumberOfLocalVariables());
        method.setByteCode(byteCode);
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

    protected void methodBlock(ASTBlock node, String name, Type returnType, MethodCompilation compilation) throws CompilerException {
        try{
            block(node, compilation);
            //On the end of a method is always empty return
            returnStatement(null, compilation);
        } catch (ReturnException e) {
            Node value = e.getValue();
            if (value == null){
                if (returnType != Types.Void()){
                    throw new CompilerException("Method '" + name + "' must return non-void value");
                }

                compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnVoid));
            }else{
                try {
                    Type valueType = getTypeForExpression(value, compilation);
                    typeCheck(returnType, valueType);

                    if (returnType == Types.Number() || returnType == Types.Boolean() || returnType == Types.Char()){
                        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnInteger));
                    }else if (returnType instanceof Reference){
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

            if (left.jjtGetNumChildren() == 1 || (CompilerTypes.isArray(left) && left.jjtGetNumChildren() == 2)) {
                variableAssignment(left, right, compilation);
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
            evaluateExpression(child, compilation);
        }

        throw new ReturnException(child);
    }

    protected void debugStatement(MethodCompilation compilation) throws CompilerException,ReturnException {
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.Breakpoint));
    }

    protected void fieldAssignment(Node left, Node right, MethodCompilation compilation) throws CompilerException {
        right = simplifyExpression(right);
        boolean isArray = CompilerTypes.isArray(left);

        int childNumber = left.jjtGetNumChildren();
        //Last member is [..Expression..]
        int lastField = (isArray) ? childNumber - 2 : childNumber - 1;
        if (childNumber > 1){

            for (int i = 0; i < lastField+1; i++){
                Node child = left.jjtGetChild(i);

                //First is always object variable/this/super
                if (i == 0) {

                    Type type = getTypeForExpression(child,compilation);

                    if (type instanceof Reference) {
                        expression(child, compilation);
                    } else {
                        throw new CompilerException("Trying to set field on non-object ");
                    }

                //Middle is normal field
                }else if (i < lastField){
                    getField((ASTName)child, compilation);
                //Last is field we want to set
                }else{
                    //Run expression we are assigning
                    evaluateExpression(right, compilation);

                    if (isArray){
                        //Push array index on stack
                        Node arrayIndexExpression = simplifyExpression(left.jjtGetChild(childNumber-1).jjtGetChild(0));
                        evaluateExpression(arrayIndexExpression, compilation);

                        //Load array reference on the stack
                        getField((ASTName)child, compilation);

                        storeArray(compilation);
                    }else {
                        putField((ASTName) child, compilation);
                    }
                }
            }


        }else{
            throw new CompilerException("Expected field assignment");
        }
    }



    protected void variableAssignment(Node left, Node right, MethodCompilation compilation) throws CompilerException {
            boolean isArray = false;

            if (CompilerTypes.isArray(left)){
                isArray = true;
            }

            right = simplifyExpression(right);
            Node variable = left.jjtGetChild(0);

            String name = ((ASTName) variable).jjtGetValue().toString();
            int position = compilation.getPositionOfLocalVariable(name);

            if (position == -1){
                throw new CompilerException("Trying to assign to an undeclared variable '" + name + "'");
            }

            //Initialize the variable
            Variable var = compilation.getLocalVariable(name);
            Type type = var.getType();

            if (isArray){
                //Set type for type check as a single element of array
                if (type instanceof Array) {
                    type = ((Array) type).getElement();
                }else{
                    throw new CompilerException("Trying to access index in non-array");
                }
            }

            try {
                Type rightType = getTypeForExpression(right, compilation);
                typeCheck(type, rightType);

                //Evaluate and put value on stack
                evaluateExpression(right, compilation);

                if (isArray){
                    //Push array index on stack
                    Node arrayIndexExpression = simplifyExpression(left.jjtGetChild(1).jjtGetChild(0));
                    evaluateExpression(arrayIndexExpression, compilation);

                    //Load array reference on the stack
                    variable((ASTName) left.jjtGetChild(0), compilation);

                    storeArray(compilation);
                }else {
                    storeVariable(var, compilation);
                }

            } catch (TypeException e) {
                throw new CompilerException("Type error on '" + name + "': " + e.getMessage());
            }

    }

    protected void evaluateExpression(Node expression, MethodCompilation compilation) throws CompilerException {
        //Run expression
        List<Instruction> ifInstructions = expression(expression, compilation);

        //If the expression is a condition we have to evaluate it
        if (CompilerTypes.isConditionalExpression(expression)){
            //Converts cond expression to actual boolean instruction
            convertConditionalExpressionToBoolean(expression, ifInstructions, compilation);
        }
    }

    protected void storeArray(MethodCompilation compilation){
        // Now on stack: Value -> Index -> Array Ref
        //TODO: Other primitives
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.StoreIntegerArray));
    }

    protected void loadArray(MethodCompilation compilation){
        // Now on stack:  Index -> Array Ref
        //TODO: Other primitives
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.LoadIntegerArray));
    }

    protected void storeVariable(Variable var, MethodCompilation compilation){
        int variableIndex = compilation.getPositionOfLocalVariable(var.getName());
        Type type = var.getType();

        InstructionSet instruction;

        if (type instanceof Reference){
            instruction = InstructionSet.StoreReference;
        }else if (type == Types.Number()){
            instruction = InstructionSet.StoreInteger;
        }else if (type == Types.Boolean()) {
            instruction = InstructionSet.StoreInteger;
        }else if (type == Types.Char()) {
            instruction = InstructionSet.StoreInteger;
        }else if (type instanceof Array){
            instruction = InstructionSet.StoreReference;
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

        if (type instanceof Reference){
            instruction = InstructionSet.LoadReference;
        }else if (type == Types.Number()){
            instruction = InstructionSet.LoadInteger;
        }else if (type == Types.Boolean()){
            instruction = InstructionSet.LoadInteger;
        }else if (type == Types.Char()){
            instruction = InstructionSet.LoadInteger;
        }else if (type instanceof Array){
            instruction = InstructionSet.LoadReference;
        }else{
            throw new NotImplementedException();
        }

        compilation.getByteCode().addInstruction(new Instruction(instruction, variableIndex));
    }

    protected Type getTypeForExpression(Node value, MethodCompilation compilation) throws CompilerException{
        if (CompilerTypes.isConditionalExpression(value) || CompilerTypes.isBooleanLiteral(value)){
            return Types.Boolean();
            //TODO: Additive might be also for Strings in future
        }else if (CompilerTypes.isNumberLiteral(value) || CompilerTypes.isAdditiveExpression(value) || CompilerTypes.isMultiplicativeExpression(value)){
            return Types.Number();
        }else if (CompilerTypes.isCharLiteral(value)){
            return Types.Char();
        }else if (CompilerTypes.isVariable(value)) {
            String rightName = ((ASTName) value).jjtGetValue().toString();

            int rightPosition = compilation.getPositionOfLocalVariable(rightName);
            if (rightPosition == -1) {
                //It's undeclared it has to be static class
                return Types.Reference(rightName);
                //throw new CompilerException("Variable '" + rightName + "' is undeclared");
            }
            return compilation.getTypeOfLocalVariable(rightName);
        }else if (CompilerTypes.isAllocationExpression(value)){
            //Array of primitives
            if (CompilerTypes.isArray(value)){
                Type elementType;
                Node element = value.jjtGetChild(0);

                if (element instanceof ASTBool){
                    elementType = Types.Boolean();
                }else if (element instanceof  ASTNumber){
                    elementType = Types.Number();
                }else if (element instanceof  ASTChar){
                    elementType = Types.Char();
                }else if (element instanceof  ASTFloat) {
                    elementType = Types.String();
                }else{
                    throw new CompilerException("Non-primitive in array type");
                }

                return Types.Array(elementType);
            }else{
                return Types.Reference("");
            }
        }else if (CompilerTypes.isThis(value) || CompilerTypes.isSuper(value)) {
            //Any reference (Might need fixing)
            return Types.Reference("");
        }else if (CompilerTypes.isCallExpression(value)){


            return null;
            //TODO: we don't know return type of the method
        }else if (CompilerTypes.isFieldExpression(value)) {
            return null;
            //TODO: we don't know type of field
        }else if (CompilerTypes.isStringLiteral(value)){
            return Types.String();
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
        if (type instanceof Reference && valueType instanceof Reference) {
            return;
        }

        if (valueType != type){
            throw new TypeException("Trying to assign '" + valueType  + "' to '" + type + "')");
        }
    }

    protected void call(Node node, MethodCompilation compilation) throws CompilerException{
        node = simplifyExpression(node);

        if (!CompilerTypes.isCallExpression(node) || node.jjtGetNumChildren() <= 1){
            throw new CompilerException("Expected method call");
        }

        // This / Super / Name
        Node caller = node.jjtGetChild(0);

        //Method name is one before last
        ASTName method = (ASTName)node.jjtGetChild(node.jjtGetNumChildren() - 2);
        String methodName = method.jjtGetValue().toString().toLowerCase();


        //Arguments are last
        ASTArguments args = (ASTArguments)node.jjtGetChild(node.jjtGetNumChildren() - 1);
        //Push arguments on the stack
        arguments(args, compilation);


        Class objectClass;

        //Evaluate the caller
        //It is simple method call, it has to be called on This
        if (node.jjtGetNumChildren() == 2){
            objectClass = currentClass;
            thisReference(compilation);
            //TODO: Super
        }else{
            Type type = getTypeForExpression(caller, compilation);
            if (!(type instanceof Reference)){
                throw new CompilerException("Trying to call method on non-object");
            }

            String className = ((Reference) type).getClassName();

            try {
                objectClass = classPool.lookupClass(className);
            } catch (LookupException e) {
                throw new CompilerException("Class '" + className  + "' not found");
            }

            int variablePosition = compilation.getPositionOfLocalVariable(((ASTName)caller).jjtGetValue().toString());

            //If it's variable. If it's not it has to be static class
            if (variablePosition != -1) {
                variable((ASTName) caller, compilation);
            }
        }

        //Load fields (if any)
        for (int i=1; i<node.jjtGetNumChildren()-2; i++) {
            ASTName field = (ASTName) node.jjtGetChild(i);
            getField(field, compilation);
        }

        //Get types of arguments (whether they are expression, variables or method call)
        List<Type> argTypes = getArgumentsTypes(args,compilation);

        invokeMethod(objectClass, methodName, argTypes, compilation);

    }

    protected void fields(Node node, MethodCompilation compilation) throws CompilerException{
        Node first = node.jjtGetChild(0);

        if (CompilerTypes.isVariable(first) || CompilerTypes.isThis(first) || CompilerTypes.isSuper(first)) {
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

            evaluateExpression(child, compilation);
        }
    }

    protected List<Type> getArgumentsTypes(Node node, MethodCompilation compilation) throws CompilerException {
        List<Type> types = new ArrayList<>();

        //Put arguments on stack in reverse order
        for (int i=node.jjtGetNumChildren()-1; i>=0; i--) {
            Node child = node.jjtGetChild(i);
            child = simplifyExpression(child);
            Type type = getTypeForExpression(child, compilation);
            types.add(type);
        }

        return types;
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
        if (CompilerTypes.isRelationalExpression(node) || CompilerTypes.isEqualityExpression(node)){
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
        if (node instanceof ASTArguments || node instanceof ASTArraySuffix){
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
        if (CompilerTypes.isConditionalExpression(node)) {
            return ifExpression(node, compilation);
        }else if (CompilerTypes.isArithmeticExpression(node)) {
            arithmeticExpression(node, compilation);
        }else if (CompilerTypes.isAllocationExpression(node)) {
            allocationExpression((ASTAllocationExpression)node, compilation);
        }else if (CompilerTypes.isVariable(node)) {
            variable((ASTName) node, compilation);
        }else if (CompilerTypes.isThis(node)){
            thisReference(compilation);
        }else if (CompilerTypes.isNumberLiteral(node)) {
            numberLiteral(node, compilation);
        }else if (CompilerTypes.isCharLiteral(node)) {
            charLiteral(node, compilation);
        }else if (CompilerTypes.isCallExpression(node)) {
            call(node, compilation);
        }else if (CompilerTypes.isArray(node)){
            array(node, compilation);
        }else if (CompilerTypes.isFieldExpression(node)) {
            fields(node, compilation);
        }else if (CompilerTypes.isStringLiteral(node)){
            stringLiteral(node, compilation);
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

    protected void array(Node node, MethodCompilation compilation) throws CompilerException{
        //Load index
        Node arrayIndexExpression = simplifyExpression(node.jjtGetChild(1).jjtGetChild(0));
        evaluateExpression(arrayIndexExpression, compilation);

        //Load variable reference on stack
        variable((ASTName)node.jjtGetChild(0), compilation);

        loadArray(compilation);
    }

    protected void numberLiteral(Node node, MethodCompilation compilation) throws CompilerException {
        String value = ((ASTNumberLiteral) node).jjtGetValue().toString();
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, Integer.parseInt(value)));
    }

    protected void charLiteral(Node node, MethodCompilation compilation) throws CompilerException {
        String value = ((ASTCharLiteral) node).jjtGetValue().toString();
        char charValue = 0;

        if (value.length() == 1) {
            charValue = value.charAt(0);
        }

        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, (int)charValue));
    }

    protected void stringLiteral(Node node, MethodCompilation compilation) throws CompilerException {
        String value = ((ASTStringLiteral) node).jjtGetValue().toString();
        //Have to trim the parenthesis
        value = value.substring(1, value.length()-1);

        int constantIndex = constantPool.addConstant(value);
        //Push value on stack, it will create array of chars
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushConstant, constantIndex));

        //Create new String
        int index = constantPool.addConstant(STRING_CLASS);
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.New, index));

        //Duplicate the object reference because invoke will pop it from stack
        //compilation.getByteCode().addInstruction(new Instruction(InstructionSet.Duplicate, index));

        //Setup constructor String(char[]) which will take characters as argument
        List<Type> argTypes = new ArrayList<>();
        argTypes.add(Types.CharArray());

        //New string constructor
        Method method = new Method(STRING_CLASS, argTypes, Types.Void());
        int constructorIndex = constantPool.addConstant(method.getDescriptor());

        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.InvokeVirtual, constructorIndex));

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
        Node child = node.jjtGetChild(0);


        //Allocation of primitives array
        if (child instanceof ASTBool || child instanceof ASTNumber || child instanceof ASTChar){
            Node expression = simplifyExpression(node.jjtGetChild(1).jjtGetChild(0));

            Type type = getTypeForExpression(expression, compilation);

            //Push size on stack
            expression(expression, compilation);

            compilation.getByteCode().addInstruction(new Instruction(InstructionSet.NewArray));

            //Allocation of object
        }else if (child instanceof ASTName) {
            String name = ((ASTName) node.jjtGetChild(0)).jjtGetValue().toString().toLowerCase();

            //Constructor arguments
            ASTArguments args = (ASTArguments) node.jjtGetChild(1);

            //Push arguments on the stack
            arguments(args, compilation);

            //Get types of arguments (whether they are expression, variables or method call)
            List<Type> argTypes = getArgumentsTypes(args,compilation);

            //Create new object and push on stack
            newObject(name, argTypes, compilation);


        }else{
            //TODO: Or float
            throw new NotImplementedException();
        }
    }

    protected void newObject(String className, List<Type> args,  MethodCompilation compilation) throws CompilerException {
        int index = constantPool.addConstant(className);
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.New, index));

        try {
            Class objClass = classPool.lookupClass(className);

            //Call constructor
            invokeMethod(objClass, className, args, compilation);

        } catch (LookupException e) {
            throw new CompilerException("Class '" + className +  "' not found");
        }
    }

    protected void invokeMethod(Class objClass, String name, List<Type> argTypes, MethodCompilation compilation) throws CompilerException {

        //Get method descriptor based on it's name, arguments and return type
        //TODO: return type?

        Method method = new Method(name, argTypes, Types.Void());
        String methodDescriptor = method.getDescriptor();

        try {
            method = objClass.lookupMethod(methodDescriptor);
        } catch (LookupException e) {
            //It can also be native method which won't be in the code
            //TODO: Do some check
            //throw new CompilerException("Method '" + methodDescriptor + "' not found in " + objClass.getClassName() );
        }


            int methodIndex = constantPool.addConstant(method.getDescriptor());

            if (method.isStaticMethod()){
                int classIndex = constantPool.addConstant(objClass.getClassName());

                compilation.getByteCode().addInstruction(new Instruction(InstructionSet.InvokeStatic, methodIndex, classIndex));
            }else {
                compilation.getByteCode().addInstruction(new Instruction(InstructionSet.InvokeVirtual, methodIndex));
            }





    }



    protected List<Instruction> ifExpression(Node node, MethodCompilation compilation) throws CompilerException {
        if (CompilerTypes.isEqualityExpression(node)|| CompilerTypes.isRelationalExpression(node)){
            return compareExpression(node, compilation);
        }else if (CompilerTypes.isOrExpression(node)) {
            return orExpression((ASTConditionalOrExpression)node, compilation);
        }else if (CompilerTypes.isAndExpression(node)){
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

            if (CompilerTypes.isEqualityExpression(node)) {
                if (operator instanceof ASTEqualOperator) {
                    instruction = new Instruction(InstructionSet.IfCompareEqualInteger, -1);
                } else {
                    instruction = new Instruction(InstructionSet.IfCompareNotEqualInteger, -1);
                }
            } else if (CompilerTypes.isRelationalExpression(node)) {

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

            if ((CompilerTypes.isOrExpression(node) && CompilerTypes.isOrExpression(child)) || (CompilerTypes.isAndExpression(node) && CompilerTypes.isAndExpression(child) ) ){
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
            if (CompilerTypes.isAndExpression(child)) {

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
            if (CompilerTypes.isOrExpression(child)){
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

            if (CompilerTypes.isAdditiveExpression(node)) {
                if (operator instanceof ASTPlusOperator) {
                    instruction = new Instruction(InstructionSet.AddInteger);
                } else {
                    instruction = new Instruction(InstructionSet.SubstractInteger);
                }
            } else if (CompilerTypes.isMultiplicativeExpression(node)) {
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
                variableAssignment(declarator, value, compilation);
            }
        }
    }




}

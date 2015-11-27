package cz.cvut.fit.ostrajava.Compiler;

import cz.cvut.fit.ostrajava.Interpreter.ClassPool;
import cz.cvut.fit.ostrajava.Interpreter.LookupException;
import cz.cvut.fit.ostrajava.Parser.*;
import cz.cvut.fit.ostrajava.Type.ArrayType;
import cz.cvut.fit.ostrajava.Type.ReferenceType;
import cz.cvut.fit.ostrajava.Type.Type;
import cz.cvut.fit.ostrajava.Type.Types;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    final String BASE_OBJECT_CLASS = "bazmek";


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

        if (extending == null){
            if (className.equals(BASE_OBJECT_CLASS)){
                extending = null;
            }else{
                extending = BASE_OBJECT_CLASS;
            }
        }else{
            extending = extending.toLowerCase();
        }


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
            aClass.setSuperName(extending);
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
            //Add generic constructor if needed
            addGenericConstructor(aClass);
        }
        return aClass;
    }

    protected void addGenericConstructor(Class aClass)  {
        //Skip if class already has constructor
        for (Method method: aClass.getMethods()){
            if (method.getName().equals(aClass.getClassName())){
                return;
            }
        }

        String className = aClass.getClassName();
        //TODO: add probably some super call

        Type classType = Types.Reference(className);

        MethodCompilation compilation = new MethodCompilation();

        //Add this variable
        compilation.addLocalVariable(THIS_VARIABLE, classType);

        //Get it's position
        int thisPosition = compilation.getPositionOfLocalVariable(THIS_VARIABLE);

        //Load This on stack and return it
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.LoadReference, thisPosition));
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnReference, thisPosition));

        //Create new method
        Method constructor = new Method(className, new ArrayList<Type>(), className, classType);
        constructor.setByteCode(compilation.getByteCode());
        constructor.setLocalVariablesCount(compilation.getNumberOfLocalVariables());

        aClass.addMethod(constructor);
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
        MethodCompilation compilation = new MethodCompilation();

        boolean isStatic = false;
        boolean isConstructor = false;

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

                //It's a constructor
                if (name.equals(aClass.getClassName())){
                    isConstructor = true;
                    //Set return type as this
                    returnType = Types.Reference(aClass.getClassName());
                }

                //Add This as first argument
                compilation.addLocalVariable(THIS_VARIABLE, Types.Reference(aClass.getClassName()));

                //Add the rest of arguments
                ASTFormalParameters params = (ASTFormalParameters)child.jjtGetChild(0);
                args = formalParameters(params, compilation);
            }else if (child instanceof ASTBlock){
                if (mode == Mode.COMPILE) {
                    methodBlock((ASTBlock) child, name, returnType, isConstructor, compilation);
                }
            }
        }

        if (name == null){
            throw new CompilerException("Missing method name in " + node );
        }

        Method method = new Method(name, args, aClass.getClassName(), returnType);


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
        method.setByteCode(compilation.getByteCode());
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

    protected void methodBlock(ASTBlock node, String name, Type returnType, boolean isConstructor, MethodCompilation compilation) throws CompilerException {
        try{
            block(node, -1, compilation);

            //Constructor always return this
            if (isConstructor) {
                returnStatement(new ASTThis(), compilation);
            //On the end of a method is always empty return
            }else{
                returnStatement(null, compilation);
            }
        } catch (ReturnException e) {
            Node value = e.getValue();
            if (value == null) {
                if (returnType != Types.Void()) {
                    throw new CompilerException("Method '" + name + "' must return non-void value");
                }

                compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnVoid));
            } else {
                try {
                    Type valueType = getTypeForExpression(value, compilation);
                    typeCheck(returnType, valueType);

                    if (returnType == Types.Number() || returnType == Types.Boolean() || returnType == Types.Char()) {
                        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnInteger));
                    } else if (returnType instanceof ReferenceType || returnType instanceof ArrayType) {
                        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.ReturnReference));
                    } else {
                        throw new NotImplementedException();
                    }
                } catch (TypeException te) {
                    throw new CompilerException("Returning incompatible type in method '" + name + "': " + te.getMessage());
                }
            }
        }
    }

    protected List<Instruction> block(ASTBlock node, int cycleStart, MethodCompilation compilation) throws CompilerException, ReturnException {
        List<Instruction> allBreakInstructions = new ArrayList<>();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
                Node child = node.jjtGetChild(i);
                if (child instanceof ASTLocalVariableDeclaration) {
                    localVariableDeclaration((ASTLocalVariableDeclaration) child, compilation);
                } else if (child instanceof ASTStatement) {
                    List<Instruction> breakInstructions = statement((ASTStatement) child, cycleStart, compilation);
                    if (breakInstructions != null) {
                        allBreakInstructions.addAll(breakInstructions);
                    }
                }
        }

        return allBreakInstructions;
    }

    protected List<Instruction> statement(ASTStatement node, int cycleStart, MethodCompilation compilation) throws CompilerException, ReturnException {
        Node child = node.jjtGetChild(0);


            //Everything written with a 'pyco' in the end
            if (child instanceof ASTStatementExpression){
                statementExpression((ASTStatementExpression) child, compilation);
            }else if (child instanceof ASTIfStatement){
                return ifStatement((ASTIfStatement) child, cycleStart, compilation);
            }else if (child instanceof ASTWhileStatement) {
                whileStatement((ASTWhileStatement) child, compilation);
            }else if (child instanceof ASTBreakStatement) {
                List<Instruction> breakInstruction = new ArrayList<>();
                breakInstruction.add(breakStatement(cycleStart, compilation));
                return breakInstruction;
            }else if (child instanceof ASTContinueStatement) {
                continueStatement(cycleStart, compilation);
            }else if (child instanceof ASTReturnStatement) {
                returnStatement(child, compilation);
            }else if (child instanceof ASTDebugStatement) {
                debugStatement(compilation);
            }else{
                throw new NotImplementedException();
            }
        return null;
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

                    if (type instanceof ReferenceType) {
                        expression(child, compilation);
                    } else {
                        throw new CompilerException("Trying to set field on non-object ");
                    }

                //Middle is normal field
                }else if (i < lastField){
                    getField((ASTName)child, compilation);
                //Last is field we want to set
                }else{


                    if (isArray){
                        //Load array reference on the stack
                        getField((ASTName) child, compilation);

                        //Push array index on stack
                        Node arrayIndexExpression = simplifyExpression(left.jjtGetChild(childNumber-1).jjtGetChild(0));
                        evaluateExpression(arrayIndexExpression, compilation);

                        //Value
                        evaluateExpression(right, compilation);

                        storeArray(compilation);
                    }else {

                        evaluateExpression(right, compilation);

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
                if (type instanceof ArrayType) {
                    type = ((ArrayType) type).getElement();
                }else{
                    throw new CompilerException("Trying to access index in non-array");
                }
            }

            try {
                Type rightType = getTypeForExpression(right, compilation);
                typeCheck(type, rightType);



                if (isArray){
                    //Load array reference on the stack
                    variable((ASTName) left.jjtGetChild(0), compilation);

                    //Push array index on stack
                    Node arrayIndexExpression = simplifyExpression(left.jjtGetChild(1).jjtGetChild(0));
                    evaluateExpression(arrayIndexExpression, compilation);

                    //Value on stack
                    evaluateExpression(right, compilation);

                    storeArray(compilation);
                }else {
                    //Evaluate and put value on stack
                    evaluateExpression(right, compilation);

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
        // Now on stack: (from furthest) Array Ref -> Index -> Value
        //TODO: Other primitives
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.StoreIntegerArray));
    }

    protected void loadArray(MethodCompilation compilation){
        // Now on stack: (from furthest) Array ->Index Ref
        //TODO: Other primitives
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.LoadIntegerArray));
    }

    protected void storeVariable(Variable var, MethodCompilation compilation){
        int variableIndex = compilation.getPositionOfLocalVariable(var.getName());
        Type type = var.getType();

        InstructionSet instruction;

        if (type instanceof ReferenceType){
            instruction = InstructionSet.StoreReference;
        }else if (type == Types.Number()){
            instruction = InstructionSet.StoreInteger;
        }else if (type == Types.Boolean()) {
            instruction = InstructionSet.StoreInteger;
        }else if (type == Types.Char()) {
            instruction = InstructionSet.StoreInteger;
        }else if (type instanceof ArrayType){
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

        if (type instanceof ReferenceType){
            instruction = InstructionSet.LoadReference;
        }else if (type == Types.Number()){
            instruction = InstructionSet.LoadInteger;
        }else if (type == Types.Boolean()){
            instruction = InstructionSet.LoadInteger;
        }else if (type == Types.Char()){
            instruction = InstructionSet.LoadInteger;
        }else if (type instanceof ArrayType){
            instruction = InstructionSet.LoadReference;
        }else{
            throw new NotImplementedException();
        }

        compilation.getByteCode().addInstruction(new Instruction(instruction, variableIndex));
    }

    protected Type getTypeForExpression(Node value, MethodCompilation compilation) throws CompilerException{
        if (CompilerTypes.isConditionalExpression(value) || CompilerTypes.isBooleanLiteral(value)){
            return Types.Boolean();
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
            //ArrayType of primitives
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
                String className = ((ASTName)value.jjtGetChild(0)).jjtGetValue().toString();
                return Types.Reference(className);
            }
        }else if (CompilerTypes.isThis(value)) {
            return Types.Reference(currentClass.getClassName());
        }else if (CompilerTypes.isSuper(value)){
            if (currentClass.getSuperClass() != null){
                return currentClass.getSuperClass().getClassType();
            }
            return null;
        }else if (CompilerTypes.isNullLiteral(value)){
            //Any reference
            return null;
        }else if (CompilerTypes.isCallExpression(value)){
            return getTypeForMethodCall(value, compilation);
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
        if (type instanceof ReferenceType && valueType instanceof ReferenceType) {
            try {
                Class valueClass = classPool.lookupClass(((ReferenceType) valueType).getClassName());
                Class typeClass = classPool.lookupClass(((ReferenceType) type).getClassName());

                if (!valueClass.inheritsFrom(typeClass)){
                    throw new CompilerException("Trying to assign not compatible types " + valueType + " to " + type);
                }else{
                    return;
                }

            } catch (LookupException e) {
                throw new CompilerException(e.getMessage());
            }
        }

        //Char and Int are the same
        if (type == Types.Number() && valueType == Types.Char() ||  type == Types.Char() && valueType == Types.Number()) {
            return;
        }

        if (valueType != type){
            throw new TypeException("Trying to assign '" + valueType  + "' to '" + type + "')");
        }
    }

    protected Type getTypeForMethodCall(Node node, MethodCompilation compilation) throws CompilerException {

        Class callerClass = null;

        try {

            //Get type for first member
            //It's method call on This
            if (node.jjtGetNumChildren() == 2){
                callerClass = currentClass;
            //It's variable or static class
            }else{
                Type classType = getTypeForExpression(node.jjtGetChild(0), compilation);

                if (classType instanceof ReferenceType) {
                    callerClass = classPool.lookupClass(((ReferenceType) classType).getClassName());
                }
            }

            //Get type for field
            for (int i=1; i<node.jjtGetNumChildren()-2; i++) {
                String fieldName = ((ASTName) node.jjtGetChild(i)).jjtGetValue().toString();

                int fieldPosition = callerClass.lookupField(fieldName);
                Field field = callerClass.getField(fieldPosition);

                Type fieldType = field.getType();

                if (fieldType instanceof ReferenceType){
                    callerClass = classPool.lookupClass(((ReferenceType) fieldType).getClassName());
                }else{
                    throw new CompilerException("Calling method on non-object field" + fieldName);
                }
            }
        } catch (LookupException e) {
            throw new CompilerException(e.getMessage());
        }

        ASTName methodNode = (ASTName)node.jjtGetChild(node.jjtGetNumChildren() - 2);
        String methodName = methodNode.jjtGetValue().toString().toLowerCase();

        ASTArguments args = (ASTArguments)node.jjtGetChild(node.jjtGetNumChildren() - 1);
        List<Type> argTypes = getArgumentsTypes(args,compilation);

        String methodDescriptor = Method.getDescriptor(methodName, argTypes, callerClass.getClassName());

        try {
            Method method = callerClass.lookupMethod(methodDescriptor, classPool);
            return method.getReturnType();
        } catch (LookupException e) {
            //TODO:it can still be native
            return null;
        }
    }

    protected void call(Node node, MethodCompilation compilation) throws CompilerException{
        boolean staticCall = false;

        node = simplifyExpression(node);

        if (!CompilerTypes.isCallExpression(node) || node.jjtGetNumChildren() <= 1){
            throw new CompilerException("Expected method call");
        }

        Node caller = node.jjtGetChild(0);


        //If it's just the method and arguments
        if (node.jjtGetNumChildren() == 2){
            //super(...) || this(...) - it's constructor call
            if (CompilerTypes.isSuper(caller) || CompilerTypes.isThis(caller)){
                constructorCall(node, compilation);
                return;
            }else {
                caller = new ASTThis();
            }
        }


        //Method name is one before last child
        ASTName method = (ASTName)node.jjtGetChild(node.jjtGetNumChildren() - 2);
        String methodName = method.jjtGetValue().toString().toLowerCase();


        //Arguments are last child
        ASTArguments args = (ASTArguments)node.jjtGetChild(node.jjtGetNumChildren() - 1);
        //Push arguments on the stack
        arguments(args, compilation);


        Class objectClass;



        Type type = getTypeForExpression(caller, compilation);

        if (!(type instanceof ReferenceType)){
            throw new CompilerException("Trying to call method on non-object");
        }

        String className = ((ReferenceType) type).getClassName();

        try {
            objectClass = classPool.lookupClass(className);
        } catch (LookupException e) {
            throw new CompilerException("Class '" + className  + "' not found");
        }


        if (CompilerTypes.isVariable(caller)){
            int variablePosition = compilation.getPositionOfLocalVariable(((ASTName)caller).jjtGetValue().toString());

            //If it's variable.
            if (variablePosition != -1) {
                variable((ASTName) caller, compilation);
            //It has to be static class
            }else{
                staticCall = true;
            }
        }else{
            //Evaluate caller
            expression(caller, compilation);
        }

        //Load fields (if any)
        for (int i=1; i<node.jjtGetNumChildren()-2; i++) {
            ASTName field = (ASTName) node.jjtGetChild(i);
            getField(field, compilation);
        }

        //Get types of arguments (whether they are expression, variables or method call)
        List<Type> argTypes = getArgumentsTypes(args,compilation);

        invokeMethod(objectClass, methodName, argTypes, compilation, staticCall);

    }

    protected void constructorCall(Node node, MethodCompilation compilation) throws CompilerException {
        Node caller = node.jjtGetChild(0);
        Node arguments = node.jjtGetChild(1);

        //Put arguments on stack
        arguments(arguments, compilation);

        //Load argument types
        List<Type> argTypes = getArgumentsTypes(arguments, compilation);

        //Get classname
        ReferenceType type = (ReferenceType)getTypeForExpression(caller, compilation);
        String className =  type.getClassName();

        //Put caller ref on stack
        expression(caller, compilation);

        invokeConstructor(className, argTypes, compilation);
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


        for (int i=0; i<node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            child = simplifyExpression(child);
            Type type = getTypeForExpression(child, compilation);
            types.add(type);
        }

        return types;
    }



    protected List<Instruction> ifStatement(ASTIfStatement node, int cycleStart, MethodCompilation compilation) throws CompilerException, ReturnException {

        List<Instruction> gotoInstructions = new ArrayList<>();
        List<Instruction> allBreakInstructions = new ArrayList<>();

        //The conditions
        for (int i = 0; i < node.jjtGetNumChildren(); i += 2) {
            //Else Statement
            if (i == node.jjtGetNumChildren() - 1) {
                ASTBlock block = (ASTBlock) node.jjtGetChild(i);
                List<Instruction> breakInstructions = block(block, cycleStart, compilation);
                allBreakInstructions.addAll(breakInstructions);
                //If or else-if statement
            } else {
                Node child = simplifyExpression(node.jjtGetChild(i));

                //If-expression skip block instructions
                List<Instruction> endBlockInstructions = ifExpression(child, compilation);
                evaluateIfExpression(child, compilation.getByteCode().getLastInstruction());


                ASTBlock block = (ASTBlock) node.jjtGetChild(i + 1);
                List<Instruction> breakInstructions = block(block, cycleStart, compilation);
                allBreakInstructions.addAll(breakInstructions);

                //Unnecessary in the last branch
                if (i < node.jjtGetNumChildren() -2) {
                    //Creates goto instruction to the end of branching
                    Instruction gotoInstruction = compilation.getByteCode().addInstruction(new Instruction(InstructionSet.GoTo, -1));
                    gotoInstructions.add(gotoInstruction);
                }

                //Change the compare instr. so it points to the end of the block
                for (Instruction ebi : endBlockInstructions) {
                    ebi.setOperand(0, compilation.getByteCode().getLastInstructionPosition() + 1);
                }
            }

        }

        //Go through all goto instruction and set them to the end
        for (Instruction i : gotoInstructions) {
            i.setOperand(0, compilation.getByteCode().getLastInstructionPosition() + 1);
        }

        return allBreakInstructions;
    }

    protected void whileStatement(ASTWhileStatement node, MethodCompilation compilation) throws CompilerException,ReturnException {


       Node expression = simplifyExpression(node.jjtGetChild(0));

        int whileExpressionPosition = compilation.getByteCode().getLastInstructionPosition()+1;


        List<Instruction> endBlockInstructions = ifExpression(expression, compilation);
        evaluateIfExpression(expression, compilation.getByteCode().getLastInstruction());

        ASTBlock block = (ASTBlock) node.jjtGetChild(1);
        List<Instruction> breakInstructions = block(block, whileExpressionPosition, compilation);
        endBlockInstructions.addAll(breakInstructions);


        //Goto instruction that goes back to the beginning of the loop
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.GoTo, whileExpressionPosition));


        //Change the compare instr. so it points to the end of the block
        for (Instruction ebi: endBlockInstructions) {
            ebi.setOperand(0, compilation.getByteCode().getLastInstructionPosition() + 1);
        }

        return;
    }

    protected Instruction breakStatement(int cycleStart, MethodCompilation compilation) throws CompilerException {
        if (cycleStart == -1){
            throw new CompilerException("Unexpected break statement outside cycle block");
        }

        Instruction goTo = new Instruction(InstructionSet.GoTo, -1);
        compilation.getByteCode().addInstruction(goTo);

        return goTo;
    }

    protected void continueStatement(int cycleStart, MethodCompilation compilation) throws CompilerException {
        if (cycleStart == -1){
            throw new CompilerException("Unexpected continue statement outside cycle block");
        }
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.GoTo, cycleStart));
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
        }else if (CompilerTypes.isThis(node)) {
            thisReference(compilation);
        }else if (CompilerTypes.isSuper(node)){
            superReference(compilation);
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
        }else if (CompilerTypes.isStringLiteral(node)) {
            stringLiteral(node, compilation);
        }else if (CompilerTypes.isBooleanLiteral(node)) {
            booleanLiteral(node, compilation);
        }else if (CompilerTypes.isNullLiteral(node)) {
            nullLiteral(node, compilation);
        }else{
            throw new NotImplementedException();
        }

        return null;
    }

    protected void thisReference(MethodCompilation compilation){
        int position = compilation.getPositionOfLocalVariable(THIS_VARIABLE);
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.LoadReference, position));
    }

    protected void superReference(MethodCompilation compilation) throws CompilerException {
        if (currentClass.getSuperClass() == null){
            throw new CompilerException("Calling super on top-object");
        }

        thisReference(compilation);
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
        //Load variable reference on stack
        variable((ASTName) node.jjtGetChild(0), compilation);

        //Load index
        Node arrayIndexExpression = simplifyExpression(node.jjtGetChild(1).jjtGetChild(0));
        evaluateExpression(arrayIndexExpression, compilation);

        loadArray(compilation);
    }

    protected void numberLiteral(Node node, MethodCompilation compilation) throws CompilerException {
        String value = ((ASTNumberLiteral) node).jjtGetValue().toString();
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, Integer.parseInt(value)));
    }

    protected void booleanLiteral(Node node, MethodCompilation compilation) throws CompilerException {
        int value = 0;

        if (node instanceof ASTTrue){
            value = 1;
        }
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, value));
    }

    protected void nullLiteral(Node node, MethodCompilation compilation) throws CompilerException {
        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, 0));
    }

    protected void charLiteral(Node node, MethodCompilation compilation) throws CompilerException {
        String value = ((ASTCharLiteral) node).jjtGetValue().toString();
        //Trim the parenthesis
        value = value.substring(1, value.length()-1);
        char charValue = 0;

        if (value.length() == 1) {
            charValue = value.charAt(0);
        }

        compilation.getByteCode().addInstruction(new Instruction(InstructionSet.PushInteger, (int) charValue));
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
        String methodDescriptor = Method.getDescriptor(STRING_CLASS, argTypes, STRING_CLASS);
        int constructorIndex = constantPool.addConstant(methodDescriptor);

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


        invokeConstructor(className, args, compilation);
    }

    protected void invokeConstructor(String className, List<Type> args,  MethodCompilation compilation) throws CompilerException{
        try {
            Class objClass = classPool.lookupClass(className);

            //Call constructor
            invokeMethod(objClass, className, args, compilation);

        } catch (LookupException e) {
            throw new CompilerException("Class '" + className +  "' not found");
        }
    }

    protected void invokeMethod(Class objClass, String name, List<Type> argTypes, MethodCompilation compilation) throws CompilerException {
        invokeMethod(objClass, name, argTypes, compilation, false);
    }

    protected void invokeMethod(Class objClass, String name, List<Type> argTypes, MethodCompilation compilation, boolean staticCall) throws CompilerException {

        //Get method based on it's name, arguments and className
        Method method = new Method(name, argTypes, objClass.getClassName());
        String methodDescriptor = method.getDescriptor();

        try {
            //We try to lookup the method
            method = objClass.lookupMethod(methodDescriptor, this.classPool);
        } catch (LookupException e) {
            //We didn't find it, use the method constructed from what we know
            //It's a native method - we don't know it's class
            method.setClassName(null);
        }


        int methodIndex = constantPool.addConstant(method.getDescriptor());

        //We can't invoke non-static method statically
        // (We can invoke static method non-statically though
        if (staticCall && !method.isStaticMethod()) {
            throw new CompilerException("Trying to invoke non-static method with a static call");
        }


        if (method.isStaticMethod()){
            compilation.getByteCode().addInstruction(new Instruction(InstructionSet.InvokeStatic, methodIndex));
        }else{
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

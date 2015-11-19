package cz.cvut.fit.ostrajava.Compiler;

import java.util.*;

/**
 * Created by tomaskohout on 11/13/15.
 */
public class ByteCode {

    protected List<Instruction> instructions;
    protected Map<String, Variable> localVars;

    public ByteCode() {
        instructions = new ArrayList<Instruction>();
        localVars = new LinkedHashMap<String, Variable>();
    }

    public Instruction addInstruction(Instruction inst){
        instructions.add(inst);
        return inst;
    }

    public int addLocalVariable(String name, Type type){
        if (localVars.containsKey(name)){
            return -1;
        }

        Variable var = new Variable(name, type);

        int pos = localVars.size();
        localVars.put(name, var);
        return pos;
    }

    public Type getTypeOfLocalVariable(String name){
        Variable var = localVars.get(name);
        return var.getType();
    }

    public Variable getLocalVariable(String name){
        Variable var = localVars.get(name);
        return var;
    }

    public int getNumberOfLocalVariables(){
        return localVars.size();
    }

    public Variable getLocalVariable(int index){
        Variable var = localVars.get(variablesKeySet().get(index));
        return var;
    }

    public int getPositionOfLocalVariable(String name){
        int pos = variablesKeySet().indexOf(name);
        return pos;
    }

    protected List<String> variablesKeySet(){
       return new ArrayList<String>(localVars.keySet());
    }

    public Instruction getInstruction(int position){
        return instructions.get(position);
    }

    public Instruction getLastInstruction(){
        return getInstruction(getLastInstructionPosition());
    }

    public int getLastInstructionPosition(){
        return instructions.size() - 1;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int in = 0;
        for (Instruction i: instructions){
            sb.append(in + ": " + i + "\n");
            in++;
        }

        return sb.toString();
    }
}

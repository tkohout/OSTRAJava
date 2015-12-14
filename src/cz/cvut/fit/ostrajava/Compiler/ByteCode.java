package cz.cvut.fit.ostrajava.Compiler;

import java.util.*;

/**
 * Created by tomaskohout on 11/13/15.
 */
public class ByteCode {

    protected List<Instruction> instructions;

    public ByteCode() {
        instructions = new ArrayList<Instruction>();

    }

    public Instruction addInstruction(Instruction inst){
        instructions.add(inst);
        return inst;
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
            sb.append(i + System.getProperty("line.separator"));
            in++;
        }

        return sb.toString();
    }
}

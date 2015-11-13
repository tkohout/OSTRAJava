package cz.cvut.fit.ostrajava.Compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class Instruction {
    protected InstructionSet instruction;
    protected List<String> operands;

    public Instruction(InstructionSet instruction,String operand){
        this.instruction = instruction;
        this.operands = new ArrayList<String>();
        this.operands.add(operand);
    }

    public Instruction(InstructionSet instruction, List<String> operands){
        this.instruction = instruction;
        this.operands = operands;
    }
}

package cz.cvut.fit.ostrajava.Compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class Instruction {
    protected InstructionSet instruction;
    protected List<String> operands;

    public Instruction(InstructionSet instruction) {
        this.instruction = instruction;
        this.operands = new ArrayList<String>();
    }

    public Instruction(InstructionSet instruction,String operand){
        this.instruction = instruction;
        this.operands = new ArrayList<String>();
        this.operands.add(operand);
    }

    public Instruction(InstructionSet instruction, List<String> operands){
        this.instruction = instruction;
        this.operands = operands;
    }

    public List<String> getOperands() {
        return operands;
    }

    public void setOperands(List<String> operands) {
        this.operands = operands;
    }

    public void setOperand(int index, String value){
        this.operands.set(index, value);
    }

    public InstructionSet getInstruction() {
        return instruction;
    }

    public void setInstruction(InstructionSet instruction) {
        this.instruction = instruction;
    }

    public void invert() throws CompilerException {
        InstructionSet newInst;

        switch (instruction){
            case IfCompareEqualInteger:
                newInst = InstructionSet.IfCompareNotEqualInteger;
                break;
            case IfCompareNotEqualInteger:
                newInst = InstructionSet.IfCompareEqualInteger;
                break;
            case IfCompareGreaterThanInteger:
                newInst = InstructionSet.IfCompareLessThanOrEqualInteger;
                break;
            case IfCompareGreaterThanOrEqualInteger:
                newInst = InstructionSet.IfCompareLessThanInteger;
                break;
            case IfCompareLessThanInteger:
                newInst = InstructionSet.IfCompareGreaterThanOrEqualInteger;
                break;
            case IfCompareLessThanOrEqualInteger:
                newInst = InstructionSet.IfCompareGreaterThanInteger;
                break;

            default:
                    throw new CompilerException("Not supported invert operation");

        }

        this.instruction = newInst;
    }
}

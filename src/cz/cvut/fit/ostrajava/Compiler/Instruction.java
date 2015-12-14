package cz.cvut.fit.ostrajava.Compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomaskohout on 11/12/15.
 */
public class Instruction {
    protected InstructionSet instruction;
    protected List<Integer> operands;

    public Instruction(InstructionSet instruction) {
        this.instruction = instruction;
        this.operands = new ArrayList<Integer>();
    }

    public Instruction(InstructionSet instruction,int operand){
        this.instruction = instruction;
        this.operands = new ArrayList<Integer>();
        this.operands.add(operand);
    }

    public Instruction(InstructionSet instruction,int operand1, int operand2){
        this.instruction = instruction;
        this.operands = new ArrayList<Integer>();
        this.operands.add(operand1);
        this.operands.add(operand2);
    }

    public Instruction(InstructionSet instruction, List<Integer> operands){
        this.instruction = instruction;
        this.operands = operands;
    }

    public Instruction(String instructionString) {
        String[] parts = instructionString.split(" ");
        this.instruction = InstructionSet.fromString(parts[0]);
        this.operands = new ArrayList<Integer>();

        for (int i=1; i<parts.length;i++){
            operands.add(Integer.parseInt(parts[i]));
        }
    }

    public List<Integer> getOperands() {
        return operands;
    }
    public int getOperand(int index) {
        return operands.get(index);
    }

    public void setOperands(List<Integer> operands) {
        this.operands = operands;
    }

    public void setOperand(int index, int value){
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

            case IfEqualZero:
                newInst = InstructionSet.IfNotEqualZero;
                break;
            case IfNotEqualZero:
                newInst = InstructionSet.IfEqualZero;
                break;
            case IfGreaterThanZero:
                newInst = InstructionSet.IfLessOrEqualThanZero;
                break;
            case IfGreaterOrEqualThanZero:
                newInst = InstructionSet.IfLessThanZero;
                break;
            case IfLessThanZero:
                newInst = InstructionSet.IfGreaterOrEqualThanZero;
                break;
            case IfLessOrEqualThanZero:
                newInst = InstructionSet.IfGreaterThanZero;
                break;

            default:
                    throw new CompilerException("Not supported invert operation");

        }

        this.instruction = newInst;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append( this.getInstruction() + " ");
        for (int operand: this.getOperands()){
            sb.append(operand + " ");
        }

        return sb.toString();
    }
}

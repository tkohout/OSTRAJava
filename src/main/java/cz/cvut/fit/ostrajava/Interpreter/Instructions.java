package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.Instruction;
import cz.cvut.fit.ostrajava.Compiler.Method;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class Instructions{
    protected List<Instruction> instructionList;
    protected InstructionIterator iterator;

    public Instructions(ClassPool classPool) {
        this.instructionList = new ArrayList<>();
        this.iterator = new InstructionIterator();

        //Go through all classes and methods and save all bytecode to the list
        for (InterpretedClass c: classPool.getClasses()){
            for (Method method: c.getMethods()){
                int position = instructionList.size();

                List<Instruction> instructions = method.getByteCode().getInstructions();

                //We have to change instructions positions relatively to the method
                for (Instruction inst: instructions){
                    switch (inst.getInstruction()){
                        case GoTo:
                        case IfCompareLessThanInteger:
                        case IfCompareLessThanOrEqualInteger:
                        case IfCompareGreaterThanInteger:
                        case IfCompareGreaterThanOrEqualInteger:
                        case IfCompareEqualInteger:
                        case IfCompareNotEqualInteger:
                        case IfLessThanZero:
                        case IfLessOrEqualThanZero:
                        case IfGreaterThanZero:
                        case IfGreaterOrEqualThanZero:
                        case IfEqualZero:
                        case IfNotEqualZero:
                            int operand = inst.getOperand(0);
                            inst.setOperand(0,operand + position);
                            break;
                    }
                }

                instructionList.addAll(instructions);

                //Keep reference to method start position
                ((InterpretedMethod) method).setInstructionPosition(position);


            }
        }
    }

    public void goTo(int position){
        iterator.setPosition(position);
    }

    public InstructionIterator getIterator() {
        return iterator;
    }

    public int getCurrentPosition(){
        return iterator.getPosition() - 1;
    }

    private class InstructionIterator implements Iterator<Instruction> {

        private int position;

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public InstructionIterator() {
            position = 0;
        }

        @Override
        public boolean hasNext() {
            return instructionList.size() > position && position >= 0;
        }

        @Override
        public Instruction next() {
            Instruction inst = instructionList.get(position);
            position++;

            return inst;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int numberToShow = 20;

        int start = getCurrentPosition() - numberToShow / 2;

        if (start < 0){
            start = 0;
        }

        for (int i = start; i<start + numberToShow; i++){
            sb.append(instructionList.get(i));

            if (i == getCurrentPosition()){
                sb.append("<--");
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}

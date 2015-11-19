package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.Instruction;
import cz.cvut.fit.ostrajava.Compiler.Method;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

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

                instructionList.addAll(method.getByteCode().getInstructions());

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
}

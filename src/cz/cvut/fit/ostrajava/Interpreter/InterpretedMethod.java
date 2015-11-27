package cz.cvut.fit.ostrajava.Interpreter;

import cz.cvut.fit.ostrajava.Compiler.Method;

import java.util.List;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class InterpretedMethod extends Method {
    int instructionPosition;

    public InterpretedMethod(Method method) {
        super(method.getName(), method.getArgs(), method.getClassName(), method.getReturnType());

        setStaticMethod(method.isStaticMethod());

        //Copy
        this.setByteCode(method.getByteCode());
        this.setLocalVariablesCount(method.getLocalVariablesCount());
    }

    public int getInstructionPosition() {
        return instructionPosition;
    }

    public void setInstructionPosition(int instructionPosition) {
        this.instructionPosition = instructionPosition;
    }
}

package cz.cvut.fit.ostrajava.Interpreter;

/**
 * Created by tomaskohout on 11/17/15.
 */

import cz.cvut.fit.ostrajava.Compiler.Method;

import java.util.List;

/**
 * Created by tomaskohout on 11/19/15.
 */
public class Stack {
    protected Frame[] frames;
    protected int framesNumber = 0;
    protected int frameStackSize;

    public Stack(int framesMaxNumber, int frameStackSize){
        this.frames = new Frame[framesMaxNumber];
        this.frameStackSize = frameStackSize;
    }

    public Frame newFrame(int returnAddress, int thisReference, Method method) throws InterpreterException {
        if (framesNumber >= frames.length){
            throw new InterpreterException("Exceeded number of frames in stack");
        }
        framesNumber++;
        frames[framesNumber-1] = new Frame(this.frameStackSize, returnAddress, thisReference, method);
        return currentFrame();
    }

    public Frame currentFrame(){
        if (framesNumber <= 0){
            return null;
        }
        return frames[framesNumber-1];
    }

    public void deleteCurrentFrame() throws InterpreterException {
        if (framesNumber <= 0){
            throw new InterpreterException("There are no frames");
        }
        frames[framesNumber-1] = null;
        framesNumber--;
    }

    public String stackTrace(){
        StringBuilder sb = new StringBuilder();
        for (int i=framesNumber-1; i>=0; i--){
            Frame frame = frames[i];
            sb.append(frame.getMethodName() + "()\n");
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return stackTrace();
    }
}


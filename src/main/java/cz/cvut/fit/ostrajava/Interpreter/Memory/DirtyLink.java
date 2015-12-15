package cz.cvut.fit.ostrajava.Interpreter.Memory;

import cz.cvut.fit.ostrajava.Interpreter.StackValue;

/**
 * Created by tomaskohout on 11/29/15.
 */
public class DirtyLink {
    StackValue from;
    StackValue reference;

    public DirtyLink(StackValue from, StackValue reference) {
        this.from = from;
        this.reference = reference;
    }

    public StackValue getFrom() {
        return from;
    }

    public StackValue getReference() {
        return reference;
    }
}

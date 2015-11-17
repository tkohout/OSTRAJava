package cz.cvut.fit.ostrajava.Compiler;

// Not really an exception
// Easy way how to propagate return from everywhere
// Not super clean

import cz.cvut.fit.ostrajava.Parser.Node;

public class ReturnException extends Exception{
    Node value = null;
    public ReturnException(Node value) {
        this.value = value;
    }

    public Node getValue() {
        return value;
    }

    public void setValue(Node value) {
        this.value = value;
    }
}

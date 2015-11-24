package cz.cvut.fit.ostrajava.Type;

public class Reference extends Type{
    private java.lang.String className;

    public Reference(java.lang.String className){
        this.className = className;
    }

    public java.lang.String getClassName() {
        return className;
    }

    public void setClassName(java.lang.String className) {
        this.className = className;
    }

    public java.lang.String toString() {
        return this.className;
    }
}

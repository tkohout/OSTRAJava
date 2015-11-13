package cz.cvut.fit.ostrajava.Compiler;

/**
 * Created by tomaskohout on 11/12/15.
 */
enum InstructionSet {
    StoreInteger("istore"),
    LoadInteger("iload"),
    AddInteger("iadd"),
    SubstractInteger("isub"),
    PushInteger("ipush")
    ;

    private String abbr;
    InstructionSet(String abbr) {
        this.abbr = abbr;
    }

    @Override
    public String toString() {
        return abbr;
    }
}

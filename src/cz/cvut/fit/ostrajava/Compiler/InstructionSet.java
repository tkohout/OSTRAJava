package cz.cvut.fit.ostrajava.Compiler;

/**
 * Created by tomaskohout on 11/12/15.
 */
public enum InstructionSet {
    StoreInteger("istore"),
    LoadInteger("iload"),
    AddInteger("iadd"),
    SubstractInteger("isub"),
    MultiplyInteger("imul"),
    DivideInteger("idiv"),
    ModuloInteger("irem"),
    PushInteger("ipush"),
    StoreReference("astore"),
    LoadReference("aload"),
    New("new"),
    IfCompareEqualInteger("if_icmpeq"),
    IfCompareNotEqualInteger("if_icmpne"),
    IfCompareGreaterThanOrEqualInteger("if_icmpge"),
    IfCompareGreaterThanInteger("if_icmpgt"),
    IfCompareLessThanOrEqualInteger("if_icmple"),
    IfCompareLessThanInteger("if_icmplt"),
    GoTo("goto"),
    InvokeVirtual("invokevirtual"),
    InvokeSpecial("invokespecial"),
    ReturnReference("areturn"),
    ReturnInteger("ireturn"),
    ReturnVoid("return")
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

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
    PushConstant("ldc"),
    NewArray("newarray"),
    StoreIntegerArray("iastore"),
    StoreReferenceArray("aastore"),
    LoadIntegerArray("iaload"),
    LoadReferenceArray("aaload"),
    New("new"),
    Duplicate("dup"),
    GetField("getfield"),
    PutField("putfield"),
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
    ReturnVoid("return"),
    Breakpoint("int"),
    Print("print"),

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

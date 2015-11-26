package cz.cvut.fit.ostrajava.Interpreter.Natives;

/**
 * Created by tomaskohout on 11/26/15.
 */
public class LogInt extends Native {
    @Override
    public NativeValue invoke(NativeValue[] args) {
        int value = args[0].intValue();
        int base = args[1].intValue();

        double res = Math.log(value) / Math.log(base);
        int resInt = (int) res;

        return new NativeValue(resInt);
    }
}

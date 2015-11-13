package cz.cvut.fit.ostrajava.Compiler;

import java.util.*;

/**
 * Created by tomaskohout on 11/13/15.
 */
public class ByteCode {

    protected List<Instruction> instructions;
    protected Map<String, String> localVars;

    public ByteCode() {
        instructions = new ArrayList<Instruction>();
        localVars = new LinkedHashMap<String, String>();
    }

    public void addInstruction(Instruction inst){
        instructions.add(inst);
    }

    public int addLocalVariable(String name, String value){
        if (localVars.containsKey(name)){
            return -1;
        }

        int pos = localVars.size();
        localVars.put(name, value);
        return pos;
    }

    public String getValueOfLocalVariable(String name){
        return localVars.get(name);
    }

    public int getPositionOfLocalVariable(String name){
        int pos = (new ArrayList<String>(localVars.keySet())).indexOf(name);
        return pos;
    }


}

//Imports we need 
import java.util.*;
import sparrow.*;
import sparrow.visitor.DepthFirst;
import IR.token.Identifier;

public class SparrowTranslator extends DepthFirst{
    public ArrayList<String> lines = new ArrayList<>();
    public ArrayList<Hashtable<String, Tuple>> livenessIntervals;
    public ArrayList<Hashtable<String, String>> variableRegisterList;
    public Hashtable<String, Tuple> currentLive;
    public Hashtable<String, String> currentRegs;
    public ArrayList<String> calleeSave;
    public int count = 0;
    public int numParams = 0;
    public int lineCount = 0;
    public boolean mainFunction = true;

    public ArrayList<String> registers = new ArrayList<>(Arrays.asList(
            "s1", "s2", "s3", "s4", "s5", 
            "s6", "s7", "s8", "s9", "s10", "s11",
            "t2", "t3", "t4", "t5"
        ));
    public ArrayList<String> paramRegs = new ArrayList<>(Arrays.asList
    (
        "a2", "a3", "a4", "a5", "a6", "a7"
    ));
    public ArrayList<String> specialRegs = new ArrayList<>(Arrays.asList
    (
        "t0","t1"
    ));
    public ArrayList<Boolean> savail = new ArrayList<>(Arrays.asList(
        true, true
    ));

    public String getRegister(){
        for(int i = 0; i< specialRegs.size(); i++){
            if(savail.get(i) == true){
                savail.set(i, false);
                return specialRegs.get(i);
            }
        }
        return "t0";
    }

    public ArrayList<String> currentlyLiveRegisters(){
        ArrayList<String> trulyLiveVariables = new ArrayList<>();
        
        //print 
       // System.out.println("--------");

        // Iterate through the Hashtable
        for (Map.Entry<String, Tuple> entry : currentLive.entrySet()) {
            String key = entry.getKey();
            Tuple value = entry.getValue();
            //System.out.println(key + " : " + value.first + " " + value.second);
            // Check if lineCount falls between Tuple.first and Tuple.second
            if (lineCount >= Integer.parseInt(value.first) && lineCount <= Integer.parseInt(value.second)) {
                trulyLiveVariables.add(currentRegs.get(key));
            }
        }

        return trulyLiveVariables;
    }

    public void releaseAllRegisters(){
        for(int i = 0; i < specialRegs.size(); i++){
            savail.set(i, true);
        }
    }

    public boolean isSpill(String test){
        if (!registers.contains(test) && !paramRegs.contains(test)) {
            return true;
        }
        return false;
    }

    public String loadVariable(String original){
        String cand = currentRegs.get(original);
        if(isSpill(cand)){
            String temp = getRegister();
            lines.add(temp + " = " + cand);
            cand = temp;
        }
        return cand;

    }

    public void cleanuplhs(String originalVar, String compReg){
        String stackSpace = currentRegs.get(originalVar);
        if(isSpill(stackSpace)){
            lines.add(stackSpace + " = " + compReg );
        }
    }

    public int numUniqueSRegsUsed(){
        Set<String> uniques = new HashSet<>();
        for (String value : currentRegs.values()) {
            if (value.startsWith("s")) {
                uniques.add(value);
            }
        }

        return uniques.size();
    }
    public int numUniqueTRegsUsed(){
        Set<String> uniques = new HashSet<>();
        for (String value : currentRegs.values()) {
            if (value.startsWith("t")) {
                uniques.add(value);
            }
        }

        return uniques.size();
    }


    public SparrowTranslator(SparrowVisitor constr){
        this.livenessIntervals = constr.lineEncodingsList;
        this.variableRegisterList = constr.variableRegisterList;
    }

        /*   List<FunctionDecl> funDecls; */
    public void visit(Program n) {
        int i = 0;
        for (FunctionDecl fd: n.funDecls) {
            lineCount = 0;
            currentLive = livenessIntervals.get(i);
            currentRegs = variableRegisterList.get(i);
            fd.accept(this);
            i+=1;
        }
  }


  /*   Program parent;
   *   FunctionName functionName;
   *   List<Identifier> formalParameters;
   *   Block block; */
  public void visit(FunctionDecl n) {
    for (Identifier fp: n.formalParameters) {
        // ... fp ...
        numParams+=1;
    }
    String concat = "";
    if(numParams > 6){
        int chance = numParams - 6;
        for(int i = 0; i< chance; i++){
            String regi = "a" + Integer.toString(i+8);
            concat += regi + " ";
        }
    }

    lines.add("func " + n.functionName.toString() + "(" + concat + ")");

    calleeSave = new ArrayList<>();
    if(!mainFunction){
        for(int i =0; i<numUniqueSRegsUsed(); i++){
            calleeSave.add(generateTrulyRandom());
            lines.add(calleeSave.get(i) + " = " + "s" + Integer.toString(i+1));
        }
    }
    

    lineCount += 1;

    n.block.accept(this);
    mainFunction = false;
    numParams = 0;
    
  }

/*   FunctionDecl parent;
   *   List<Instruction> instructions;
   *   Identifier return_id; */
  public void visit(Block n) {
    for (Instruction i: n.instructions) {
        i.accept(this);
    }

    String trueRet = loadVariable(n.return_id.toString());
    String trueT = generateTrulyRandom();
    lines.add(trueT + " = " + trueRet);

    if(!mainFunction){
        for(int i = 0; i< numUniqueSRegsUsed(); i++){
            lines.add("s" + Integer.toString(i+1) + " = " + calleeSave.get(i));
        }
    }
    
    
    lines.add("return " + trueT);
    releaseAllRegisters();
  }  

/*   Label label; */
public void visit(LabelInstr n) {
    lines.add(n.label.toString() + ":");
    lineCount += 1;
}  

/*   Identifier lhs;
   *   int rhs; */
public void visit(Move_Id_Integer n) {
    lines.add("t0" + " = " + Integer.toString(n.rhs));
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = " + "t0");
    lineCount += 1;
}

  /*   Identifier lhs;
   *   FunctionName rhs; */
public void visit(Move_Id_FuncName n) {
    lines.add("t0" + " = @" + n.rhs.toString());
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = " + "t0");
    lineCount += 1;
}

  /*   Identifier lhs;
   *   Identifier arg1;
   *   Identifier arg2; */
public void visit(Add n) {
    String arg1reg = loadVariable(n.arg1.toString());
    String arg2reg = loadVariable(n.arg2.toString());
    lines.add("t0 = " + arg1reg + " + " + arg2reg);
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = " + "t0");
    releaseAllRegisters();
    lineCount += 1;
}

/*   Identifier lhs;
   *   Identifier arg1;
   *   Identifier arg2; */
  public void visit(Subtract n) {
    String arg1reg = loadVariable(n.arg1.toString());
    String arg2reg = loadVariable(n.arg2.toString());
    lines.add("t0 = " + arg1reg + " - " + arg2reg);
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = " + "t0");
    releaseAllRegisters();
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier arg1;
   *   Identifier arg2; */
  public void visit(Multiply n) {
    String arg1reg = loadVariable(n.arg1.toString());
    String arg2reg = loadVariable(n.arg2.toString());
    lines.add("t0 = " + arg1reg + " * " + arg2reg);
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = " + "t0");
    releaseAllRegisters();
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier arg1;
   *   Identifier arg2; */
  public void visit(LessThan n) {
    String arg1reg = loadVariable(n.arg1.toString());
    String arg2reg = loadVariable(n.arg2.toString());
    lines.add("t0 = " + arg1reg + " < " + arg2reg);
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = " + "t0");
    releaseAllRegisters();
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier base;
   *   int offset; */
  public void visit(Load n) {
    String basereg = loadVariable(n.base.toString());
    lines.add("t0 = [ " +  basereg + " + " + Integer.toString(n.offset) + " ]");
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = " + "t0");
    releaseAllRegisters();
    lineCount += 1;
  }

/*   Identifier base;
   *   int offset;
   *   Identifier rhs; */
  public void visit(Store n) {
    String basereg = loadVariable(n.base.toString());
    String rhsreg = loadVariable(n.rhs.toString());
    lines.add("[ " +  basereg + " + " + Integer.toString(n.offset) + " ] = " + rhsreg);
    releaseAllRegisters();
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier rhs; */
  public void visit(Move_Id_Id n) {
    String rhsreg = loadVariable(n.rhs.toString());
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = " + rhsreg);
    releaseAllRegisters();
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier size; */
  public void visit(Alloc n) {
    String sizereg = loadVariable(n.size.toString());
    lines.add("t0 = " + "alloc( " + sizereg + " )");
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = t0");
    releaseAllRegisters();
    lineCount += 1;
  }

  /*   Identifier content; */
  public void visit(Print n) {
    String contReg = loadVariable(n.content.toString());
    lines.add("print( " + contReg + " )");
    lineCount += 1;
  }

  /*   String msg; */
  public void visit(ErrorMessage n) {
    lines.add("error( " + n.msg + " )");
    lineCount +=1 ;
  }

  /*   Label label; */
  public void visit(Goto n) {
    lines.add("goto " + n.label.toString());
    lineCount += 1;
  }

  /*   Identifier condition;
   *   Label label; */
  public void visit(IfGoto n) {
    String condreg = loadVariable(n.condition.toString());
    lines.add("if0 " + condreg + " goto " + n.label.toString());
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier callee;
   *   List<Identifier> args; */
  public void visit(Call n) {
    //First do caller saved
    ArrayList<String> callerSave = currentlyLiveRegisters();
    ArrayList<String> filteredCallerSave = new ArrayList<>();
    for (String register : callerSave) {
        if (register.startsWith("t")) {
            if(!filteredCallerSave.contains(register)){
                filteredCallerSave.add(register);
            }
            
        }
    }    
    callerSave = new ArrayList<>();
    for(int i =0; i<filteredCallerSave.size(); i++){
        callerSave.add(generateTrulyRandom());
        lines.add(callerSave.get(i) + " = " + filteredCallerSave.get(i));
    }



    //First, save all old parameters 
    ArrayList<String> oldParams = new ArrayList<>();
    Hashtable <String, String> copy = new Hashtable<>();
    for (Map.Entry<String, String> entry : currentRegs.entrySet()) {
        copy.put(entry.getKey(), entry.getValue());
    }
    for(int i = 0; i< numParams; i++){
        oldParams.add(generateTrulyRandom());
        lines.add("t0 = " + "a" + Integer.toString(i + 2));
        lines.add(oldParams.get(i) + " = t0");
        
        String valueToReplace = "a" + (i + 2);
        ArrayList<String> keysToReplace = new ArrayList<>();
        for (Map.Entry<String, String> entry : currentRegs.entrySet()) {
            if (entry.getValue().equals(valueToReplace)) {
                keysToReplace.add(entry.getKey());
            }
        }
        for (String key : keysToReplace) {
            currentRegs.put(key, oldParams.get(i));
        }
        releaseAllRegisters();
    }


    //Now that we have saved all the old parameters, we need to assign the new parameters 
    int counter = 2;
    for(Identifier element : n.args){
        String argreg = loadVariable(element.toString());
        lines.add("a" + counter + " = " + argreg);
        counter += 1;
        releaseAllRegisters();
    }

    String concat = "";
    counter -= 1;
    if(counter > 7){
        int chance = counter - 7;
        for(int i = 0; i< chance; i++){
            String regi = "a" + Integer.toString(i+8);
            concat += regi + " ";
        }
    }


    //Now that we have the new parameters stored in a2...an, actually write the code for the call
    String calleeReg = loadVariable(n.callee.toString());
    lines.add("t0 = " + "call " + calleeReg + "(" + concat + ")");
    String actualLhs = currentRegs.get(n.lhs.toString());
    lines.add(actualLhs + " = t0");
    //Make sure we dont unsave the register that holds the function return value
    String specialSaveRegister = actualLhs;
    releaseAllRegisters();

    //Now set all of the old params back to their usual registers
    for(int i = 0; i< numParams; i++){
        String oldID = oldParams.get(i);
        lines.add("t0 = " + oldID);
        lines.add("a" + Integer.toString(i + 2) + " = t0");
    }
    currentRegs = copy;

    for(int i = 0; i< filteredCallerSave.size(); i++){
        if(!filteredCallerSave.get(i).equals(specialSaveRegister)){
            lines.add(filteredCallerSave.get(i) + " = " + callerSave.get(i));
        }
    }

    lineCount += 1;
  }


public String generateTrulyRandom(){
    count += 1; 
    return "ffff1f" + Integer.toString(count);
}

public void print(){
    for(String line : lines){
        System.out.println(line);
    }
}


    
}



//Imports we need 
import java.util.*;
import sparrow.*;
import sparrow.visitor.DepthFirst;

import IR.token.Identifier;
//end imports 



public class SparrowVisitor extends DepthFirst{

    public int lineCount = 0; 
    public ArrayList<Hashtable<String, Tuple>> lineEncodingsList = new ArrayList<>();
    public ArrayList<ArrayList<Tuple>> loopFinder = new ArrayList<>();

    public Hashtable<String, Tuple> lineEncodings;
    public ArrayList<Tuple> looper;
    public ArrayList<LoopStruct> encounteredLabels; 


    //Stuff for the linear allocation 
    public ArrayList<Hashtable<String, String>> variableRegisterList = new ArrayList<>();
    public Hashtable<String, String> currentMethodsRegisters;
    public ArrayList<String> registers = new ArrayList<>(Arrays.asList(
            "t2", "t3", "t4", "t5", "s1", "s2", "s3", "s4", "s5", 
            "s6", "s7", "s8", "s9", "s10", "s11"
            
        ));
    public ArrayList<String> altRegisters = new ArrayList<>(Arrays.asList(
        "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
        "t2", "t3", "t4", "t5"
    ));
    public ArrayList<String> deepCopyAltRegisters() {
        return new ArrayList<>(registers);
    }
    public ArrayList<Boolean> availibility = new ArrayList<>(Arrays.asList(
        true, true, true, true, true, true, true, true, true, true, true, true, true, true, true
    ));
    public ArrayList<String> availibleParams = new ArrayList<>(Arrays.asList(
        "a2", "a3", "a4", "a5", "a6", "a7"
    ));
    public ArrayList<Boolean> paramBools = new ArrayList<>(Arrays.asList(
        true, true, true, true, true, true
    ));



    //randomvariablegeneration
    public int count = 0;

    public String getRegister(){
        for(int i = 0; i< registers.size(); i++){
            if(availibility.get(i) == true){
                availibility.set(i, false);
                return registers.get(i);
            }
        }
    
        return null;
    }

    public void releaseRegister(String reg){
        for(int i = 0; i< registers.size(); i++){
            if(registers.get(i).equals(reg)){
                availibility.set(i, true);
                return;
            }
        }
    }

    public void releaseAllRegisters(){
        for(int i = 0; i < registers.size(); i++){
            availibility.set(i, true);
        }
        
    }

    public void getFreeParamRegs(Hashtable<String, String> curRegMaps){
        for(String val : curRegMaps.values()){
            if(val.startsWith("a")){
                for(int i = 0; i<availibleParams.size(); i++){
                    if(val.equals(availibleParams.get(i))){
                        paramBools.set(i, false);
                    }
                }
            }
        }
    }
    public void resetParams(){
        for(int i = 0; i<paramBools.size(); i++){
            paramBools.set(i, true);
        }
    }

    public void runLinearAlgorithm(){
        for(int i = 0; i< lineEncodingsList.size(); i++){
            ArrayList<String> copycat = deepCopyAltRegisters();
            if(i == 0){
                registers = altRegisters;
            }
            
            Hashtable<String, Tuple> currentFunctionVarIntervals = lineEncodingsList.get(i);
            Hashtable<String, String> currentRegisterMapping = variableRegisterList.get(i);
            
            //free up parameter registers
            //getFreeParamRegs(currentRegisterMapping);
            

            List<Map.Entry<String, Tuple>> entryList = new ArrayList<>(currentFunctionVarIntervals.entrySet());
                        entryList.sort((entry1, entry2) -> {
                            int intFirst1 = Integer.parseInt(entry1.getValue().first);
                            int intFirst2 = Integer.parseInt(entry2.getValue().first);
                            return Integer.compare(intFirst1, intFirst2);
                        });
            for(Map.Entry<String, Tuple> entry : entryList){
                //System.out.println("Key: " + entry.getKey() + ", First: " + entry.getValue().first + ", Second: " + entry.getValue().second);
            }

            Hashtable<String, String> temporaryRegisters = new Hashtable<>();
            for(int j = 0; j < entryList.size(); j++){
                Map.Entry<String, Tuple> currentEntry = entryList.get(j);
                int currentTime = Integer.parseInt(currentEntry.getValue().first);

                ArrayList<String> deleteArr = new ArrayList<>();
                //Permanentize and release all temporaries who have expired by currentTime
                for(String element : temporaryRegisters.keySet()){
                    //for every temporary, figure out if it's end time has passed
                    for(Map.Entry<String, Tuple> entry : entryList){
                        if(entry.getKey().equals(element)){
                            if(Integer.parseInt(entry.getValue().second) <= currentTime){
                                //if the end time passed, release register
                                this.releaseRegister(temporaryRegisters.get(element));
                                //and move it to permanent
                                currentRegisterMapping.put(element, temporaryRegisters.get(element));
                                //and delete it from the temporaries
                                deleteArr.add(element);
                                
                            }
                        }
                    }
                }

                //do all necessary deletions
                for(String element : deleteArr){
                    temporaryRegisters.remove(element);
                }
                //if a register is availible, assign it
                //But first, simply check if this value is dead, if so, just give it the register t0
                boolean garbo = false;
                if(Integer.parseInt(currentEntry.getValue().first) == Integer.parseInt(currentEntry.getValue().second)){
                    temporaryRegisters.put(currentEntry.getKey(), "t0");
                    garbo = true; 
                }
                if(!garbo){
                    String possibleReg = this.getRegister();
                    if(possibleReg != null){
                        temporaryRegisters.put(currentEntry.getKey(), possibleReg);
                    }
                    else{
                        //find the register with the latest end time
                        int latest = -1;
                        String possible = "";
                        String possibleOne = "";
                        for(String element : temporaryRegisters.keySet()){
                            //for every temporary, figure out if it's end time is bigger than current latest
                            for(Map.Entry<String, Tuple> entry : entryList){
                                if(entry.getKey().equals(element)){
                                    if(Integer.parseInt(entry.getValue().second) > latest){
                                        //update values
                                        latest = Integer.parseInt(entry.getValue().second);
                                        possible = element;
                                        possibleOne = temporaryRegisters.get(element);
                                    }
                                }
                            }
                        }
    
                        //now that we have the one we need to spill, do it and get our register again
                        this.releaseRegister(possibleOne);
                        currentRegisterMapping.put(possible, possible + "128");
                        temporaryRegisters.remove(possible);
                        possibleReg = this.getRegister();
                        temporaryRegisters.put(currentEntry.getKey(), possibleReg);
    
                    }
    
    
                }
                
            }


            for(String keyOfI : temporaryRegisters.keySet()){
                currentRegisterMapping.put(keyOfI, temporaryRegisters.get(keyOfI));
            }

            variableRegisterList.add(currentRegisterMapping);
            this.releaseAllRegisters();
            for (Map.Entry<String, String> entry : currentRegisterMapping.entrySet()) {
                //System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }

            if(i == 0){
                registers = copycat;
            }

        }


    }

    


    /*   List<FunctionDecl> funDecls; */
    public void visit(Program n) {
        int count = 0;
        for (FunctionDecl fd: n.funDecls) {
            lineCount = 0;
            lineEncodings = new Hashtable<>();
            looper = new ArrayList<>();
            encounteredLabels = new ArrayList<>();
            lineEncodingsList.add(lineEncodings);
            loopFinder.add(looper);

            currentMethodsRegisters = new Hashtable<>();
            variableRegisterList.add(currentMethodsRegisters);
            fd.accept(this);
        }
        this.cleanParams();
        this.loopResolver();
        this.runLinearAlgorithm();
  }


    /*   Program parent;
   *   FunctionName functionName;
   *   List<Identifier> formalParameters;
   *   Block block; */
  public void visit(FunctionDecl n) {
    int i = 2;
    for (Identifier fp: n.formalParameters) {
        // ... fp ...
        String candidate;
        if(i <= 7){
            candidate = "a" + Integer.toString(i);
        }
        else{
            candidate = "a" + Integer.toString(i);
        }
        currentMethodsRegisters.put(fp.toString(), candidate);
        lineEncodings.put(fp.toString() , new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
        i+=1;
    }
    lineCount += 1;
    n.block.accept(this);
  }

    /*   FunctionDecl parent;
   *   List<Instruction> instructions;
   *   Identifier return_id; */
  public void visit(Block n) {
    for (Instruction i: n.instructions) {
        i.accept(this);
    }
    if(lineEncodings.containsKey(n.return_id.toString())){
        Tuple obj = lineEncodings.get(n.return_id.toString());
        obj.second = Integer.toString(lineCount);
    }
  }

  /*   Identifier lhs;
   *   int rhs; */
  public void visit(Move_Id_Integer n) {
    
    if(lineEncodings.containsKey(n.lhs.toString())){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   FunctionName rhs; */
  public void visit(Move_Id_FuncName n) {
    if(lineEncodings.containsKey( n.lhs.toString() )){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier arg1;
   *   Identifier arg2; */
  public void visit(Add n) {
    if(lineEncodings.containsKey(n.lhs.toString())){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }

    if(lineEncodings.containsKey(n.arg1.toString())){
        Tuple obj = lineEncodings.get(n.arg1.toString());
        obj.second = Integer.toString(lineCount);
    }
    if(lineEncodings.containsKey(n.arg2.toString())){
        Tuple obj = lineEncodings.get(n.arg2.toString());
        obj.second = Integer.toString(lineCount);
    }

    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier arg1;
   *   Identifier arg2; */
  public void visit(Subtract n) {
    if(lineEncodings.containsKey( n.lhs.toString() )){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }

    if(lineEncodings.containsKey( n.arg1.toString() )){
        Tuple obj = lineEncodings.get(n.arg1.toString());
        obj.second = Integer.toString(lineCount);
    }
    if(lineEncodings.containsKey( n.arg2.toString() )){
        Tuple obj = lineEncodings.get(n.arg2.toString());
        obj.second = Integer.toString(lineCount);
    }

    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier arg1;
   *   Identifier arg2; */
  public void visit(Multiply n) {
    if(lineEncodings.containsKey( n.lhs.toString() )){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }

    if(lineEncodings.containsKey( n.arg1.toString() )){
        Tuple obj = lineEncodings.get(n.arg1.toString());
        obj.second = Integer.toString(lineCount);
    }
    if(lineEncodings.containsKey( n.arg2.toString() )){
        Tuple obj = lineEncodings.get(n.arg2.toString());
        obj.second = Integer.toString(lineCount);
    }

    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier arg1;
   *   Identifier arg2; */
  public void visit(LessThan n) {
    if(lineEncodings.containsKey( n.lhs.toString() )){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }

    if(lineEncodings.containsKey( n.arg1.toString() )){
        Tuple obj = lineEncodings.get(n.arg1.toString());
        obj.second = Integer.toString(lineCount);
    }
    if(lineEncodings.containsKey( n.arg2.toString() )){
        Tuple obj = lineEncodings.get(n.arg2.toString());
        obj.second = Integer.toString(lineCount);
    }

    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier base;
   *   int offset; */
  public void visit(Load n) {
    if(lineEncodings.containsKey( n.lhs.toString() )){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }

    if(lineEncodings.containsKey( n.base.toString() )){
        Tuple obj = lineEncodings.get(n.base.toString());
        obj.second = Integer.toString(lineCount);
    }

    lineCount += 1;
  }

  /*   Identifier base;
   *   int offset;
   *   Identifier rhs; */
  public void visit(Store n) {
    if(lineEncodings.containsKey( n.rhs.toString() )){
        Tuple obj = lineEncodings.get(n.rhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    if(lineEncodings.containsKey( n.base.toString() )){
        Tuple obj = lineEncodings.get(n.base.toString());
        obj.second = Integer.toString(lineCount);
    }

    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier rhs; */
  public void visit(Move_Id_Id n) {
    if(lineEncodings.containsKey( n.lhs.toString() )){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }

    if(lineEncodings.containsKey( n.rhs.toString() )){
        Tuple obj = lineEncodings.get(n.rhs.toString());
        obj.second = Integer.toString(lineCount);
    }

    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier size; */
  public void visit(Alloc n) {
    if(lineEncodings.containsKey( n.lhs.toString() )){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }
    if(lineEncodings.containsKey(n.size.toString())){
        Tuple obj = lineEncodings.get(n.size.toString());
        obj.second = Integer.toString(lineCount);
    }
    lineCount += 1;
  }

  /*   Identifier content; */
  public void visit(Print n) {
    if(lineEncodings.containsKey( n.content.toString() )){
        Tuple obj = lineEncodings.get(n.content.toString());
        obj.second = Integer.toString(lineCount);
    }
    lineCount += 1;
  }

  /*   Identifier lhs;
   *   Identifier callee;
   *   List<Identifier> args; */
  public void visit(Call n) {
    if(lineEncodings.containsKey( n.lhs.toString() )){
        Tuple obj = lineEncodings.get(n.lhs.toString());
        obj.second = Integer.toString(lineCount);
    }
    else{
        lineEncodings.put(n.lhs.toString(), new Tuple(Integer.toString(lineCount), Integer.toString(lineCount)));
    }
    if(lineEncodings.containsKey( n.callee.toString() )){
        Tuple obj = lineEncodings.get(n.callee.toString());
        obj.second = Integer.toString(lineCount);
    }
    for(Identifier apple: n.args){
        if(lineEncodings.containsKey(apple.toString())){
            Tuple obj = lineEncodings.get(apple.toString());
            obj.second = Integer.toString(lineCount);
        }
    }


    lineCount += 1;
  }

  public void print(){
    for(Hashtable<String, Tuple> lines : lineEncodingsList){
        System.out.println(" ____________");
        for (String key : lines.keySet()) {
            System.out.println("Key: " + key + ", Value: ");
            lines.get(key).print();
        }
    }
  }

  /*   Label label; */
  public void visit(LabelInstr n) {
    encounteredLabels.add(new LoopStruct(n.label.toString(), lineCount));

    lineCount += 1;
  }

  /*   String msg; */
  public void visit(ErrorMessage n) {
    lineCount += 1;
  }

  /*   Label label; */
  public void visit(Goto n) {
    for(LoopStruct element : this.encounteredLabels){
        if(element.has(n.label.toString())){
            this.looper.add(new Tuple(Integer.toString(element.lineNumber), Integer.toString(lineCount)));
        }
        
    }


    lineCount += 1;
  }

  /*   Identifier condition;
   *   Label label; */
  public void visit(IfGoto n) {
    if(lineEncodings.containsKey( n.condition.toString() )){
        Tuple obj = lineEncodings.get(n.condition.toString());
        obj.second = Integer.toString(lineCount);
    }


    for(LoopStruct element : this.encounteredLabels){
        if(element.has(n.label.toString())){
            this.looper.add(new Tuple(Integer.toString(element.lineNumber), Integer.toString(lineCount)));
        }
        
    }

    lineCount += 1;
  }


  public void loopResolver(){
    //the ranges of each loop is given in ArrayList<ArrayList<Tuple>> loopFinder


    //for every function
    for(int i = 0; i< lineEncodingsList.size(); i++){
        //Gives the hashtable for the current method's variables (mapping variable names to star,end pairs)
        Hashtable<String, Tuple> variableStorage = lineEncodingsList.get(i);

        //Gives the arraylist of tuples containing the loop ranges for the current method
        ArrayList<Tuple> loopRanges = loopFinder.get(i);

        //Get all variables we want to consider 
        Set<String> variables = variableStorage.keySet();
        //Now, we iterate through each variable
        for(String element : variables){
            //Get the life of each variable 
            Tuple variableRange = variableStorage.get(element);
            int variableStart = Integer.parseInt(variableRange.first);
            int variableEnd = Integer.parseInt(variableRange.second);
            //Check which loops start after the variable is allocated, and end after the variable is disallocated
            
            
            for(Tuple candidate : loopRanges){
                //Find the range of the loop 
                int loopStart = Integer.parseInt(candidate.first);
                int loopEnd = Integer.parseInt(candidate.second);

                if(variableEnd < loopStart){
                    continue;
                }

                if(loopStart > variableStart){
                    if(loopEnd > variableEnd){
                        variableEnd = loopEnd; 
                    }
                }
            }

            variableRange.second = Integer.toString(variableEnd);
        }

    }
    
  }

public String generateVarName(){
        this.count += 1;
        return "p" + Integer.toString(this.count);
    }

public void cleanParams(){
    for (int i = 0; i < lineEncodingsList.size(); i++) {
        Hashtable<String, Tuple> lineEncodings = lineEncodingsList.get(i);
        Hashtable<String, String> variableRegisters = variableRegisterList.get(i);

        // Use an iterator to avoid ConcurrentModificationException
        Iterator<Map.Entry<String, Tuple>> iterator = lineEncodings.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Tuple> entry = iterator.next();
            String firstValue = entry.getKey();

            // Check if any entry in variableRegisters has a matching value
            if (variableRegisters.containsKey(firstValue)) {
                iterator.remove();
            }
        }
    }
}





}

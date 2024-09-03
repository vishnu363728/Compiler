import java.util.*;
import sparrowv.visitor.*;
import sparrowv.*;
import IR.token.Identifier;

public class SparrowVisitor extends DepthFirst{
    public ArrayList<HashMap<String, String>> methodMappings = new ArrayList<>();
    public ArrayList<Integer> spacePerMethod = new ArrayList<>();
    public ArrayList<Integer> paramSpacePerMethod = new ArrayList<>();
    public HashMap<String, String> currentMapping;
    public int currentParamCount;
    public int currentCount;

      /*   List<FunctionDecl> funDecls; */
  public void visit(Program n) {
    for (FunctionDecl fd: n.funDecls) {
        currentCount = -8;
        currentParamCount = 0;
        currentMapping = new HashMap<>();
        fd.accept(this);
        methodMappings.add(currentMapping);
        spacePerMethod.add(currentCount);
        paramSpacePerMethod.add(currentParamCount);
    }
  }

  /*   Program parent;
   *   FunctionName functionName;
   *   List<Identifier> formalParameters;
   *   Block block; */
  public void visit(FunctionDecl n) {
    for (Identifier fp: n.formalParameters) {
        currentMapping.put(fp.toString(), Integer.toString(currentParamCount) + "(fp)");
        currentParamCount += 4;
        // ... fp ...
    }
    n.block.accept(this);
  }

    /*   Identifier lhs;
   *   Register rhs; */
  public void visit(Move_Id_Reg n) {
    String stackSpaceName = n.lhs.toString();
    if(!currentMapping.containsKey(stackSpaceName)){
        currentCount -= 4;
        currentMapping.put(stackSpaceName, Integer.toString(currentCount) + "(fp)");
    }

}
    public void print(){
        for(HashMap<String, String> map : this.methodMappings){
            for (Map.Entry<String, String> entry : map.entrySet()) {
                // Get the key and value from the entry
                String key = entry.getKey();
                String value = entry.getValue();
                
                // Print the key-value pair
                System.out.println("Key: " + key + ", Value: " + value);
            }
        }
        
    }




}

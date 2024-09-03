import minijava.syntaxtree.*;
import java.util.*;

public class ClassSymbol {
    public String name;
    public String parentName;
    public boolean markDirty;
    public ClassSymbol parent;
    public boolean mainclass;
    public LinkedHashSet<MethodSymbol> methodSet = new LinkedHashSet<>();
    public LinkedHashSet<TypeSymbol> memberVariables = new LinkedHashSet<>();
    public MethodSymbol currentMethod;


    public ClassSymbol(String name, String parentName) {
        this.name = name;
        this.parentName = parentName;
        this.markDirty = true;
        this.mainclass = false;

        if(name.equals(parentName)){
            System.out.println("Type error");
            System.exit(1);
        }
    }

    public ClassSymbol(String name){
        this.name = name;
        this.mainclass = false;
        this.parent = null;
        this.markDirty = false;
    }

    public ClassSymbol(String name, boolean mainclass){
        this.name = name;
        this.mainclass = mainclass;
        this.parent = null;
        this.markDirty = false;
    }

    public void addMethod(MethodSymbol method) {
        for (MethodSymbol m : this.methodSet) {
            if (m.name.equals(method.name)) {
                System.out.println("Type error");
                System.exit(1);
            }
        }
        if(this.parent != null){
            for (MethodSymbol m : parent.methodSet){
                if(m.name.equals(method.name)){
                    if(!m.signatureEquivalence(method)){
                        System.out.println("Type error");
                        System.exit(1);
                    }
                }
            }
        }

        this.methodSet.add(method);
    }

    public void addMemberVariable(TypeSymbol variable) {
        for (TypeSymbol mVar : this.memberVariables) {
            if (mVar.identifier.equals(variable.identifier)) {
                System.out.println("Type error");
                System.exit(1);
            }
        }
        
        this.memberVariables.add(variable);
    }

    public MethodSymbol containsMethod(String methodName) {
        for(MethodSymbol m : this.methodSet){
            if(m.name.equals(methodName)){
                return m;
            }
        }
        return null;
    }

    public TypeSymbol containsMemberVariable(String identifier) {
        for (TypeSymbol variable : memberVariables) {
            if (variable.identifier.equals(identifier)) {
                return variable;
            }
        }
        return null;
    }

    public TypeSymbol containsMutualMemberVariable(String identifier) {

        if(this.containsMemberVariable(identifier) != null){
            return this.containsMemberVariable(identifier);
        }
        ClassSymbol current = this.parent;
        while(current != null){
            if(current.containsMemberVariable(identifier) != null){
                return current.containsMemberVariable(identifier);
            }
            current = current.parent;
        }
        return null;
    }

    public MethodSymbol containsMutualMethod(String methodName){
        ClassSymbol current = this;
        while(current != null){
            if(current.containsMethod(methodName) != null){
                return current.containsMethod(methodName);
            }
            current = current.parent;
        }
        return null;
        

    }

    public boolean isGrandparentOfThis(String proposed){
        ClassSymbol currentClass = this.parent;
        while(currentClass != null){
            if(currentClass.name.equals(proposed)){
                return true;
            }
            currentClass = currentClass.parent;
        }
        return false;
    }

    public boolean hasCyclicalParents(){
        ClassSymbol currentClass = this.parent;
        HashSet<ClassSymbol> encounteredParents = new HashSet<>();
        encounteredParents.add(currentClass);
        currentClass = currentClass.parent;
        while(currentClass != null){
            if(encounteredParents.contains(currentClass)){
                return true;
            }
            currentClass = currentClass.parent;
        }
        return false;
    }


    public void print(){
        System.out.println("-------");
        String thep;
        if(this.parent == null){
            thep = "none";
        }
        else{
            thep = parent.name;
        }
        System.out.println("Class name : " + this.name + " w parent " + thep);
        System.out.println("Member variables : ");
        for(TypeSymbol element : memberVariables){
            element.print();
        }
        System.out.println("Methods : ");
        for(MethodSymbol element : methodSet){
            element.print();
        }
        System.out.println("-------");       
        
        this.instantiationSparrow().print();
    }

    public void fixOverridingBug(){
        if(this.parent == null){
            return;
        }
        for(MethodSymbol element : this.methodSet){
            for(MethodSymbol ele : this.parent.methodSet){
                if(element.name.equals(ele.name)){
                    if(!element.signatureEquivalence(ele)){
                        System.out.println("Type error");
                        System.exit(1);
                    }
                }
            }
        }
    }

    public SparrowFile instantiationSparrow(){
        SparrowFile classSparrow = new SparrowFile();
        ArrayList<String> wlines = classSparrow.lines;       
        
        String sizeOfClassName = SparrowFile.randomVarName();
        String sizeOfMethodTableName = SparrowFile.randomVarName();
        String dataTableName = SparrowFile.randomVarName();
        String vtableName = SparrowFile.randomVarName();
        String parentFlag = SparrowFile.randomVarName();
        
        int numMethods = methodSet.size();
        int numVars = memberVariables.size();
        int totalSizeOfClass = 12 + numVars*4;
        int totalSizeOfMethodTable = numMethods*4;
        int pFlag = 0;
        if(this.parent != null){
            pFlag = 1;
        }

        //this line initializes the size of the class
        wlines.add(sizeOfClassName + SparrowFile.equals() + Integer.toString(totalSizeOfClass));
        //this line allocates the memory for the data table alloted by the size
        wlines.add(dataTableName + SparrowFile.equals() + SparrowFile.alloc(sizeOfClassName));
        //this line initializes the size of the method table 
        wlines.add(sizeOfMethodTableName + SparrowFile.equals() + Integer.toString(totalSizeOfMethodTable));
        //this line allocates the memory for the method table alloted by the size 
        wlines.add(vtableName + SparrowFile.equals() + SparrowFile.alloc(sizeOfMethodTableName));


        //Fills out method table
        ArrayList<MethodSymbol> metlist = new ArrayList<>(methodSet);
        for(int i = 0; i< numMethods;i++){
            String offset = SparrowFile.randomVarName();
            String methodHouse = SparrowFile.randomVarName();
            wlines.add(SparrowFile.equalsStmt(offset, Integer.toString(i * 4)));

            String methodCoolName = metlist.get(i).generateVtableEntry();
            wlines.add(SparrowFile.equalsStmt(offset, SparrowFile.addition(vtableName, offset)));
            wlines.add(SparrowFile.equalsStmt(methodHouse, methodCoolName));
            wlines.add(SparrowFile.equalsStmt(SparrowFile.brackets(SparrowFile.addition(offset,Integer.toString(0))),methodHouse));
        }




        //this line initializes the parent flag
        wlines.add(parentFlag + SparrowFile.equals() + Integer.toString(pFlag));
        //this line puts the parent flag in the 0 byte slot
        wlines.add(SparrowFile.brackets(SparrowFile.addition(dataTableName, Integer.toString(0))) + SparrowFile.equals() + parentFlag);
        //this line puts the method table in the 8 byte slot
        wlines.add(SparrowFile.brackets(SparrowFile.addition(dataTableName, Integer.toString(8))) + SparrowFile.equals() + vtableName);


        if(pFlag == 1){
            SparrowFile inheritance = this.parent.instantiationSparrow();
            String parentPointerString = inheritance.var;
            classSparrow.concatenateLinesBefore(inheritance);
            wlines = classSparrow.lines;       
            //this line puts the parent pointer in the 4 byte slot
            wlines.add(SparrowFile.brackets(SparrowFile.addition(dataTableName, Integer.toString(4))) + SparrowFile.equals() + parentPointerString);
        }
        classSparrow.var = dataTableName;
        return classSparrow;
    }
}
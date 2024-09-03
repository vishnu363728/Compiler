import java.util.*;

public class MethodSymbol {
    public String name;
    public String returnType;
    public ClassSymbol associatedClass;
    public ArrayList<TypeSymbol> parameters = new ArrayList<>();
    public HashSet<TypeSymbol> localVariables = new HashSet<>();

    public ArrayList<String> getParameterRegisters(){
        ArrayList<String> params = new ArrayList<String>();
        params.add("this");
        for(TypeSymbol element : parameters){
            params.add(element.sparrowVariable);
        }
        return params;
    }

    public String generateVtableEntry(){
        return "@" + this.associatedClass.name + this.name;
    }

    public String findIdOfMethodVar(String request){
        for(TypeSymbol element : this.parameters){
            if(element.identifier.equals(request)){
                return element.sparrowVariable;
            }
        }
        for(TypeSymbol element : this.localVariables){
            if(element.identifier.equals(request)){
                return element.sparrowVariable;
            }
        }
        return null;
    }
    
    

    public MethodSymbol(String name, String returnType, ClassSymbol associatedClass) {
        this.name = name;
        this.returnType = returnType;
        this.associatedClass = associatedClass;
    }

    public boolean signatureEquivalence(MethodSymbol another){
        if(!another.returnType.equals(this.returnType)){
            return false;
        }
        if(another.parameters.size() != this.parameters.size()){
            return false;
        }
        for(int i = 0; i < parameters.size(); i++){
            if(!(another.parameters.get(i).type).equals((this.parameters).get(i).type)){
                return false;
            }
        }
        return true;
    }

    public void addParameters(TypeSymbol addition){
        for (TypeSymbol par : this.parameters) {
            if (par.identifier.equals(addition.identifier)) {
                System.out.println("Type error");
                System.exit(1);
            }
        }
        this.parameters.add(addition);
    }

    public void addLocalVariables(TypeSymbol addition){
        for (TypeSymbol localVar : this.localVariables) {
            if (localVar.identifier.equals(addition.identifier)) {
                System.out.println("Type error");
                System.exit(1);
            }
        }
        this.localVariables.add(addition);
    }

    public String findIdentifierType(String id){
        for(TypeSymbol element : this.parameters){
            if(element.identifier.equals(id)){
                return element.type;
            }
        }
        for(TypeSymbol element : this.localVariables){
            if(element.identifier.equals(id)){
                return element.type;
            }
        }

        if(this.associatedClass.containsMutualMemberVariable(id) != null){
            return this.associatedClass.containsMutualMemberVariable(id).type;
        }
        return null;
    }

    public void changeType(String id, String desired){
        for(TypeSymbol element : this.parameters){
            if(element.identifier.equals(id)){
                element.type = desired;
                return;
            }
        }
        for(TypeSymbol element : this.localVariables){
            if(element.identifier.equals(id)){
                element.type = desired;
                return;
            }
        }

        if(this.associatedClass.containsMutualMemberVariable(id) != null){
            this.associatedClass.containsMutualMemberVariable(id).type = desired;
            return;
        }
    }

    public void print(){
        System.out.println("-------");
        System.out.println("Method name : " + this.name + " w return type " + this.returnType);
        System.out.println("params : ");
        for(TypeSymbol element : this.parameters){
            element.print();
        }
        System.out.println("local variables : ");
        for(TypeSymbol element : this.localVariables){
            element.print();
        }
        System.out.println("-------");        
    }
}

import minijava.syntaxtree.*;
import java.util.*;

public class ClassSymbol {
    public String name;
    public String parentName;
    public boolean markDirty;
    public ClassSymbol parent;
    public boolean mainclass;
    public HashSet<MethodSymbol> methodSet = new HashSet<>();
    public HashSet<TypeSymbol> memberVariables = new HashSet<>();
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
        this.markDirty = false;
    }

    public ClassSymbol(String name, boolean mainclass){
        this.name = name;
        this.mainclass = mainclass;
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

}
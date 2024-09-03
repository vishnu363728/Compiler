public class TypeSymbol {
    public String identifier;
    public String type;
    public ClassSymbol associatedClass;

    public TypeSymbol(String identifier, String type) {
        this.identifier = identifier;
        this.type = type;
        this.associatedClass = null;
    }

    public TypeSymbol(String identifier, String type, ClassSymbol associatedClass) {
        this.identifier = identifier;
        this.type = type;
        this.associatedClass = associatedClass;
    }

    public boolean thisIsSubtypeOfParam(TypeSymbol param){
        if((this.associatedClass == null) || (param.associatedClass == null)){
            if(this.type.equals(param.type)){
                return true;
            }
            return false;
        }
        ClassSymbol cur = this.associatedClass;
        while(cur != null){
            if(cur.name.equals(param.associatedClass.name)){
                return true;
            }
            cur = cur.parent;
        }
        return false;
    }

    public void print(){
        System.out.println("Type is " + this.type + " Identifier is " + this.identifier);
    }
}

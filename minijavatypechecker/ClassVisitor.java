import minijava.syntaxtree.*;
import minijava.visitor.*;
import java.util.*;
public class ClassVisitor extends GJVoidDepthFirst<ClassSymbol>{
       /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    public HashSet<ClassSymbol> classes = new HashSet<>();
    public HashSet<TypeSymbol> allTypes = new HashSet<>();
    public String curType;
    public ClassSymbol curClass;

    public boolean isASubtypeOfB(String A, String B){
        if(A.equals(B)){return true;}
        if(A.equals("int") || A.equals("bool") || A.equals("array")){
            return false;
        }
        ClassSymbol classA = this.getClassFromName(A);
        if(A == null){return false;}
        return classA.isGrandparentOfThis(B);
    }

    public ClassSymbol getClassFromName(String className){
        for(ClassSymbol element: classes){
            if(element.name.equals(className)){
                return element;
            }
        }
        return null;
    }

    public MethodSymbol getMethodFromNameAndClass(String className, String methodName){
        ClassSymbol currentClass = this.getClassFromName(className);
        while(currentClass != null){
            if(currentClass.containsMethod(methodName) != null){
                return currentClass.containsMethod(methodName);
            }
            currentClass = currentClass.parent;
        }
        return null;
    }

    public void print(){
        for(ClassSymbol element : classes){
            element.print();
        }
    }

    public void addClass(ClassSymbol classToAdd){
        for(ClassSymbol element : this.classes){
            if(element.name.equals(classToAdd.name)){
                System.out.println("Type error");
                System.exit(1);
            }
        }
        this.classes.add(classToAdd);
    }
   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    public void visit(Goal n, ClassSymbol my) {
        n.f0.accept(this, my);
        n.f1.accept(this, my);
        this.resolveParents();
        this.detectCyclicalInheritance();
        this.resolveTypeClasses();
    }

    /**
        * f0 -> "class"
        * f1 -> Identifier()
        * f2 -> "{"
        * f3 -> "public"
        * f4 -> "static"
        * f5 -> "void"
        * f6 -> "main"
        * f7 -> "("
        * f8 -> "String"
        * f9 -> "["
        * f10 -> "]"
        * f11 -> Identifier()
        * f12 -> ")"
        * f13 -> "{"
        * f14 -> ( VarDeclaration() )*
        * f15 -> ( Statement() )*
        * f16 -> "}"
        * f17 -> "}"
        */
        public void visit(MainClass n, ClassSymbol my) {    
            n.f1.accept(this,my);
            ClassSymbol mc = new ClassSymbol(this.curType, true);
            this.addClass(mc);
            MethodSymbol mainMethod = new MethodSymbol("main", "void", mc);
            mc.addMethod(mainMethod);
            mc.currentMethod = mainMethod;
            this.curClass = mc;      
            n.f14.accept(this, my);
        }

                /**
            * f0 -> "class"
            * f1 -> Identifier()
            * f2 -> "{"
            * f3 -> ( VarDeclaration() )*
            * f4 -> ( MethodDeclaration() )*
            * f5 -> "}"
            */
        public void visit(ClassDeclaration n, ClassSymbol my) {        
            n.f1.accept(this,my);
            ClassSymbol mc = new ClassSymbol(this.curType);   
            this.addClass(mc);
            this.curClass = mc;     
            n.f3.accept(this,my);
            n.f4.accept(this,my);
        }

    /**
        * f0 -> "class"
        * f1 -> Identifier()
        * f2 -> "extends"
        * f3 -> Identifier()
        * f4 -> "{"
        * f5 -> ( VarDeclaration() )*
        * f6 -> ( MethodDeclaration() )*
        * f7 -> "}"
        */
        public void visit(ClassExtendsDeclaration n, ClassSymbol my) {  
            n.f3.accept(this,my);
            String parent = this.curType;
            n.f1.accept(this,my);
            ClassSymbol mc = new ClassSymbol(this.curType, parent);
            this.addClass(mc);
            this.curClass = mc;     
            n.f5.accept(this,my);
            n.f6.accept(this,my);            
        }

        /**
            * f0 -> "public"
            * f1 -> Type()
            * f2 -> Identifier()
            * f3 -> "("
            * f4 -> ( FormalParameterList() )?
            * f5 -> ")"
            * f6 -> "{"
            * f7 -> ( VarDeclaration() )*
            * f8 -> ( Statement() )*
            * f9 -> "return"
            * f10 -> Expression()
            * f11 -> ";"
            * f12 -> "}"
            */
        public void visit(MethodDeclaration n, ClassSymbol my) {
            n.f1.accept(this,my);
            String retType = this.curType;
            n.f2.accept(this,my);
            String name = this.curType;
            MethodSymbol ourMethod = new MethodSymbol(name, retType, this.curClass);
            this.curClass.addMethod(ourMethod);
            this.curClass.currentMethod = ourMethod;
            n.f4.accept(this,my);
            n.f7.accept(this,my);
        }

                /**
            * f0 -> Type()
            * f1 -> Identifier()
            */
        public void visit(FormalParameter n, ClassSymbol my) {
            n.f0.accept(this, my);
            String type = this.curType;
            n.f1.accept(this, my);
            String name = this.curType;
            TypeSymbol additive = new TypeSymbol(name,type,my);
            allTypes.add(additive);
            this.curClass.currentMethod.addParameters(additive);

        }

   /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
        public void visit(VarDeclaration n, ClassSymbol my){
            n.f0.accept(this, my);
            String type = this.curType;
            n.f1.accept(this,my);
            String name = this.curType;
            if(this.curClass.currentMethod != null){
                TypeSymbol additive = new TypeSymbol(name,type,my);
                for(TypeSymbol element : this.curClass.currentMethod.parameters){
                    if(element.identifier.equals(additive.identifier)){
                        System.out.println("Type error");
                        System.exit(1);
                    }
                }
                allTypes.add(additive);
                this.curClass.currentMethod.addLocalVariables(additive);
            }
            else{
                TypeSymbol additive = new TypeSymbol(name,type,my);
                allTypes.add(additive);
                this.curClass.addMemberVariable(additive);
            }
            
        }

    /**
        * f0 -> <IDENTIFIER>
        */
        public void visit(Identifier n, ClassSymbol my) {
            this.curType = n.f0.toString();
           // System.out.println(this.curType);
        }        

        /**
            * f0 -> "int"
            * f1 -> "["
            * f2 -> "]"
            */
        public void visit(ArrayType n, ClassSymbol my) {
            this.curType = "array";
         }
      
         /**
          * f0 -> "boolean"
          */
         public void visit(BooleanType n, ClassSymbol my) {
            this.curType = "bool";

         }
      
         /**
          * f0 -> "int"
          */
         public void visit(IntegerType n, ClassSymbol my) {
            this.curType = "int";

         }

         public void resolveParents(){
            for(ClassSymbol element : classes){
                if(element.markDirty){
                    for(ClassSymbol ele : classes){
                        if(ele.name.equals(element.parentName)){
                            if(ele.mainclass){
                                System.out.println("Type error");
                                System.exit(1);
                            }
                            element.parent = ele;
                            element.markDirty = false;
                        }
                    }
                    if(element.markDirty){
                        System.out.println("Type error");
                        System.exit(1);
                    }
                }
            }
         }

         public void detectCyclicalInheritance(){
            for(ClassSymbol element : this.classes){
                element.fixOverridingBug();
                if(element.parent != null){
                    if(element.hasCyclicalParents()){
                        System.out.println("Type error");
                        System.exit(1);
                    }
                }
            }
         }

         public void resolveTypeClasses(){
            ArrayList<String> typeEnum = new ArrayList<>();
            typeEnum.add("int");
            typeEnum.add("bool");
            typeEnum.add("array");
            for(TypeSymbol element : this.allTypes){
                if(!typeEnum.contains(element.type)){
                    if(this.getClassFromName(element.type) != null){
                        element.associatedClass = this.getClassFromName(element.type);
                    }
                    else{
                        System.out.println("Type error");
                        System.exit(1);
                    }
                }
            }
         }


}

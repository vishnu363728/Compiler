import minijava.syntaxtree.*;
import minijava.visitor.*;
import java.util.*;

public class TypeVisitor extends GJVoidDepthFirst<ClassSymbol>{
    public ClassVisitor symbolTable;
    public ClassSymbol currentClass; 
    public String curType;
    public String idName;
    public boolean wantType = true;
    public ArrayList<String> argumentStorage = new ArrayList<>();


    public TypeVisitor(ClassVisitor symbolTable){
        this.symbolTable = symbolTable; 
    }
   /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
    public void visit(Goal n, ClassSymbol my) {
        n.f0.accept(this, my);
        n.f1.accept(this, my);
        System.out.println("Program type checked successfully");
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
        this.wantType = false;
        n.f1.accept(this,my);
        this.wantType = true;
        String className = this.idName;
        this.currentClass = this.symbolTable.getClassFromName(className);
        this.currentClass.currentMethod = this.currentClass.containsMethod("main");
        n.f15.accept(this, my);
        this.currentClass.currentMethod = null;
        this.currentClass = null;        
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
        this.wantType = false;     
        n.f1.accept(this,my);
        this.wantType = true;
        String className = this.idName;
        this.currentClass = this.symbolTable.getClassFromName(className);
        n.f4.accept(this,my);
        this.currentClass.currentMethod = null;
        this.currentClass = null;
        
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
        this.wantType = false;
        n.f1.accept(this,my);
        this.wantType = true;
        String className = this.idName;
        this.currentClass = this.symbolTable.getClassFromName(className);
        n.f6.accept(this,my);      
        this.currentClass.currentMethod = null;
        this.currentClass = null;        
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
        this.wantType = false;
        n.f2.accept(this,my);
        this.wantType = true;
        String name = this.idName;
        MethodSymbol ourMethod = this.currentClass.containsMethod(name);
        this.currentClass.currentMethod = ourMethod;
        n.f8.accept(this,my);
        n.f10.accept(this,my);
        String RightHandReturn = this.curType;
        String LeftHandReturn = this.currentClass.currentMethod.returnType;
        if(!symbolTable.isASubtypeOfB(RightHandReturn, LeftHandReturn)){
            System.out.println("Type error");
            System.exit(1);
        }
    }  




       /**
    * f0 -> IntegerLiteral()
    *       | TrueLiteral()
    *       | FalseLiteral()
    *       | Identifier()
    *       | ThisExpression()
    *       | ArrayAllocationExpression()
    *       | AllocationExpression()
    *       | NotExpression()
    *       | BracketExpression()
    
   public void visit(PrimaryExpression n, ClassSymbol my) {
        n.f0.accept(this, my);
        return _ret;
    }*/

    /**
    * f0 -> <INTEGER_LITERAL>
    */
    public void visit(IntegerLiteral n, ClassSymbol my) {
        this.curType = "int";
    }

       /**
    * f0 -> "true"
    */
   public void visit(TrueLiteral n, ClassSymbol my) {
        this.curType = "bool";
    }

       /**
    * f0 -> "false"
    */
   public void visit(FalseLiteral n, ClassSymbol my) {
        this.curType = "bool";
    }

/**
    * f0 -> <IDENTIFIER>
    */
    public void visit(Identifier n, ClassSymbol my) {
        this.idName = n.f0.toString();
        if(wantType){
            if(this.currentClass.currentMethod.findIdentifierType(this.idName) != null){
                this.curType = this.currentClass.currentMethod.findIdentifierType(this.idName);
            }
            else{
                System.out.println("Type error");
                System.exit(1);
            }
        }
    }            

      /**
    * f0 -> "this"
    */
    public void visit(ThisExpression n, ClassSymbol my) {
        this.curType = this.currentClass.name;
    } 


   /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public void visit(ArrayAllocationExpression n, ClassSymbol my) {
        n.f3.accept(this, my);
        if(this.curType.equals("int")){
            this.curType = "array";
        }
        else{
            System.out.println("Type error");
            System.exit(1);
        }
        
     }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public void visit(AllocationExpression n, ClassSymbol my) {
        wantType = false;
        n.f1.accept(this, my);
        wantType = true;
        this.curType = this.idName;
    }     

   /**
    * f0 -> "!"
    * f1 -> Expression()
    */
    public void visit(NotExpression n, ClassSymbol my) {
        n.f1.accept(this, my);
        if(!this.curType.equals("bool")){
            System.out.println("Type error");
            System.exit(1);
        }
     }   
     
  /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public void visit(BracketExpression n, ClassSymbol my) {
        n.f1.accept(this, my); //curType will be set
     }     
    
   /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
    public void visit(AndExpression n, ClassSymbol my) {
        
        n.f0.accept(this, my);
        String firstType = this.curType;
        n.f2.accept(this, my);
        String secondType = this.curType; 
        if(firstType.equals("bool") && secondType.equals("bool")){
            this.curType = "bool";
        }
        else{
            System.out.println("Type error");
            System.exit(1);
        }
     }    
    
   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public void visit(CompareExpression n, ClassSymbol my) {
        n.f0.accept(this, my);
        String firstType = this.curType;
        n.f2.accept(this, my);
        String secondType = this.curType;

        if(firstType.equals("int") && secondType.equals("int")){
            this.curType = "bool";
        }
        else{
            System.out.println("Type error");
            System.exit(1);
        }
    }

        /**
        * f0 -> PrimaryExpression()
        * f1 -> "+"
        * f2 -> PrimaryExpression()
        */
    public void visit(PlusExpression n, ClassSymbol my) {
        n.f0.accept(this, my);
        String firstType = this.curType;
        n.f2.accept(this, my);
        String secondType = this.curType;
        if(firstType.equals("int") && secondType.equals("int")){
            this.curType = "int";
        }
        else{
            System.out.println("Type error");
            System.exit(1);
        }
    }

        /**
        * f0 -> PrimaryExpression()
        * f1 -> "-"
        * f2 -> PrimaryExpression()
        */
    public void visit(MinusExpression n, ClassSymbol my) {
        n.f0.accept(this, my);
        String firstType = this.curType;
        n.f2.accept(this, my);
        String secondType = this.curType;
        if(firstType.equals("int") && secondType.equals("int")){
            this.curType = "int";
        }
        else{
            System.out.println("Type error");
            System.exit(1);
        }
    
    }

        /**
        * f0 -> PrimaryExpression()
        * f1 -> "*"
        * f2 -> PrimaryExpression()
        */
    public void visit(TimesExpression n, ClassSymbol my) {
        n.f0.accept(this, my);
        String firstType = this.curType;
        n.f2.accept(this, my);
        String secondType = this.curType;
        if(firstType.equals("int") && secondType.equals("int")){
            this.curType = "int";
        }
        else{
            System.out.println("Type error");
            System.exit(1);
        }
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public void visit(ArrayLookup n, ClassSymbol my) {
        n.f0.accept(this, my);
        String firstType = this.curType;
        n.f2.accept(this, my);
        String secondType = this.curType;
        if(firstType.equals("array") && secondType.equals("int")){
            this.curType = "int";
        }
        else{
            System.out.println("Type error");
            System.exit(1);
        }
     }    

  
    /**
        * f0 -> PrimaryExpression()
        * f1 -> "."
        * f2 -> "length"
        */
    public void visit(ArrayLength n, ClassSymbol my) {
        n.f0.accept(this, my);
        String firstType = this.curType;
        if(firstType.equals("array")){
            this.curType = "int";
        }
        else{
            System.out.println("Type error");
            System.exit(1);
        }
    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    public void visit(MessageSend n, ClassSymbol my) {
        n.f0.accept(this, my);
        String className = this.curType;
        wantType = false;
        n.f2.accept(this,my);
        wantType = true;
        String methodName = this.idName; 
        n.f4.accept(this, my);
        int argumentCount = argumentStorage.size();
        ClassSymbol relevantClass = symbolTable.getClassFromName(className);
        
        if(relevantClass == null){
            System.out.println("Type error");
            System.exit(1);
        }
        MethodSymbol relevantMethod = relevantClass.containsMutualMethod(methodName);
        if(relevantMethod == null){
            System.out.println("Type error");
            System.exit(1);
        }
        
        //true stuff required by the method 
        String retType = relevantMethod.returnType;
        int numParams = relevantMethod.parameters.size();
        if(numParams != argumentCount){
            System.out.println("Type error");
            System.exit(1);
        }
        for(int i = 0; i<argumentCount; i++){
            String trueType = relevantMethod.parameters.get(i).type;
            if(!symbolTable.isASubtypeOfB(argumentStorage.get(i), trueType)){
                System.out.println("Type error");
                System.exit(1);
            }
        }

        this.curType = retType;
        argumentStorage.clear();
        
    }
  
     /**
      * f0 -> Expression()
      * f1 -> ( ExpressionRest() )*
      */
     public void visit(ExpressionList n, ClassSymbol my) {
        n.f0.accept(this, my);
        argumentStorage.add(this.curType);
        n.f1.accept(this, my);
     }
  
     /**
      * f0 -> ","
      * f1 -> Expression()
      */
     public void visit(ExpressionRest n, ClassSymbol my) {
        n.f1.accept(this, my);
        argumentStorage.add(this.curType);
     }

   /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public void visit(AssignmentStatement n, ClassSymbol my) {
        n.f0.accept(this, my);
        String firstType = this.curType;
        n.f2.accept(this, my);
        String secondType = this.curType;
        if(!symbolTable.isASubtypeOfB(secondType, firstType)){
            System.out.println("Type error");
            System.exit(1);
        }
     }     

   /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    public void visit(ArrayAssignmentStatement n, ClassSymbol my) {
        n.f0.accept(this, my);
        String arrType = this.curType;
        n.f2.accept(this, my);
        String indexInt = this.curType;
        n.f5.accept(this, my);
        String rightType = this.curType;

        if(!(arrType.equals("array") && indexInt.equals("int") && rightType.equals("int"))){
            System.out.println("Type error");
            System.exit(1);
        }
        
     }     

   /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    public void visit(IfStatement n, ClassSymbol my) {
        n.f2.accept(this, my);
        String parenType = this.curType;
        if(!parenType.equals("bool")){
            System.out.println("Type error");
            System.exit(1);
        }
        n.f4.accept(this, my);
        n.f6.accept(this, my);
     }
    
   /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public void visit(WhileStatement n, ClassSymbol my) {
        n.f2.accept(this, my);
        if(!this.curType.equals("bool")){
            System.out.println("Type error");
            System.exit(1);
        }
        n.f4.accept(this, my);
     }

   /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    public void visit(PrintStatement n, ClassSymbol my) {
        n.f2.accept(this, my);
        if(!this.curType.equals("int")){
            System.out.println("Type error");
            System.exit(1);
        }
     }

}

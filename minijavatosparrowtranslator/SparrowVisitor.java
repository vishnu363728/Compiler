import minijava.syntaxtree.*;
import minijava.visitor.*;
import java.util.*;

public class SparrowVisitor extends GJNoArguDepthFirst<SparrowFile>{

   
    ClassVisitor symbtable;    
    ClassSymbol currentClass;
    String currentID;
    boolean specialFlag = false;
    boolean lhsFlag = false;
    boolean typeFlag = false;

    public SparrowVisitor(ClassVisitor symbtable){
        this.symbtable = symbtable;
    }
    /**
    * f0 -> MainClass()
    * f1 -> ( TypeDeclaration() )*
    * f2 -> <EOF>
    */
   public SparrowFile visit(Goal n) {
    
      SparrowFile mainClass = n.f0.accept(this);

      for(Node classy : n.f1.nodes){
        SparrowFile classFile = classy.accept(this);
        mainClass.concatenateLinesAfter(classFile);
    }      
    
      for(String line : mainClass.lines){
        System.out.println(line);
      }
      return mainClass;
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
    public SparrowFile visit(MainClass n) {        
        SparrowFile mainFile = new SparrowFile();
        n.f1.accept(this);
        this.currentClass = this.symbtable.getClassFromName(this.currentID);
        mainFile.lines.add(SparrowFile.functionDeclaration("Main", "", new ArrayList<String>()));


        for(Node stmt : n.f15.nodes){
            SparrowFile stmtFile = stmt.accept(this);
            mainFile.concatenateLinesAfter(stmtFile);
        }

        mainFile.lines.add("a0 = 0");
        mainFile.lines.add("return a0");

        return mainFile;
    }


   /**
    * f0 -> ClassDeclaration()
    *       | ClassExtendsDeclaration()
    */
    public SparrowFile visit(TypeDeclaration n) {
        return n.f0.accept(this);
     }

    /**
    * f0 -> "class"
    * f1 -> Identifier()
    * f2 -> "{"
    * f3 -> ( VarDeclaration() )*
    * f4 -> ( MethodDeclaration() )*
    * f5 -> "}"
    */
   public SparrowFile visit(ClassDeclaration n) {
        n.f1.accept(this);
        this.currentClass = this.symbtable.getClassFromName(this.currentID);
        
        SparrowFile methods = new SparrowFile();

        for(Node me : n.f4.nodes){
            SparrowFile methodFile = me.accept(this);
            methods.concatenateLinesAfter(methodFile);
        }

        return methods;
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
   public SparrowFile visit(ClassExtendsDeclaration n) {
        n.f1.accept(this);
        this.currentClass = this.symbtable.getClassFromName(this.currentID);

        SparrowFile methods = new SparrowFile();

        for(Node me : n.f6.nodes){
            SparrowFile methodFile = me.accept(this);
            methods.concatenateLinesAfter(methodFile);
        }
        
        return methods;
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
   public SparrowFile visit(MethodDeclaration n) {
        SparrowFile methodFile = new SparrowFile();

        n.f2.accept(this);
        this.currentClass.currentMethod = this.currentClass.containsMethod(this.currentID);
        methodFile.lines.add(SparrowFile.functionDeclaration(this.currentClass.name, this.currentClass.currentMethod.name, this.currentClass.currentMethod.getParameterRegisters()));
        String zero = SparrowFile.randomVarName();
        methodFile.lines.add(SparrowFile.equalsStmt(zero, Integer.toString(0)));
        for(TypeSymbol element : this.currentClass.currentMethod.localVariables){
            methodFile.lines.add(SparrowFile.equalsStmt(element.sparrowVariable, zero));
        }
        SparrowFile stmts = new SparrowFile();
        for(Node stmt : n.f8.nodes){
            SparrowFile stmtFile = stmt.accept(this);
            stmts.concatenateLinesAfter(stmtFile);
        }

        SparrowFile retExpr = n.f10.accept(this);
        String retVariable = retExpr.var;
        methodFile.concatenateLinesAfter(stmts);
        methodFile.concatenateLinesAfter(retExpr);
        methodFile.lines.add(SparrowFile.returnStatement(retVariable));
        return methodFile;
    }

    /**
    * f0 -> <IDENTIFIER>
    */
    public SparrowFile visit(Identifier n) {
        this.currentID = n.f0.toString();
        if(!specialFlag){
            return null;
        }
        else{
            SparrowFile ret = this.retrieveVariableFromID(this.currentID, this.lhsFlag, this.currentClass.name, "this");
            if(this.typeFlag){
                String ident = this.currentClass.currentMethod.findIdentifierType(this.currentID);
                ret.retType = ident;
            }
            return ret;
        }
    }

    /**
    * f0 -> Block()
    *       | AssignmentStatement()
    *       | ArrayAssignmentStatement()
    *       | IfStatement()
    *       | WhileStatement()
    *       | PrintStatement()
    */
    public SparrowFile visit(Statement n) {
        SparrowFile stmtFile = n.f0.accept(this);
        return stmtFile;
    }

        /**
        * f0 -> "{"
        * f1 -> ( Statement() )*
        * f2 -> "}"
        */
    public SparrowFile visit(Block n) {
    
        SparrowFile blockFile = new SparrowFile();
        for(Node stmt : n.f1.nodes){
            SparrowFile stmtFile = stmt.accept(this);
            blockFile.concatenateLinesAfter(stmtFile);
        }
        return blockFile;
    }

        /**
        * f0 -> Identifier()
        * f1 -> "="
        * f2 -> Expression()
        * f3 -> ";"
        */
    public SparrowFile visit(AssignmentStatement n) {
        SparrowFile asgmt = new SparrowFile();

        //lhs flag makes sure we get actual pointer for assignment 
        this.lhsFlag = true;
        this.specialFlag = true;
        this.typeFlag = true;
        SparrowFile lhsCode = n.f0.accept(this);
        String gotId = this.currentID;
        this.typeFlag = false;
        String desiredLhsClass = lhsCode.retType;
        this.specialFlag = false;
        
        asgmt.concatenateLinesAfter(lhsCode);
        String lhsVar = lhsCode.var;

        SparrowFile expr = n.f2.accept(this);
        String typeOfRHS = expr.retType;

        asgmt.concatenateLinesAfter(expr);

        //quick null pointer checking 
        String typeOfLhs = this.currentClass.currentMethod.findIdentifierType(gotId);
        if((typeOfLhs != null) && (!typeOfLhs.equals("int")) && (!typeOfLhs.equals("bool"))){
            generateNullPointerError(expr.var);
        }

        String currentVariable = SparrowFile.randomVarName();
        asgmt.lines.add(SparrowFile.equalsStmt(currentVariable, expr.var));
        //currentVariable now stores the result of the right hand expression
    
        if((typeOfRHS != null) && (!typeOfRHS.equals("int")) &&(!typeOfRHS.equals("bool")) && (!typeOfRHS.equals("array"))){
            this.currentClass.currentMethod.changeType(gotId, typeOfRHS);
        }

        

        

        
        asgmt.lines.add(SparrowFile.equalsStmt(lhsVar, currentVariable));
        

        return asgmt;
    }

    public SparrowFile retrieveVariableFromID(String id, boolean lhs, String class_name, String classPointer){
        SparrowFile retr = new SparrowFile();
        String variableAssumingLocal = this.currentClass.currentMethod.findIdOfMethodVar(id);
        if(true && (variableAssumingLocal != null)){
            retr.var = variableAssumingLocal;
            return retr;
        }
        else{
            return symbtable.getMemberVariable(class_name, id, classPointer, lhs);
        }
    }

    public SparrowFile generateNullPointerError(String pointerToObject){
        SparrowFile gen = new SparrowFile();
        String goto1 = SparrowFile.randomVarName();
        String goto2 = SparrowFile.randomVarName();
        gen.lines.add(SparrowFile.ifstmt(pointerToObject, goto1));
        gen.lines.add(SparrowFile.gotoStmt(goto2));
        gen.lines.add(SparrowFile.linex(goto1));
        gen.lines.add("error(\"null pointer exception\")");
        gen.lines.add(SparrowFile.linex(goto2));

        return gen;

    }


    public SparrowFile generateArrayIfs(String condition){
        SparrowFile gen = new SparrowFile();
        String goto1 = SparrowFile.randomVarName();
        gen.lines.add(SparrowFile.ifstmt(condition, goto1));
        gen.lines.add("error(\"array index out of bounds\")");
        gen.lines.add(SparrowFile.linex(goto1));
        return gen;
    }

    public SparrowFile generateArrayError(String pointerToArray, String idOfIndexToTest){
        SparrowFile err = new SparrowFile();
        String lenOfArr = SparrowFile.randomVarName();
        String bigCond = SparrowFile.randomVarName();
        String smallCond = SparrowFile.randomVarName();
        String zero = SparrowFile.randomVarName();
        String one = SparrowFile.randomVarName();
        String firstt = SparrowFile.randomVarName();
        err.lines.add(SparrowFile.equalsStmt(zero, Integer.toString(0)));
        err.lines.add(SparrowFile.equalsStmt(lenOfArr, SparrowFile.brackets(SparrowFile.addition(pointerToArray,Integer.toString(0)))));
        err.lines.add(SparrowFile.equalsStmt(one, Integer.toString(1)));
        err.lines.add(SparrowFile.equalsStmt(firstt, SparrowFile.addition(one, idOfIndexToTest)));
        err.lines.add(SparrowFile.equalsStmt(bigCond, SparrowFile.cmp(lenOfArr, firstt)));
        err.lines.add(SparrowFile.equalsStmt(smallCond, SparrowFile.cmp(idOfIndexToTest, zero)));

        err.concatenateLinesAfter(this.generateArrayIfs(bigCond));
        err.concatenateLinesAfter(this.generateArrayIfs(smallCond));

        return err;        
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
    public SparrowFile visit(ArrayAssignmentStatement n) {
        SparrowFile arr = new SparrowFile();
        
        //This gets the code for pointer to the array marked by the identifier
        this.lhsFlag = false;
        this.specialFlag = true;
        SparrowFile arrFile = n.f0.accept(this);
        this.specialFlag = false;
        //This is the register that the actual pointer is housed 
        String pointerToArray = arrFile.var;


        //append the necessary code the retrieve the pointer
        arr.concatenateLinesAfter(arrFile);
        arr.concatenateLinesAfter(generateNullPointerError(pointerToArray));
        
        //Find the index we are interested in
        SparrowFile result = n.f2.accept(this);
        //Append the code to find the index 
        arr.concatenateLinesAfter(result);
        //Store the index value 
        String ind = result.var;

        //error check 
        arr.concatenateLinesAfter(generateArrayError(pointerToArray, ind));

        //Now, perform the computation for shifting the array pointer by 4 + 4bytes*index
        String four = SparrowFile.randomVarName();
        String mult = SparrowFile.randomVarName();
        String finalShift = SparrowFile.randomVarName();
        arr.lines.add(SparrowFile.equalsStmt(four, Integer.toString(4)));
        arr.lines.add(SparrowFile.equalsStmt(mult,SparrowFile.mult(four, ind)));
        arr.lines.add(SparrowFile.equalsStmt(finalShift, SparrowFile.addition(mult, four)));

        //Add the shift to the array pointer 
        arr.lines.add(SparrowFile.equalsStmt(finalShift, SparrowFile.addition(pointerToArray,finalShift)));

        //So now that we know what to shift our array by, move on to the rhs 
        SparrowFile rhs = n.f5.accept(this);
        arr.concatenateLinesAfter(rhs);
        String rhsVar = rhs.var;
        arr.lines.add(SparrowFile.equalsStmt(SparrowFile.brackets(SparrowFile.addition(finalShift,Integer.toString(0))), rhsVar));
        return arr;
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
    public SparrowFile visit(IfStatement n) {
        //Get the code for both branches 
        SparrowFile ifBranch = n.f4.accept(this);
        SparrowFile elseBranch = n.f6.accept(this);

        SparrowFile ifstmt = new SparrowFile();
        SparrowFile cond = n.f2.accept(this);
        String condVar = cond.var;
        ifstmt.concatenateLinesAfter(cond);
        String ones = SparrowFile.randomVarName();
        ifstmt.lines.add(SparrowFile.equalsStmt(ones, Integer.toString(1)));
        ifstmt.lines.add(SparrowFile.equalsStmt(condVar, SparrowFile.subtract(ones, condVar)));
        String firstGoto = SparrowFile.randomVarName();

        String thirdGoto = SparrowFile.randomVarName();        
        ifstmt.lines.add(SparrowFile.ifstmt(condVar, firstGoto));
        ifstmt.concatenateLinesAfter(elseBranch);
        ifstmt.lines.add(SparrowFile.gotoStmt(thirdGoto));
        ifstmt.lines.add(SparrowFile.linex(firstGoto));
        ifstmt.concatenateLinesAfter(ifBranch);
        ifstmt.lines.add(SparrowFile.linex(thirdGoto));

        return ifstmt;
    }

        /**
        * f0 -> "while"
        * f1 -> "("
        * f2 -> Expression()
        * f3 -> ")"
        * f4 -> Statement()
        */
    public SparrowFile visit(WhileStatement n) {
        SparrowFile whileStmt = new SparrowFile();
        SparrowFile compsToGetCond = n.f2.accept(this);
        String cond = compsToGetCond.var;
        String firstLine = SparrowFile.randomVarName();
        String secondLine = SparrowFile.randomVarName();
        whileStmt.lines.add(SparrowFile.linex(firstLine));
        whileStmt.concatenateLinesAfter(compsToGetCond);
        whileStmt.lines.add(SparrowFile.ifstmt(cond, secondLine));
        SparrowFile aStuff = n.f4.accept(this);
        whileStmt.concatenateLinesAfter(aStuff);
        whileStmt.lines.add(SparrowFile.gotoStmt(firstLine));
        whileStmt.lines.add(SparrowFile.linex(secondLine));

        return whileStmt;
    }

        /**
        * f0 -> "System.out.println"
        * f1 -> "("
        * f2 -> Expression()
        * f3 -> ")"
        * f4 -> ";"
        */
    public SparrowFile visit(PrintStatement n) {
        
        SparrowFile printer = new SparrowFile();
        SparrowFile expr = n.f2.accept(this);        
        String printable = expr.var;
        printer.concatenateLinesAfter(expr);
        printer.lines.add(SparrowFile.printStmt(printable));
        return printer;
        
    }

        /**
        * f0 -> AndExpression()
        *       | CompareExpression()
        *       | PlusExpression()
        *       | MinusExpression()
        *       | TimesExpression()
        *       | ArrayLookup()
        *       | ArrayLength()
        *       | MessageSend()
        *       | PrimaryExpression()
        */
    public SparrowFile visit(Expression n) {
        return n.f0.accept(this);
    }

        /**
        * f0 -> PrimaryExpression()
        * f1 -> "&&"
        * f2 -> PrimaryExpression()
        */
    public SparrowFile visit(AndExpression n) {
  
        String expressionResult = SparrowFile.randomVarName();
        String goto1 = SparrowFile.randomVarName();
        String goto2 = SparrowFile.randomVarName();
        String cond = SparrowFile.randomVarName();
        String one = SparrowFile.randomVarName();

        //store computations 
        SparrowFile andExpr = new SparrowFile();
        SparrowFile first = n.f0.accept(this);
        String a = first.var;
        SparrowFile second = n.f2.accept(this);
        String b = second.var;

        //Compute a 
        andExpr.concatenateLinesAfter(first);
        //Initalize a variable with 1 in it
        andExpr.lines.add(SparrowFile.equalsStmt(one,Integer.toString(1)));
        //Store a in cond
        andExpr.lines.add(SparrowFile.equalsStmt(cond, a));
        //If statement that zaps to goto1 if a is false 
        andExpr.lines.add(SparrowFile.ifstmt(cond, goto1));
        //Compute b and put it in result, then leave
        andExpr.concatenateLinesAfter(second);
        andExpr.lines.add(SparrowFile.equalsStmt(expressionResult, b));
        andExpr.lines.add(SparrowFile.gotoStmt(goto2));
        //if in goto1, set result to 0
        andExpr.lines.add(SparrowFile.linex(goto1));
        andExpr.lines.add(SparrowFile.equalsStmt(expressionResult,cond));
        //if goto2, leave
        andExpr.lines.add(SparrowFile.linex(goto2));

        andExpr.var = expressionResult;
        return andExpr;

    }

   /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public SparrowFile visit(CompareExpression n) {
        SparrowFile cmp = new SparrowFile();
        SparrowFile first = n.f0.accept(this);
        String firstVar = first.var;
        SparrowFile second = n.f2.accept(this);
        String secondVar = second.var;
        String result = SparrowFile.randomVarName();

        cmp.concatenateLinesAfter(first);
        cmp.concatenateLinesAfter(second);
        cmp.lines.add(SparrowFile.equalsStmt(result, SparrowFile.cmp(firstVar,secondVar)));
        
        cmp.var = result;
        return cmp;
     }

        /**
        * f0 -> PrimaryExpression()
        * f1 -> "+"
        * f2 -> PrimaryExpression()
        */
    public SparrowFile visit(PlusExpression n) {
        SparrowFile plus = new SparrowFile();
        SparrowFile first = n.f0.accept(this);
        String firstVar = first.var;
        SparrowFile second = n.f2.accept(this);
        String secondVar = second.var;
        String result = SparrowFile.randomVarName();
        plus.concatenateLinesAfter(first);
        plus.concatenateLinesAfter(second);

        plus.lines.add(SparrowFile.equalsStmt(result,SparrowFile.addition(firstVar, secondVar)));
        plus.var = result;
        return plus;
    }

        /**
        * f0 -> PrimaryExpression()
        * f1 -> "-"
        * f2 -> PrimaryExpression()
        */
    public SparrowFile visit(MinusExpression n) {
        SparrowFile plus = new SparrowFile();
        SparrowFile first = n.f0.accept(this);
        String firstVar = first.var;
        SparrowFile second = n.f2.accept(this);
        String secondVar = second.var;
        String result = SparrowFile.randomVarName();
        plus.concatenateLinesAfter(first);
        plus.concatenateLinesAfter(second);

        plus.lines.add(SparrowFile.equalsStmt(result,SparrowFile.subtract(firstVar, secondVar)));
        plus.var = result;
        return plus;
    }

        /**
        * f0 -> PrimaryExpression()
        * f1 -> "*"
        * f2 -> PrimaryExpression()
        */
    public SparrowFile visit(TimesExpression n) {
        SparrowFile plus = new SparrowFile();
        SparrowFile first = n.f0.accept(this);
        String firstVar = first.var;
        SparrowFile second = n.f2.accept(this);
        String secondVar = second.var;
        String result = SparrowFile.randomVarName();
        plus.concatenateLinesAfter(first);
        plus.concatenateLinesAfter(second);

        plus.lines.add(SparrowFile.equalsStmt(result,SparrowFile.mult(firstVar, secondVar)));
        plus.var = result;
        return plus;
    }

/**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public SparrowFile visit(ArrayLookup n) {
        SparrowFile arr = new SparrowFile();
        SparrowFile computeArrayPtr = n.f0.accept(this);
        String arrayPtr = computeArrayPtr.var;

        SparrowFile indexComp = n.f2.accept(this);
        String index = indexComp.var;

        

        arr.concatenateLinesAfter(computeArrayPtr);
        arr.concatenateLinesAfter(generateNullPointerError(arrayPtr));
        arr.concatenateLinesAfter(indexComp);

        arr.concatenateLinesAfter(generateArrayError(arrayPtr, index));
        //Now, perform the computation for shifting the array pointer by 4 + 4bytes*index
        String four = SparrowFile.randomVarName();
        String mult = SparrowFile.randomVarName();
        String finalShift = SparrowFile.randomVarName();
        arr.lines.add(SparrowFile.equalsStmt(four, Integer.toString(4)));
        arr.lines.add(SparrowFile.equalsStmt(mult,SparrowFile.mult(four, index)));
        arr.lines.add(SparrowFile.equalsStmt(finalShift, SparrowFile.addition(mult, four)));
        arr.lines.add(SparrowFile.equalsStmt(finalShift, SparrowFile.addition(arrayPtr,finalShift)));
        String result = SparrowFile.randomVarName();

        arr.lines.add(SparrowFile.equalsStmt(result, SparrowFile.brackets(SparrowFile.addition(finalShift,Integer.toString(0)))));

        arr.var = result;
        return arr;
                
    }

/**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public SparrowFile visit(ArrayLength n) {
        SparrowFile arr = new SparrowFile();
        SparrowFile computeArrayPtr = n.f0.accept(this);
        String arrayPtr = computeArrayPtr.var;
        arr.concatenateLinesAfter(computeArrayPtr);
        String result = SparrowFile.randomVarName();
        arr.lines.add(SparrowFile.equalsStmt(result, SparrowFile.brackets(SparrowFile.addition(arrayPtr,Integer.toString(0)))));

        arr.var = result;
        return arr;
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
    */
    public SparrowFile visit(PrimaryExpression n) {
        this.specialFlag = true;
        this.typeFlag = true;
        this.lhsFlag = false;
        SparrowFile ident = n.f0.accept(this);
        this.typeFlag = false;
        this.specialFlag = false;

        return ident;
     }

/**
    * f0 -> <INTEGER_LITERAL>
    */
    public SparrowFile visit(IntegerLiteral n) {
        SparrowFile inter = new SparrowFile();
        String ret = SparrowFile.randomVarName();
        String a = n.f0.toString();
        inter.lines.add(SparrowFile.equalsStmt(ret, a));
        inter.var = ret;
        return inter;

    }
  
/**
    * f0 -> "true"
    */
    public SparrowFile visit(TrueLiteral n) {
        SparrowFile lit = new SparrowFile();
        String ret = SparrowFile.randomVarName();
        lit.lines.add(SparrowFile.equalsStmt(ret, Integer.toString(1)));
        lit.var = ret;
        return lit;
     }


/**
    * f0 -> "false"
    */
    public SparrowFile visit(FalseLiteral n) {
        SparrowFile lit = new SparrowFile();
        String ret = SparrowFile.randomVarName();
        lit.lines.add(SparrowFile.equalsStmt(ret, Integer.toString(0)));
        lit.var = ret;
        return lit;
     }     

/**
    * f0 -> "this"
    */
    public SparrowFile visit(ThisExpression n) {
        SparrowFile te = new SparrowFile();
        te.var = "this";
        te.retType = this.currentClass.name;
        return te;
     }

/**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public SparrowFile visit(ArrayAllocationExpression n) {
        SparrowFile alo = new SparrowFile();
        String allocAmount = SparrowFile.randomVarName();
        String finalPointer = SparrowFile.randomVarName();
        String arlen = SparrowFile.randomVarName();

        SparrowFile expr = n.f3.accept(this);
        String space = expr.var;
        
        alo.concatenateLinesAfter(expr);
        alo.lines.add(SparrowFile.equalsStmt(allocAmount, Integer.toString(4)));
        alo.lines.add(SparrowFile.equalsStmt(arlen, space));
        alo.lines.add(SparrowFile.equalsStmt(space, SparrowFile.mult(allocAmount, space)));
        alo.lines.add(SparrowFile.equalsStmt(allocAmount, SparrowFile.addition(allocAmount, space)));
        alo.lines.add(SparrowFile.equalsStmt(finalPointer, SparrowFile.alloc(allocAmount)));
        alo.lines.add(SparrowFile.equalsStmt(SparrowFile.brackets(SparrowFile.addition(finalPointer, Integer.toString(0))), arlen));
        alo.var = finalPointer;
        return alo;
        
     }
  
/**
    * f0 -> "!"
    * f1 -> Expression()
    */
    public SparrowFile visit(NotExpression n) {
        SparrowFile not = new SparrowFile();
        SparrowFile expr = n.f1.accept(this);
        String exprRes = expr.var;
        String result = SparrowFile.randomVarName();
        String one = SparrowFile.randomVarName();
        not.concatenateLinesAfter(expr);
        not.lines.add(SparrowFile.equalsStmt(one, Integer.toString(1)));
        not.lines.add(SparrowFile.equalsStmt(result, SparrowFile.subtract(one, exprRes)));
        not.var = result;
        return not;
     }
  
     /**
      * f0 -> "("
      * f1 -> Expression()
      * f2 -> ")"
      */
     public SparrowFile visit(BracketExpression n) {
        return n.f1.accept(this);
     }

        /**
        * f0 -> PrimaryExpression()
        * f1 -> "."
        * f2 -> Identifier()
        * f3 -> "("
        * f4 -> ( ExpressionList() )?
        * f5 -> ")"
        */
    public SparrowFile visit(MessageSend n) {
        
        SparrowFile msg = new SparrowFile();
        
        this.typeFlag = true;
        SparrowFile found = n.f0.accept(this);
        this.typeFlag = false;

        //System.out.println(found.retType);

        //compute the object pointer and add to our file 
        msg.concatenateLinesAfter(found);
        String objectPointer = found.var;
        String objectType = found.retType;
        msg.concatenateLinesAfter(generateNullPointerError(objectPointer));
        
        n.f2.accept(this);
        String methodName = this.currentID;


        ArrayList<String> pStruct = new ArrayList<String>();
        

        SparrowFile procureMethodId = this.symbtable.getMemberMethod(objectType, methodName, objectPointer);
        pStruct.add(procureMethodId.classPtr);
        msg.retType = procureMethodId.retType;
        msg.concatenateLinesAfter(procureMethodId);
        String actualMethodIdToFeed = procureMethodId.var;


        SparrowFile parameters = n.f4.accept(this);
        msg.concatenateLinesAfter(parameters);
        if(parameters != null){
            for(String element : parameters.paramz){
                pStruct.add(element);
            }
        }

        String result = SparrowFile.randomVarName();
        msg.lines.add(SparrowFile.equalsStmt(result, SparrowFile.callq(actualMethodIdToFeed,pStruct)));
        msg.var = result;
        
        return msg;
        
    }

/**
    * f0 -> Expression()
    * f1 -> ( ExpressionRest() )*
    */
    public SparrowFile visit(ExpressionList n) {
        
        SparrowFile maker = new SparrowFile();
        SparrowFile expr = n.f0.accept(this);     
        maker.paramz.add(expr.var);
        maker.concatenateLinesAfter(expr);


        for(Node rest : n.f1.nodes){
            SparrowFile eFile = rest.accept(this);
            maker.concatenateLinesAfter(eFile);
            maker.paramz.add(eFile.var);
        }

        return maker;
     }
  
     /**
      * f0 -> ","
      * f1 -> Expression()
      */
     public SparrowFile visit(ExpressionRest n) {
        return n.f1.accept(this);        
     }

   /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public SparrowFile visit(AllocationExpression n) {
        n.f1.accept(this);
        String nameOfClass = this.currentID;
        ClassSymbol relevant = symbtable.getClassFromName(nameOfClass);
        SparrowFile ret = relevant.instantiationSparrow();
        ret.retType = nameOfClass;
        return ret;
      
    }
}

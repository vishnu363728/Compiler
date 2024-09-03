import java.util.*;

public class SparrowFile {
    public ArrayList<String> lines = new ArrayList<String>();
    public String var;
    public static int wordCount = 0;
    public String retType = null;
    public String classPtr = null;
    public ArrayList<String> paramz = new ArrayList<String>();
    
    public static String randomVarName(){
        String varname = "v" + Integer.toString(wordCount);
        wordCount++;
        return varname;
    }

    public static String equals(){
        return " = ";
    }

    public static String equalsStmt(String a, String b){
        return a + equals() + b;
    }

    public static String brackets(String param){
        return "[ " + param + " ]";
    }

    public static String callq(String msgId, ArrayList<String> params){
        String builder = "( ";
        for(String element : params){
            builder += element;
            builder += " ";
        }
        builder += ")";
        return "call " + msgId + builder;
    }

    public static String addition(String param1, String param2){
        return param1 + " + " + param2;
    }

    public static String subtract(String param1, String param2){
        return param1 + " - " + param2;
    }

    public static String ifstmt(String condVar, String ifGoto){
        return "if0 " + condVar + " goto " + ifGoto;
    }

    public static String printStmt(String id){
        return "print( " + id + " )";
    }

    public static String linex(String gotoName){
        return gotoName + ": ";
    }

    public static String cmp(String a, String b){
        return a + " < " + b;
    }

    public static String gotoStmt(String gotoname){
        return "goto " + gotoname;
    }
    public static String mult(String a, String b){
        return a + " * " + b;
    }

    public static String alloc(String param){
        return "alloc(" + param + ")";
    }

    public static String functionDeclaration(String className, String functionName, ArrayList<String> params){

        String paramsString = "";
        for(String element : params){
            paramsString += element + " ";
        }
        //paramsString = paramsString.substring(0,paramsString.length() - 1);
        return "func " + className + functionName + "(" + paramsString  + ")";
    }

    public static String returnStatement(String retVariable){
        return "return " + retVariable;
    }
    public void concatenateLinesBefore(SparrowFile first){

        ArrayList<String> copy = new ArrayList<String>();
        for(String line : first.lines){
            copy.add(line);
        }
        for(String line : this.lines){
            copy.add(line);
        }

        this.lines = copy;
    }

    public void concatenateLinesAfter(SparrowFile second){
        if(second == null){
            return;
        }
        for(String line : second.lines){
            this.lines.add(line);
        }
    }

    public void print(){
        for(String element : this.lines){
            System.out.println(element);
        }
    }
    

}

import minijava.MiniJavaParser;
import minijava.syntaxtree.*;
import minijava.visitor.*;
import minijava.syntaxtree.Node;
import minijava.visitor.GJDepthFirst;
import java.io.*;
import java.util.*;

public class Typecheck{
  public static void main(String [] args) {
        /* This is a placeholder file,
            please create a new class with required name for the homework
            and remove this file */
            InputStream in = System.in;

            //Node root;
            try{
              Goal root = new MiniJavaParser(in).Goal();
              //Setting up the Symbol tables first
              ClassVisitor symbtable = new ClassVisitor();
              root.accept(symbtable, null);
              //symbtable.print();     
              TypeVisitor cooker = new TypeVisitor(symbtable);
              
              root.accept(cooker, null);
            }
            catch(Exception e){
              System.out.println("Type error");
              System.exit(1);
            }

  }
}
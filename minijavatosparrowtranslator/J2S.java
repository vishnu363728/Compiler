import java.util.*;
import minijava.MiniJavaParser;
import minijava.syntaxtree.*;
import minijava.visitor.*;

public class J2S{
  public static void main(String [] args) {
    //Node root;
    try{
      Goal root = new MiniJavaParser(System.in).Goal();

      ClassVisitor cv = new ClassVisitor();
      root.accept(cv,null);
      //cv.print();

      SparrowVisitor cooker = new SparrowVisitor(cv);
      root.accept(cooker);
    }
    catch(Exception e){
      e.printStackTrace();
      System.exit(1);
    }
  }
}


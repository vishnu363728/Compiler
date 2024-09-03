import java.io.InputStream;
import java.util.*;
import IR.ParseException;
import IR.SparrowParser;
import IR.syntaxtree.Node;
import IR.visitor.SparrowConstructor;
import IR.visitor.Visitor;
import sparrow.Program;

public class S2SV{
  public static void main(String [] args) throws Throwable{
    InputStream in = System.in;
    new SparrowParser(in);
    Node root = SparrowParser.Program();
    SparrowConstructor constructor = new SparrowConstructor();
    root.accept(constructor);
    Program program = constructor.getProgram();

    SparrowVisitor mine = new SparrowVisitor();
    program.accept(mine);
    //mine.print();
    SparrowTranslator cooker = new SparrowTranslator(mine);
    program.accept(cooker);
    cooker.print();

        /* This is a placeholder file,
            please create a new class with required name for the homework
            and remove this file */
  }
}
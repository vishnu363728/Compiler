import java.io.InputStream;
import java.io.IOException;
import java.util.HashMap;

import IR.ParseException;
import IR.SparrowParser;
import IR.registers.Registers;
import IR.syntaxtree.Node;
import IR.visitor.SparrowVConstructor;
import sparrowv.Program;

public class SV2V{
    public static void main(String [] args) throws Throwable{
        Registers.SetRiscVregs();
        InputStream in = System.in;
        new SparrowParser(in);
        Node root = SparrowParser.Program();
        SparrowVConstructor constructor = new SparrowVConstructor();
        root.accept(constructor);
        Program program = constructor.getProgram();

        SparrowVisitor mine = new SparrowVisitor();
        program.accept(mine);
        //mine.print();
        SparrowTranslator my = new SparrowTranslator(mine);
        program.accept(my);
        my.print();

  }

    

}

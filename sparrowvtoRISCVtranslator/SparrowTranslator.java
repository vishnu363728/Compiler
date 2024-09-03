import java.util.ArrayList;
import java.util.HashMap;
import sparrowv.visitor.*;
import sparrowv.*;
import IR.token.Identifier;


public class SparrowTranslator extends DepthFirst{
    public ArrayList<HashMap<String, String>> methodMappings;
    public ArrayList<Integer> spacePerMethod;
    public ArrayList<Integer> paramSpacePerMethod;
    public HashMap<String, String> currentMapping;
    public int currentParamCount;
    public int currentCount;
    public String curMethod;

    public ArrayList<String> lines = new ArrayList<>();
    public ArrayList<String> endList = new ArrayList<>();
    public int varcount = 0;

    public SparrowTranslator(SparrowVisitor my){
        this.methodMappings = my.methodMappings;
        this.spacePerMethod = my.spacePerMethod;
        this.paramSpacePerMethod = my.paramSpacePerMethod;
    }

    public void print(){
        for(String line : this.lines){
            System.out.println(line);
        }
    }

        /*   List<FunctionDecl> funDecls; */
    public void visit(Program n) {
        int i = 0;
        endList.add(".globl error");
        endList.add("error:");
        endList.add("  mv a1, a0");
        endList.add("  li a0, @print_string");
        endList.add("  ecall");
        endList.add("  li a1, 10");
        endList.add("  li a0, @print_char");
        endList.add("  ecall");
        endList.add("  li a0, @exit");
        endList.add("  ecall");
        endList.add("abort_17:");
        endList.add("  j abort_17");
        endList.add("");
        endList.add(".globl alloc");
        endList.add("alloc:");
        endList.add("  mv a1, a0");
        endList.add("  li a0, @sbrk");
        endList.add("  ecall");
        endList.add("  jr ra");
        endList.add("");
        endList.add(".data");
        for (FunctionDecl fd: n.funDecls) {
            if(i == 0){
                this.lines.add(".equiv @sbrk, 9");
                this.lines.add(".equiv @print_string, 4");
                this.lines.add(".equiv @print_char, 11");
                this.lines.add(".equiv @print_int, 1");
                this.lines.add(".equiv @exit 10");
                this.lines.add(".equiv @exit2, 17");
                this.lines.add("");
                this.lines.add(".text");
                this.lines.add("jal " + fd.functionName.toString());
                this.lines.add("li a0, @exit");
                this.lines.add("ecall");
                this.lines.add("");
            }
            this.lines.add("");
            this.lines.add(".globl " + fd.functionName.toString());
            curMethod = fd.functionName.toString();
            this.lines.add(fd.functionName.toString() + ":");

            this.lines.add("sw fp, -8(sp)");
            this.lines.add("mv fp, sp");
            currentMapping = this.methodMappings.get(i);
            currentParamCount = this.paramSpacePerMethod.get(i);
            currentCount = this.spacePerMethod.get(i);
            this.lines.add("li a1, " + Integer.toString(currentCount * -1));
            this.lines.add("sub sp, sp, a1");
            this.lines.add("sw ra, -4(fp)");
            fd.accept(this);
            i += 1;
        }
        this.cleanup();
    }

    /*   Program parent;
    *   FunctionName functionName;
    *   List<Identifier> formalParameters;
    *   Block block; */
    public void visit(FunctionDecl n) {
        for (Identifier fp: n.formalParameters) {
            // ... fp ...
        }
        n.block.accept(this);
    }

    /*   FunctionDecl parent;
    *   List<Instruction> instructions;
    *   Identifier return_id; */
    public void visit(Block n) {
        for (Instruction i: n.instructions) {
            i.accept(this);
        }
        lines.add("lw a0, " + this.currentMapping.get(n.return_id.toString()));
        lines.add("lw ra, -4(fp)");
        lines.add("lw fp, -8(fp)");
        lines.add("li a1, " + Integer.toString(currentCount * -1));
        lines.add("add sp, sp, a1");
        lines.add("jr ra");
    }

    /*   Label label; */
    public void visit(LabelInstr n) {
        lines.add(n.label.toString() + this.curMethod + ":");
    }

    /*   Register lhs;
    *   int rhs; */
    public void visit(Move_Reg_Integer n) {
        lines.add("li " + n.lhs.toString() + ", " + Integer.toString(n.rhs));
    }

    /*   Register lhs;
    *   FunctionName rhs; */
    public void visit(Move_Reg_FuncName n) {
        lines.add("la " + n.lhs.toString() + ", " + n.rhs.toString());
    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    public void visit(Add n) {
        lines.add("add " + n.lhs.toString() + ", " + n.arg1.toString() + ", " + n.arg2.toString());
    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    public void visit(Subtract n) {
        lines.add("sub " + n.lhs.toString() + ", " + n.arg1.toString() + ", " + n.arg2.toString());
    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    public void visit(Multiply n) {
        lines.add("mul " + n.lhs.toString() + ", " + n.arg1.toString() + ", " + n.arg2.toString());
    }

    /*   Register lhs;
    *   Register arg1;
    *   Register arg2; */
    public void visit(LessThan n) {
        lines.add("slt " + n.lhs.toString() + ", " + n.arg1.toString() + ", " + n.arg2.toString());
    }

    /*   Register lhs;
    *   Register base;
    *   int offset; */
    public void visit(Load n) {
        lines.add("lw " + n.lhs.toString() + ", " + Integer.toString(n.offset) + "(" + n.base.toString() + ")"); 
    }

    /*   Register base;
    *   int offset;
    *   Register rhs; */
    public void visit(Store n) {
        lines.add("sw " + n.rhs.toString() + ", " + Integer.toString(n.offset) + "(" + n.base.toString() + ")"); 
    }

    /*   Register lhs;
    *   Register rhs; */
    public void visit(Move_Reg_Reg n) {
        lines.add("mv " + n.lhs.toString() + ", " + n.rhs.toString()); 
    }

    /*   Identifier lhs;
    *   Register rhs; */
    public void visit(Move_Id_Reg n) {
        String ident = this.currentMapping.get(n.lhs.toString());
        lines.add("sw " + n.rhs.toString() + ", " + ident);
    }

    /*   Register lhs;
    *   Identifier rhs; */
    public void visit(Move_Reg_Id n) {
        String ident = this.currentMapping.get(n.rhs.toString());
        lines.add("lw " + n.lhs.toString() + ", " + ident);
    }

    /*   Register lhs;
    *   Register size; */
    public void visit(Alloc n) {
        lines.add("mv a0, " + n.size.toString());
        lines.add("jal alloc");
        lines.add("mv " + n.lhs.toString() + ", a0");
    }

    /*   Register content; */
    public void visit(Print n) {
        lines.add("mv a1, " + n.content.toString());
        lines.add("li a0, @print_int");
        lines.add("ecall");
        lines.add("li a1, 10");
        lines.add("li a0, 11");
        lines.add("ecall");
    }

    /*   String msg; */
    public void visit(ErrorMessage n) {
        String error_msg = this.generate();
        lines.add("la a0, " + error_msg);
        lines.add("j error");
        endList.add("");
        endList.add(".globl " + error_msg);
        endList.add(error_msg + ":");
        endList.add(".asciiz " + n.msg);
        endList.add(".align 2");
    }

    /*   Label label; */
    public void visit(Goto n) {
        lines.add("j " + n.label.toString() + this.curMethod);
    }

    /*   Register condition;
    *   Label label; */
    public void visit(IfGoto n) {
        String l1 = generate();
        String l2 = generate();
        lines.add("beqz " + n.condition.toString() + ", " + l1);
        lines.add("jal " + l2);
        lines.add(l1 + ":");
        lines.add("jal " + n.label.toString() + this.curMethod);
        lines.add(l2 + ":");
    }

    /*   Register lhs;
    *   Register callee;
    *   List<Identifier> args; */
    public void visit(Call n) {
        int space = 0;
        for(Identifier arg : n.args){
            space += 1;
        }
        space = space * 4;
        lines.add("li a1, " + Integer.toString(space));
        lines.add("sub sp, sp, a1");
        int counter = 0;
        for(Identifier arg : n.args){
            lines.add("lw a1, " + this.currentMapping.get(arg.toString()));
            lines.add("sw a1, " + Integer.toString(counter) + "(sp)");
            counter += 4;
        }
        lines.add("jalr " + n.callee.toString());
        lines.add("mv " + n.lhs.toString() + ", a0");
        lines.add("addi sp, sp, " + space);

    }

    public String generate(){
        this.varcount += 1;
        return "msg_" + Integer.toString(this.varcount);
    }

    public void cleanup(){
        for(String element : this.endList){
            lines.add(element); 
        }
    }
}

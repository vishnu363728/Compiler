import java.util.*;

public class LoopStruct {
    public String loopLabel;
    public int lineNumber;

    public LoopStruct(String f, int s){
        this.loopLabel = f;
        this.lineNumber = s;
    }

    public boolean has(String label){
        if(loopLabel.equals(label)){
            return true;
        }
        return false;
    }
}

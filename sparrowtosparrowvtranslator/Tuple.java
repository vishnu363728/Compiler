import java.util.*;

public class Tuple {
    public String first;
    public String second; 

    public Tuple(String f, String s){
        this.first = f;
        this.second = s;
    }

    public Tuple(String f){
        this.first = f;
    }

    public void print(){
        System.out.println(" first is " + this.first + " and second is " + this.second);
    }
}

class Main {
    public static void main(String[] a){
        System.out.println(new A().run());
    }
}

class B{
    int x;
    public int helper(int param) {
        
        x = param;
        System.out.println(x);
        return x;
    }
}


class A extends B{
    B ba;
    A ap;
    public int run() {
        int[] a;
        int b;
        //ba = ap;
        //a = ba.helper(12);
        b = 0;
        return b;
    }
    

    public int helper(int param) {
        int bpp;
        bpp = 10;
        System.out.println(bpp);
        return bpp;
    }
    
}
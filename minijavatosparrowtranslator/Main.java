class WeirdTest{
	public static void main(String[] args) {
		B b = new B();
		System.out.println(b.run());
		System.out.println(b.foo());
		System.out.println(b.bar());
	}
  }
  
  class A {
	  int[] x;
  
	  public int run() {
		  x = new int[10];
		  x[0] = 1;
		  return x[0];
	  }
  
	  public int bar() {
		  return x[0];
	  }
  }
  
  class B extends A {
	  int[] x;
  
	  public int foo() {
		  x = new int[10];
		  x[0] = 2;
		  return x[0];
	  }
  }
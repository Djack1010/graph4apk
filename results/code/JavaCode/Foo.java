public class Foo { 

	public static void main(String[]args){
		Foo f = new Foo();
		int a = 7;
		int b = 14;
		int x = (f.bar(21)+ a) * b;
	}
	
	public int bar(int n) {
		boolean z = test();
		do{
			z = test();	
		}while(z);
		return 0;
	}
	
	public boolean test() { return true; }
}

package arrays;

public class ArrayTest {
	
	int[] int_array = { 8, 1, 2, 3, 4 };
	
	public static void main(String[] args) {
		ArrayTest a = new ArrayTest();
		a.Run();
	}
	
	void Run() {
		PlayWithArrayManually( int_array );
		PlayWithArrayMacro( int_array );
		PlayWithArrayCritical( int_array );
	}
	
	native void PlayWithArrayManually( int[] int_array );
	
	native void PlayWithArrayMacro( int[] int_array );
	
	native void PlayWithArrayCritical( int[] int_array );

	static {
		System.loadLibrary( "ArrayTest" );
	}
}

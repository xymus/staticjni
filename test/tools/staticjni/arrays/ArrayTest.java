package arrays;

import net.xymus.staticjni.NativeArrayAccess;
import net.xymus.staticjni.NativeCall;

public class ArrayTest {
	
	int[] int_array = { 8, 1, 2, 3, 4 };
	
	public static void main(String[] args) {
		ArrayTest a = new ArrayTest();
		a.Run();
	}
	
	void Run() {
		PlayWithArrayManually( int_array );
		PlayWithArrayMacro( int_array );
	}
	
	@NativeArrayAccess( "int[]" )
	native void PlayWithArrayManually( int[] int_array );
	
	@NativeArrayAccess( "int[]" )
	native void PlayWithArrayMacro( int[] int_array );

	static {
		System.loadLibrary( "ArrayTest" );
	}
}

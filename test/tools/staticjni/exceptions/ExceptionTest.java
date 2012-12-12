package exceptions;


public class ExceptionTest {
	static {
		System.loadLibrary( "ExceptionTest" );
	}
	
	public static void main ( String[] args ) {
		ExceptionTest a = new ExceptionTest();
		a.Run();
	}

	void Run() {
		try {
			ExceptionProne();
			System.out.println( "Expected exception in ExceptionProne" );
		} catch ( MyException ex ) {
			System.out.println( "MyException caught as expected" );
		}
		
		try {
			RuntimeExceptionProne();
			System.out.println( "Expected exception in RuntimeExceptionProne" );
		} catch ( RuntimeException ex ) {
			System.out.println( "RuntimeException caught as expected" );
		}
	}
	
	native void ExceptionProne() throws MyException, RuntimeException;
	
	native void RuntimeExceptionProne() throws RuntimeException;
}

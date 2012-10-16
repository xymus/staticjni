import net.xymus.staticjni.*;

class TestLocal {

	int i = 12345;

	public static void main ( String[] args ) {
		System.out.println( "test local" );

		TestLocal a = new TestLocal();
		a.foo();
		System.out.println( a.bar( 12, 34, 'u' ) );
	}

	protected TestLocal(){}

	protected int javaMeth( int a ) {
		return a * 12;
	}

	@NativeCall( "javaMeth" )
	private native void foo();

	@NativeCalls( {"foo", "i"} )
	private native int bar( int a, int b, char c );

	static {
		System.loadLibrary( "TestLocal" );
	}
}

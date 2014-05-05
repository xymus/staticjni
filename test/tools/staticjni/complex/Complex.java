package complex;

import net.xymus.staticjni.NativeCall;
import net.xymus.staticjni.NativeCalls;
import net.xymus.staticjni.NativeNew;
import net.xymus.staticjni.NativeSuperCall;

public class Complex {
	public static void main ( String[] args ) {
		Complex a = new Complex();
		a.Run();
	}

	public void Run() {
		playWithMethods();
		System.out.println( "hashCode: " + hashCode() );
		System.out.println( "local_sub.i: " + local_sub.i );
	}

	public Sub local_sub;

	/*
	 * sets local_sub = sub
	 * print local_sub.i
	 * local_sub.i = something
	 */
	@NativeCalls( {"local_sub", "complex.Sub i", "playWithConstructors"} )
	public native void playWithFields( Sub sub );

	/*
	 *  PlayWithStatics();
	 *	Sub s = PlayWithConstructors();
	 *	PlayWithFields(s);
	 *  s.Foo();
	 */
	@NativeCalls( {"playWithStatics", "playWithFields", "complex.Sub foo"} )
	public native void playWithMethods();

	/*
	 * Simply call StaticFoo
	 */
	@NativeCalls( {"StaticFoo", "StaticBar"} )
	public native void playWithStatics();
	static public void StaticFoo() {
		System.out.println( "StaticFoo" );
	}
	static native void StaticBar();

	/*
	 * Instantiate a Sub and returns it
	 */
	@NativeNew( "complex.Sub (int)" )
	public native Sub playWithConstructors();

	/*
	 * returns super * 2
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	@NativeSuperCall
	public native int hashCode();

	static {
		System.loadLibrary( "Complex" );
	}
}

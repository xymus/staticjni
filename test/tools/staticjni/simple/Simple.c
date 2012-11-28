#include "Simple.h"

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     Simple
 * Method:    foo
 * Signature: ()V
 * Imported methods: 
 */
void Simple_foo__impl( Simple self )
{
	jint v = Simple_javaMeth( self, 123 );
	printf( "foo %d\n", v );
}

/*
 * Class:     Simple
 * Method:    bar
 * Signature: (IIC)I
 * Imported methods: 
 */
jint Simple_bar__impl( Simple self, jint a, jint b, jchar c)
{
	printf( "bar intro\n" );
	Simple_foo( self );
	printf( "bar %d %d %c\n", a, b, c );
	return a + b;
}

#ifdef __cplusplus
}
#endif

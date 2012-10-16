#include "TestLocal.h"

#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     TestLocal
 * Method:    foo
 * Signature: ()V
 * Imported methods: 
 */
void TestLocal_foo__impl( TestLocal self )
{
	jint v = TestLocal_javaMeth( self, 123 );
	printf( "foo %d\n", v );
}

/*
 * Class:     TestLocal
 * Method:    bar
 * Signature: (IIC)I
 * Imported methods: 
 */
jint TestLocal_bar__impl( TestLocal self, jint a, jint b, jchar c)
{
	printf( "bar intro\n" );
	TestLocal_foo( self );
	printf( "bar %d %d %c\n", a, b, c );
	return a + b;
}

#ifdef __cplusplus
}
#endif

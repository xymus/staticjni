#include "arrays_ArrayTest.h"

/*
 * Class:     arrays_ArrayTest
 * Method:    PlayWithArray
 * Signature: ([I)V
 * Imported methods:
 */
void ArrayTest_PlayWithArrayManually__impl( ArrayTest self, jintArray arr ) {
	int i;

	/* manually */
	jint size = length_jintArray( arr );
	jint *native_arr = get_access_jintArray( arr );
		for ( i = 0; i < size; i ++ ) {
			printf( "%i\n", native_arr[i] );
		}
	release_access_jintArray( arr, native_arr );
}

void ArrayTest_PlayWithArrayMacro__impl( ArrayTest self, jintArray arr ) {
	int i;

	/* with helper macro */
	jint *native_arr;
	jint size;
	access_jintArray( arr, native_arr, size ) {
		for ( i = 0; i < size; i ++ ) {
			printf( "%i\n", native_arr[i] );
		}
	}
}

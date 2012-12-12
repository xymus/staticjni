#include "exceptions_ExceptionTest.h"

/*
 * Class:     exceptions_ExceptionTest
 * Method:    ExceptionProne
 * Signature: ()V
 * Imported methods:
 */
void ExceptionTest_ExceptionProne__impl( ExceptionTest self )
{
	throw_new_MyException( "MyException throwed for testing purposes" );
}

/*
 * Class:     exceptions_ExceptionTest
 * Method:    RuntimeExceptionProne
 * Signature: ()V
 * Imported methods:
 */
void ExceptionTest_RuntimeExceptionProne__impl( ExceptionTest self )
{
	throw_new_RuntimeException( "RuntimeException throwed for testing purposes" );
}

CLASSPATH=../../../../src/share/classes/
JAVAH=java -cp ../../../../dist/lib/javah.jar com.sun.tools.javah.Main
JAVA_INCLUDE_PATH = /usr/lib/jvm/java-6-openjdk/include/

default: run

exceptions_ExceptionTest.class:
	javac -classpath ${CLASSPATH}:.. ExceptionTest.java

frontier: exceptions_ExceptionTest.class
	${JAVAH} -classpath ${CLASSPATH}:.. -staticjni exceptions.ExceptionTest

libExceptionTest.so: frontier
	gcc -fPIC -g -c -Wall exceptions_ExceptionTest_frontier.c exceptions_ExceptionTest.c -I ${JAVA_INCLUDE_PATH}
	gcc -shared -Wl,-soname,libExceptionTest.so -o libExceptionTest.so exceptions_ExceptionTest.o exceptions_ExceptionTest_frontier.o -lc

run: exceptions_ExceptionTest.class libExceptionTest.so
	java -Djava.library.path=. -cp ${CLASSPATH}:.. exceptions.ExceptionTest

clean:
	rm *.class *.so *.o complexe_ExceptionTest_frontier.[ch] exceptions_ExceptionTest.h

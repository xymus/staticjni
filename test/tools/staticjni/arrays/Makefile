CLASSPATH=../../../../src/share/classes/
JAVAH=java -cp ../../../../dist/lib/javah.jar com.sun.tools.javah.Main
JAVA_INCLUDE_PATH = /usr/lib/jvm/java-6-openjdk/include/

default: run

arrays_ArrayTest.class:
	javac -classpath ${CLASSPATH}:.. ArrayTest.java

frontier: arrays_ArrayTest.class
	${JAVAH} -classpath ${CLASSPATH}:.. -staticjni arrays.ArrayTest

libArrayTest.so: frontier
	#gcc -fPIC -g -c -Wall arrays_ArrayTest_frontier.c arrays_ArrayTest.c -I ${JAVA_INCLUDE_PATH} -E | less
	gcc -fPIC -g -c -Wall arrays_ArrayTest_frontier.c arrays_ArrayTest.c -I ${JAVA_INCLUDE_PATH}
	gcc -shared -Wl,-soname,libArrayTest.so -o libArrayTest.so arrays_ArrayTest.o arrays_ArrayTest_frontier.o -lc

run: arrays_ArrayTest.class libArrayTest.so
	java -Xcheck:jni -Djava.library.path=. -cp ${CLASSPATH}:.. arrays.ArrayTest

clean:
	rm *.class *.so *.o complexe_ArrayTest_frontier.[ch] arrays_ArrayTest.h

CLASSPATH=../../../../src/share/classes/
JAVAH=java -cp ../../../../dist/lib/javah.jar com.sun.tools.javah.Main
JAVA_INCLUDE_PATH = /usr/lib/jvm/java-6-openjdk/include/

default: run

complex_Complex.class:
	javac -classpath ${CLASSPATH}:.. Complex.java

frontier: complex_Complex.class
	${JAVAH} -classpath ${CLASSPATH}:.. -staticjni complex.Complex

libComplex.so: frontier
	gcc -fPIC -g -c -Wall complex_Complex_frontier.c complex_Complex.c -I ${JAVA_INCLUDE_PATH}
	gcc -shared -Wl,-soname,libComplex.so -o libComplex.so complex_Complex.o complex_Complex_frontier.o -lc

run: complex_Complex.class libComplex.so
	java -Djava.library.path=. -cp ${CLASSPATH}:.. complex.Complex

clean:
	rm *.class *.so *.o complexe_Complex_frontier.[ch] complex_Complex.h

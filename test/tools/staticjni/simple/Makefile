CLASSPATH=../../../../src/share/classes/
JAVAH=java -cp ../../../../dist/lib/javah.jar com.sun.tools.javah.Main
JAVA_INCLUDE_PATH = /usr/lib/jvm/java-6-openjdk/include/

default: run

frontier:
	javac -classpath ${CLASSPATH} Simple.java
	${JAVAH} -classpath ${CLASSPATH}:. -staticjni Simple

so: frontier
	gcc -fPIC -g -c -Wall Simple_frontier.c Simple.c -I ${JAVA_INCLUDE_PATH}
	gcc -shared -Wl,-soname,libSimple.so -o libSimple.so Simple.o Simple_frontier.o -lc

run: so
	java -Djava.library.path=. -cp ${CLASSPATH}:. Simple

clean:
	rm *.class *.so *.o Simple_frontier.[ch] Simple.h

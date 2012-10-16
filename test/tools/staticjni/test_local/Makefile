CLASSPATH=../../../../src/share/classes/
JAVAH=java -cp ../../../../dist/lib/javah.jar com.sun.tools.javah.Main
JAVA_INCLUDE_PATH = /usr/lib/jvm/java-6-openjdk/include/

all: run

frontier:
	javac -classpath ${CLASSPATH} TestLocal.java
	${JAVAH} -classpath ${CLASSPATH}:. -staticjni TestLocal

so: frontier
	gcc -fPIC -g -c -Wall TestLocal_frontier.c TestLocal.c -I ${JAVA_INCLUDE_PATH}
	gcc -shared -Wl,-soname,libTestLocal.so -o libTestLocal.so TestLocal.o TestLocal_frontier.o -lc

run: so
	java -Djava.library.path=. -cp ${CLASSPATH}:. TestLocal

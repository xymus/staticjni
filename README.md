# Configure and compile modified javah

To specify the jvm to use, uncomment and change the line "# boot.java.home = ..." to indicate the root of the jvm, for example: "boot.java.home = /usr/lib/jvm/java-7-openjdk-amd64/"

To compile tools and tests, then run tests, call: ./staticjni_test.sh

# Code relevant to staticjni

All StaticJNI code is in src/share/com/sun/tools/javah/

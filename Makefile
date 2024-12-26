all: main

main:
	javac -d bin -classpath lib/ojdbc6.jar -sourcepath src src/Main.java

exeMain: 
	java -classpath bin:lib/ojdbc6.jar Main

clean:
	rm -f bin/*.class


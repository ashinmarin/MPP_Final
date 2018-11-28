all:
	find src -name "*.java" > sources.txt
	javac @sources.txt
clean:
	find . -name "*.class" -exec rm -f {} \; 

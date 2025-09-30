# Makefile for Spellify Magic Card Game

# Variables
CLASSPATH = .:lib/json-20250517.jar
SOURCES = Main.java Card.java
MAIN_CLASS = Main

# Default target
all: compile

# Compile the Java files
compile:
    javac -cp $(CLASSPATH) $(SOURCES)

# Run the application
run:
	java -cp $(CLASSPATH) $(MAIN_CLASS)

# Compile and run in one command
start: compile run

# Clean compiled files
clean:
	rm -f *.class

# Help target
help:
    @echo "Available targets:"
	@echo "  compile - Compile the Java source files"
	@echo "  run     - Run the compiled application"
	@echo "  start   - Compile and run the application"
	@echo "  clean   - Remove compiled .class files"
	@echo "  help    - Show this help message"

# Declare phony targets
.PHONY: all compile run start clean help
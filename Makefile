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

# Create JAR file with embedded resources
jar: compile manifest
	jar cfm guess-the-mtg-card.jar manifest.txt *.class cardlist.json lib/json-20250517.jar

# Create standalone JAR with all dependencies embedded
jar-standalone: compile manifest
	mkdir -p temp
	cd temp && jar xf ../lib/json-20250517.jar
	jar cfm guess-the-mtg-card-standalone.jar manifest.txt *.class cardlist.json -C temp .
	rm -rf temp

# Create manifest file
manifest:
	echo "Main-Class: Main" > manifest.txt
	echo "Class-Path: lib/json-20250517.jar" >> manifest.txt

# Clean compiled files
clean:
	rm -f *.class guess-the-mtg-card.jar guess-the-mtg-card-standalone.jar manifest.txt launch4j-config.xml
	rm -rf temp MTG-Card-Guesser.exe



# Linux package (run this on Linux)
exe-linux: jar-standalone
	jpackage --input . \
	    --name "mtg-card-guesser" \
	    --app-version "1.0" \
	    --main-jar guess-the-mtg-card-standalone.jar \
	    --main-class Main \
	    --type deb \
	    --dest dist-linux \
	    --linux-shortcut \
	    --linux-menu-group "Games" \
	    --install-dir "/opt/mtg-card-guesser" \
	    --linux-deb-maintainer "eldercraft@example.com"

exe-windows: jar-standalone
	jpackage --input . --name "MTG-Card-Guesser" --app-version "1.0" --main-jar guess-the-mtg-card-standalone.jar --main-class Main --type exe --dest dist-windows --win-shortcut --win-shortcut-prompt --win-menu --win-menu-group "Games" --win-per-user-install --description "Magic: The Gathering Card Guessing Game"



# Help target
help:
	@echo "Available targets:"
	@echo "  compile - Compile the Java source files"
	@echo "  run     - Run the compiled application"
	@echo "  start   - Compile and run the application"
	@echo "  jar     - Create JAR file with resources"
	@echo "  jar-standalone - Create standalone JAR with all dependencies"
	@echo "  manifest - Create manifest file"
	@echo "  clean   - Remove compiled .class files and JAR"
	@echo "  help    - Show this help message"

# Declare phony targets
.PHONY: all compile run start clean help jar-standalone exe-launch4j launch4j-config exe-linux
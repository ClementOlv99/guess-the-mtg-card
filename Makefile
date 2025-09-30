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

# Create Windows EXE using Launch4j (works on Linux)
exe-launch4j: jar-standalone launch4j-config
	@echo "Creating Windows EXE using Launch4j..."
	./launch4j/launch4j launch4j-config.xml

# Create Launch4j config
launch4j-config:
	@echo '<?xml version="1.0" encoding="UTF-8"?>' > launch4j-config.xml
	@echo '<launch4jConfig>' >> launch4j-config.xml
	@echo '  <dontWrapJar>false</dontWrapJar>' >> launch4j-config.xml
	@echo '  <headerType>gui</headerType>' >> launch4j-config.xml
	@echo '  <jar>guess-the-mtg-card-standalone.jar</jar>' >> launch4j-config.xml
	@echo '  <outfile>MTG-Card-Guesser.exe</outfile>' >> launch4j-config.xml
	@echo '  <errTitle></errTitle>' >> launch4j-config.xml
	@echo '  <cmdLine></cmdLine>' >> launch4j-config.xml
	@echo '  <chdir>.</chdir>' >> launch4j-config.xml
	@echo '  <priority>normal</priority>' >> launch4j-config.xml
	@echo '  <downloadUrl>http://java.com/download</downloadUrl>' >> launch4j-config.xml
	@echo '  <supportUrl></supportUrl>' >> launch4j-config.xml
	@echo '  <stayAlive>false</stayAlive>' >> launch4j-config.xml
	@echo '  <restartOnCrash>false</restartOnCrash>' >> launch4j-config.xml
	@echo '  <manifest></manifest>' >> launch4j-config.xml
	@echo '  <icon></icon>' >> launch4j-config.xml
	@echo '  <jre>' >> launch4j-config.xml
	@echo '    <path></path>' >> launch4j-config.xml
	@echo '    <bundledJre64Bit>false</bundledJre64Bit>' >> launch4j-config.xml
	@echo '    <bundledJreAsFallback>false</bundledJreAsFallback>' >> launch4j-config.xml
	@echo '    <minVersion>1.8.0</minVersion>' >> launch4j-config.xml
	@echo '    <maxVersion></maxVersion>' >> launch4j-config.xml
	@echo '    <jdkPreference>preferJre</jdkPreference>' >> launch4j-config.xml
	@echo '    <runtimeBits>64/32</runtimeBits>' >> launch4j-config.xml
	@echo '  </jre>' >> launch4j-config.xml
	@echo '</launch4jConfig>' >> launch4j-config.xml

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
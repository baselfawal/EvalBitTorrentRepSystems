.PHONY: all clean doc compile

LIB_JARS=`find -L lib/ -name "*.jar" | tr [:space:] :`

compile:
	mkdir -p classes
	javac -sourcepath src -classpath $(LIB_JARS) -d classes `find -L -name "*.java"`

doc:
	mkdir -p doc
	javadoc -sourcepath src -classpath $(LIB_JARS) -d doc peersim.bittorrent

run:
	java -cp $(LIB_JARS):classes peersim.Simulator config-BitTorrent.cfg

all: compile doc run

clean: 
	rm -fr classes doc
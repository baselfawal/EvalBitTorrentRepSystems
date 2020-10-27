
# Andrew West - Makefile for the P2P Simulator (JAVA)

# Compilers
$JC = "javac"

# Optimization flag
$OPT = ""

# Cleanup macro
$UNMAKE = "del  *.class */*.class"

# Macros for whole libraries
$CORE_FILES = "core_lib/*.java"
$GEN_FILES = "generator_lib/*.java"
$SIM_FILES = "simulator_lib/*.java" 
$TSYS_FILES = "trust_system_lib/*.java"

# ----------------------------------------------

# Give the commands to compile the executables.

#all: TraceGenerator TraceSimulator OutputParser

function TraceGenerator {
    #TraceGenerator.java $(CORE_FILES) $(GEN_FILES)
	write-Host "[Compiling trace generator]"
	Invoke-Expression "$JC $OPT TraceGenerator.java $CORE_FILES $GEN_FILES"
	write-Host "[Trace generator compilation successful!]"
}

function TraceSimulator {
    #TraceSimulator.java $(CORE_FILES) $(SIM_FILES) $(TSYS_FILES)
	write-Host "[Compiling trace simulator]"
	Invoke-Expression "$JC $OPT TraceSimulator.java $CORE_FILES $SIM_FILES $TSYS_FILES"
	write-Host [Trace simulator compilation successful!]
}

function OutputParser {
    #OutputParser.java
	write-Host "[Compiling output parser]"
	Invoke-Expression "$JC $OPT OutputParser.java"
	write-Host "[Output parser compilation successful!]"
}

function delete {
	Invoke-Expression $UNMAKE
}

OutputParser

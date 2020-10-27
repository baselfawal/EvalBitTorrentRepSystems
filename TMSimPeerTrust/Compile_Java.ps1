# Andrew West - Makefile for the P2P Simulator
# Inefficient but terse compilation of the two executables

# Compilers
$CC = "gcc"
$JC = "javac"

# Libraries to include.
$LIBS = "-lm"

# Optimization flag
$OPT = "-O3"

# Cleanup macro
$CLEANUP = "del *.o"
# Macros for whole libraries
$CORE_FILES = "core_lib\*.c core_lib\*.h"
$GEN_FILES = "gen_lib\*.c gen_lib\*.h"
$SIM_FILES = "sim_lib\*.c sim_lib\*.h"
$TSYS_FILES = "tsys_lib\*.c tsys_lib\*.h"
$PARSE_FILES = "parser\*.java"

# Header search path
$FIND_HEADER = "-I core_lib -I tsys_lib -I sim_lib"

# ----------------------------------------------.

# Give the commands to compile the executables.

function all {
 #gen_trace
 #sim_run
 #parse
}

function all.condor {
gen_trace.condor 
#sim_run.condor 
#parse
}


#all.condor: gen_trace.condor sim_run.condor parse

function gen_trace { 
    write-Host $CORE_FILES $GEN_FILES
	write-Host "Compiling trace generator"
	Invoke-Expression "$CC $LIBS $OPT $FIND_HEADER -c core_lib\*.c gen_lib\*.c"
	Invoke-Expression "$CC $LIBS $OPT *.o -o gen_trace"
	Invoke-Expression "$CLEANUP"
	write-Host "Trace generator compilation successful!"
}

function sim_run {
    #$(CORE_FILES) $(SIM_FILES) $(TSYS_FILES)
	write-Host "Compiling trust simulator"
	Invoke-Expression "$CC $LIBS $OPT $FIND_HEADER -c core_lib\*.c sim_lib\*.c tsys_lib\*.c"
	Invoke-Expression "$CC $LIBS $OPT *.o -o sim_run"
	Invoke-Expression "$CLEANUP"
	write-Host "Trust simulator compilation successful!"
}

function parse {
    write-Host $PARSE_FILES
	write-Host "Compiling output parser"
	Invoke-Expression "$JC parser\*.java"
	write-Host "Output parser compiled!"
}

function gen_trace.condor{ 
    write-Host $CORE_FILES $GEN_FILES
	write-Host "Compiling CONDOR trace generator"
	Invoke-Expression "$CC $LIBS $OPT $FIND_HEADER -c core_lib\*.c gen_lib\*.c"
	Invoke-Expression "condor_compile $CC $LIBS $OPT *.o -o gen_trace.condor"
	Invoke-Expression "$CLEANUP"
	write-Host "Trace generator CONDOR compilation successful!"
}

#Write-Host $CC $JC $LIBS $OPT $CORE_FILES
#write-Host $FIND_HEADER
all
#all.condor
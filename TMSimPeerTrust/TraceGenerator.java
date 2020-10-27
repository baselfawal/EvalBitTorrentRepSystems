
import core_lib.*;
import generator_lib.GeneratorOutput;
import generator_lib.GeneratorUtils;

import java.io.*;

/**
 * TraceGenerator is a driver program that generates trace files embodying 
 * network runs that can be used in the evaluation of trust management systems.
 */
public class TraceGenerator{

	// ************************** PRIVATE FIELDS *****************************

	/**
	 * The filename/path of the *.trace file to be written
	 */
	private static String OUTPUT;

	// ************************** PUBLIC METHODS *****************************	
	
	/**
	 * The main driver method.
	 * @param args See the enclosed README document for usage information
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException{
		
			// Parse the parameters into a Global object
		final Globals GLOBALS = parse_arguments(args);
		
			// Open object to print to trace, and write header
		GeneratorOutput Trace = new GeneratorOutput(OUTPUT, GLOBALS);
		Trace.writeHeader();
		
			// Initialize the network object with passed parameters
		Network nw = new Network(GLOBALS);
			
			// Do the User initializations and print them to trace
		GeneratorUtils Generator = new GeneratorUtils(nw, GLOBALS);
		Generator.generateUsers();
		Trace.writeUsers(nw);
		
			// Then, create and output initial libraries
		Generator.generateInitLibs();
		Trace.writeLibraries(nw);
		
		for(int i=0; i < (GLOBALS.NUM_TRANS + GLOBALS.WARMUP); i++){
			if(GLOBALS.SMART_GEN)
				Trace.writeTrans(Generator.genTransactionSmart());
			else
				Trace.writeTrans(Generator.genTransactionNaive());
		} // Generate and print transactions, per mode parameter
		System.out.print("Transaction generation complete...\n");
		System.out.printf("Done! Output written to %s\n\n", OUTPUT);
		
		Trace.shutdown();
		return;
	}
	
	// ************************** PRIVATE METHODS ****************************

	/**
	 * Parse the command-line arguments provided to the main() method.
	 * @param args See the enclosed README document for usage information
	 * @return A Globals object wrapping parameter variables
	 */
	private static Globals parse_arguments(String[] args){
		if(args.length % 2 == 1){
			System.out.print("\nInvalid # of arguments. Aborting.\n\n");
			System.exit(1);
		} // Check the number of arguments
		
			// Set default in case some arguments aren't provided
		int NUM_USERS = 25;
		int NUM_FILES = 5000;
		int NUM_TRANS = 10000;
		double ZIPF = 0.4;
		int PRE_TRUSTED = 5;
		int USR_PURE = 0;
		int USR_FEED = 0;
		int USR_PROV = 0;
		int USR_DISG = 0;
		int USR_SYBL = 0;
		int BAND_MAX = 2;
		int BAND_PER = 1;
		int WARMUP = 0;
		boolean SMART_GEN = true;
		OUTPUT = "trace_0.trace";

		for(int i=1; i < args.length; i+=2){
			if(args[i-1].equalsIgnoreCase("-users"))
				NUM_USERS = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-files"))
				NUM_FILES = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-trans"))
				NUM_TRANS = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-zipf"))
				ZIPF = Double.parseDouble(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:pre_trusted"))
				PRE_TRUSTED = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:purely"))
				USR_PURE = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:feedback"))
				USR_FEED = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:provider"))
				USR_PROV = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:disguise"))
				USR_DISG = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-usr:sybil"))
				USR_SYBL = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-band:max_conn"))
				BAND_MAX = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-band:period"))
				BAND_PER = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-mode:warmup"))
				WARMUP = Integer.parseInt(args[i]);
			else if(args[i-1].equalsIgnoreCase("-mode:smartgen"))
				SMART_GEN = Boolean.parseBoolean(args[i]);
			else if(args[i-1].equalsIgnoreCase("-output"))
				OUTPUT = args[i];
			else{
				System.out.print("\nInvalid argument(s). Aborting.\n\n");
				System.exit(1);
			} // Catch any unsupported arguments		
		} // Parse all arguments
		
		int USR_GOOD = (NUM_USERS-USR_PURE-USR_FEED-USR_PROV-USR_DISG-USR_SYBL);
		if(USR_GOOD < 0){
			System.out.print("\nError: Number of malicious users > total " +
					"users. Aborting.\n\n");
			System.exit(1);
		} // Make sure user counts are legal
		
		if(PRE_TRUSTED > USR_GOOD){
			System.out.print("\nError: Number of pre-trusted users > good " +
					"users .Aborting.\n\n");
			System.exit(1);
		} // Make sure pre-trusted count is legal 
		
		if(!OUTPUT.endsWith(".trace")){
			System.out.print("\nError: Output file doesn't end in *.trace " +
					"extension. Aborting.\n\n");
			System.exit(1);
		} // Make sure output file extension is well formed
		
		return(new Globals(NUM_USERS, NUM_FILES, NUM_TRANS, ZIPF, 
				PRE_TRUSTED, USR_GOOD, USR_PURE, USR_FEED, USR_PROV, USR_DISG, 
				USR_SYBL, BAND_MAX, BAND_PER, WARMUP, SMART_GEN));
	}
}

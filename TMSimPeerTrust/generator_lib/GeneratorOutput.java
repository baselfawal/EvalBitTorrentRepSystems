
package generator_lib;

import java.io.*;
import java.util.Iterator;
import core_lib.*;

/**
 * The GeneratorOutput class assists the TraceGenerator driver program in 
 * writing data to the trace file
 */
public class GeneratorOutput{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * The Network parameterization object
	 */
	private final Globals GLOBALS;
	
	/**
	 * Output stream to the trace file
	 */
	private PrintWriter out;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a GeneratorOutput object
	 * @param output Filename/path of the trace file to be written
	 * @param GLOBALS The Network parameterization object
	 */
	public GeneratorOutput(String output, Globals GLOBALS){
		try{
			FileOutputStream fos = new FileOutputStream(output);
			out = new PrintWriter(fos);
		} catch(FileNotFoundException e){
			System.out.println("\nError: Problems opening output trace file." +
					" Aborting.\n\n");
			System.exit(1);
		} // Open the PrintWriter on output file.
		this.GLOBALS = GLOBALS;
	}
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Write the header (mostly GLOBAL variables) data to the trace file.
	 */
	public void writeHeader(){
		out.printf("%d Users\n", GLOBALS.NUM_USERS);
		out.printf("%d Files\n", GLOBALS.NUM_FILES);
		out.printf("%d Transactions\n", GLOBALS.NUM_TRANS);
		out.printf("%d Maximum Connections\n", GLOBALS.BAND_MAX);
		out.printf("%d Cycle Length per Upload-Download\n", GLOBALS.BAND_PER);
		out.printf("%d Warm-up Transactions\n", GLOBALS.WARMUP);
		out.printf("%f Zipf constant\n", GLOBALS.ZIPF);
		out.printf("%d Pre-Trusted Users\n", GLOBALS.PRE_TRUSTED);
		out.printf("%d Well-Behaved (Good) Users\n", GLOBALS.USR_GOOD);
		out.printf("%d Purely Malicious Users\n", GLOBALS.USR_PURE);
		out.printf("%d Feedback Skewing Users\n", GLOBALS.USR_FEED);
		out.printf("%d Malignant Providing Users\n", GLOBALS.USR_PROV);
		out.printf("%d Disguised Malicous Users\n", GLOBALS.USR_DISG);
		out.printf("%d Sybil Attack Users\n", GLOBALS.USR_SYBL);
		out.printf("%b Intelligent Trans. Generation\n", GLOBALS.SMART_GEN);
		out.printf("%d Trace Generation Seed\n\n", GLOBALS.RAND_SEED);
		System.out.print("\nHeader complete...\n");
	}
	
	/**
	 * Write User library data to the trace file
	 * @param nw The Network whose User data to write
	 */
	public void writeUsers(Network nw){
		for(int i=0; i < GLOBALS.NUM_USERS; i++){
			out.printf("(%f,", nw.getUser(i).getCleanup());
			out.printf("%f,", nw.getUser(i).getHonesty());
			out.printf("%d,", User.BehaviorToInt(nw.getUser(i).getModel()));
			out.printf("%b)\n", nw.getUser(i).isPreTrusted());
		} // Print all User initialization data to trace
		out.printf("\n"); // Line separator
		System.out.print("User initialization complete...\n");
	}
	
	/**
	 * Write file library data to the trace file
	 * @param nw The Network whose file data to write
	 */
	public void writeLibraries(Network nw){
		Iterator<FileCopy> iter;
		FileCopy copy;
		for(int i=0; i < GLOBALS.NUM_FILES; i++){
			iter = nw.getFileIterator(i);
			while(iter.hasNext()){
				copy = iter.next();
				out.printf("(%d,%d,%b)\n", copy.getOwner(), i, copy.getValid());
			} // Print trace entry for each file copy
		} // Output entries for all files in the Network
		out.print("\n"); // Blank separator
		System.out.print("Library initialization complete...\n");
	}
	
	/**
	 * Write a single transaction to the trace file
	 * @param trans The Transaction whose data to write
	 */
	public void writeTrans(Transaction trans){
		out.printf("(%d,%d)\n", trans.getRecv(), trans.getFile());
	}
	
	/**
	 * Shutdown (flush and close) the output stream
	 */
	public void shutdown(){
		out.flush();
		out.close();
	}
	
}

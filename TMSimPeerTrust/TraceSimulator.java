
import java.io.*;

import core_lib.*;
import simulator_lib.SimulatorInput;
import simulator_lib.SimulatorOutput;
import simulator_lib.SimulatorUtils;
import simulator_lib.SimulatorMalicious;
import trust_system_lib.*;

/**
 * The TraceSimulator class, when given a trace file and TM algorithm,
 * simulates the trace in that environment and outputs a statistical file.
 */
public class TraceSimulator{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * The TSYS enumeration lists the implemented TM algorithms.
	 */
	private enum TSYS{NONE, EIGEN, ET_INC, TNA_SL, MYTRUST, PEERTRUST, THRESHOLDTRUST};
	
	/**
	 * The path/filename of the input trace file.
	 */
	private static String FILE_NAME;
	
	/**
	 * Instance of a trust algorithm managing a simulation.
	 */
	private static TrustAlg TALG;
	
	/**
	 * Label describing the algorithm managing a simulation.
	 */
	private static TSYS TSYSTEM;
	
	/**
	 * Malicious strategy being applied during this simulation
	 */
	private static SimulatorMalicious.MAL_STRATEGY STRATEGY;

	// ************************** PUBLIC METHODS *****************************

	
	/**
	 * The main driver method.
	 * @param args  See the README document for usage information
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		long start_time = System.currentTimeMillis();
		parse_arguments(args);
		
			// Open the input file, and read off global variables
		SimulatorInput Trace = new SimulatorInput(FILE_NAME);
		Globals GLOBALS = Trace.parseGlobals();
		
			// Create the network and add static trace data to it
		Network nw = new Network(GLOBALS);
		Trace.parseUsers(nw);
		Trace.parseLibraries(nw);
		System.out.print("\nTrace file parsed and static initialization " +
				"complete...\n");
		
			// Create and parameterize object coordinating malicious behavior
		SimulatorMalicious mal = new SimulatorMalicious(nw, STRATEGY);
		
			// Set and construct the TM managing the Network
		if(TSYSTEM == TSYS.EIGEN)
			TALG = new EigenTM(nw);
		else if(TSYSTEM == TSYS.ET_INC)
			TALG = new EtIncTM(nw);
		else if(TSYSTEM == TSYS.TNA_SL)
			TALG = new TnaSlTM(nw);
		else if(TSYSTEM == TSYS.NONE)
			TALG = new NoneTM(nw);
		else if(TSYSTEM == TSYS.MYTRUST)
			TALG = new MyTrustTM(nw);
		else if(TSYSTEM == TSYS.PEERTRUST)
			TALG = new PeerTrustTM(nw);
		else if(TSYSTEM == TSYS.THRESHOLDTRUST)
			TALG = new ThresholdTM(nw);
		
			// Perform the warm-up transactions
		System.out.printf("Beginning warm-up phase... (%d transactions)\n", 
				GLOBALS.WARMUP);
		SimulatorUtils Simulate = new SimulatorUtils();
		for(int i=0; i < GLOBALS.WARMUP; i++){
			Simulate.simTrans(nw, i, Trace.parseNextTransaction(), mal, TALG);
			if((i % 500 == 0) && (i != 0)){
				System.out.printf("Warm-up transactions completed: %d...\n", i);
				System.out.flush();
			} // Periodic status updates during warm-up phase
		} // Parse and dynamically perform transactions
		System.out.print("Warm-up phase complete...\n");
		
			// Reset statistics and perform actual transactions
		nw.STATS.reset();
		System.out.printf("Beginning simulation phase... (%d transactions)\n", 
				GLOBALS.NUM_TRANS);
		for(int i=GLOBALS.WARMUP; i < (GLOBALS.WARMUP+GLOBALS.NUM_TRANS); i++){
			Simulate.simTrans(nw, i, Trace.parseNextTransaction(), mal, TALG);
			if(((i-GLOBALS.WARMUP) % 500 == 0) && (i != 0)){
				System.out.printf("Transactions completed so far: %d...\n", 
						(i-GLOBALS.WARMUP));
				System.out.flush();
			} // Periodic status updates during simulation phase
		} // Parse and dynamically perform transactions
		System.out.printf("Simulation phase complete...\n");
		Simulate.commitRemaining(nw, GLOBALS.WARMUP + GLOBALS.NUM_TRANS, TALG);
		
			// Set extension on output file; open; print header and stats
		FILE_NAME = FILE_NAME.substring(0, FILE_NAME.lastIndexOf('.')+1);
		FILE_NAME = FILE_NAME.concat(TALG.fileExtension());
		SimulatorOutput Output = new SimulatorOutput(FILE_NAME);
		Output.printHeader(GLOBALS, Trace.getGenSeed(), TALG, STRATEGY);
		Output.printStatistics(nw.GLOBALS, nw.STATS);
		
			// Calculate runtime and print final notes to terminal
		long stop_time = System.currentTimeMillis();
		double run_time = ((stop_time - start_time) / 1000.0); 
		System.out.printf("Run complete! Data written to %s\n", FILE_NAME);
		System.out.printf("Simulation runtime: %f secs\n\n", run_time);
		
			// Cleanup and exit
		Trace.shutdown();
		Output.shutdown();
		return;
	}
	
	// ************************** PRIVATE METHODS ****************************	

	/**
	 * Parse the command-line arguments provided to the main() method.
	 * @param args See the enclosed README document for usage information
	 */
	private static void parse_arguments(String[] args){
		if(args.length != 6){
			System.out.print("\nInvalid # of arguments. Aborting.\n\n");
			System.exit(1);
		} // Check the number of arguments
		
		for(int i=1; i < args.length; i+=2){
			if(args[i-1].equalsIgnoreCase("-input"))
				FILE_NAME = args[i];
			else if(args[i-1].equalsIgnoreCase("-tm")){
				if(args[i].equalsIgnoreCase("eigen"))
					TSYSTEM = TSYS.EIGEN;
				else if(args[i].equalsIgnoreCase("eigentrust"))
					TSYSTEM = TSYS.EIGEN;
				else if(args[i].equalsIgnoreCase("et_inc"))
					TSYSTEM = TSYS.ET_INC;
				else if(args[i].equalsIgnoreCase("etinc"))
					TSYSTEM = TSYS.ET_INC;
				else if(args[i].equalsIgnoreCase("tna_sl"))
					TSYSTEM = TSYS.TNA_SL;
				else if(args[i].equalsIgnoreCase("tnasl"))
					TSYSTEM = TSYS.TNA_SL;
				else if(args[i].equalsIgnoreCase("mytrust"))
					TSYSTEM = TSYS.MYTRUST;
				else if(args[i].equalsIgnoreCase("peertrust"))
					TSYSTEM = TSYS.PEERTRUST;
				else if(args[i].equalsIgnoreCase("thresholdt"))
					TSYSTEM = TSYS.THRESHOLDTRUST;
				else
					TSYSTEM = TSYS.NONE;
			} else if(args[i-1].equalsIgnoreCase("-strategy")){
				if(args[i].equalsIgnoreCase("isolated"))
					STRATEGY = SimulatorMalicious.MAL_STRATEGY.ISOLATED;
				else if(args[i].equalsIgnoreCase("collective"))
					STRATEGY = SimulatorMalicious.MAL_STRATEGY.COLLECTIVE;
				else // if(args[i].equalsIgnoreCase("naive"))
					STRATEGY = SimulatorMalicious.MAL_STRATEGY.NAIVE;
			} else{ 
				System.out.print("\nRequired argument missing. Aborting.\n\n");
				System.exit(1);
			} // Neither '-input' or '-trust_sys' should be omitted
		} // Parse arguments. Check for required flags.
	}
	
}

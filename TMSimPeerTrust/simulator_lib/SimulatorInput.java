
package simulator_lib;

import java.io.*;
import core_lib.*;

/**
 * The SimulatorInput class assists the TraceSimulator driver program in
 * reading and parsing data from the provided trace file.
 */
public class SimulatorInput{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * The Network parameterization object.
	 */
	private Globals GLOBALS;
	
	/**
	 * The seed used to generate the trace being read.
	 */
	private long generator_seed;
	
	/**
	 * Input stream/reader from the trace file.
	 */
	private BufferedReader in;
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct a SimulatorInput object
	 * @param input Filename/path to the trace file to open
	 */
	public SimulatorInput(String input){
		try {
			FileInputStream fis = new FileInputStream(new File(input));
			InputStreamReader isr = new InputStreamReader(fis);
			in = new BufferedReader(isr);
		} catch(FileNotFoundException e){
			System.out.println("\nError: Problems opening output trace file." +
					" Aborting.\n\n");
			System.exit(1);
		} // Open the BufferedReader on input file.
	}
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Parse the trace file into a Network parameterization object.
	 * @return A Globals object with parameterization data from the trace
	 * @throws IOException
	 */
	public Globals parseGlobals() throws IOException{
			
			// We have to initialize all global variables here, because
			// compiler doesn't realize they are all done in 'switch' below
		int NUM_USERS = 0, NUM_FILES = 0, NUM_TRANS = 0, BAND_MAX = 0;
		int BAND_PER = 0, WARMUP = 0, PRE_TRUSTED = 0, USR_GOOD = 0; 
		int USR_PURE = 0, USR_FEED = 0, USR_PROV = 0, USR_DISG = 0;
		int USR_SYBL = 0;
		double ZIPF = 0.0;
		boolean SMART_GEN = true;
		
		String line;
		for(int i=0; i <= 15; i++){
			line = in.readLine();
			line = line.substring(0, line.indexOf(' '));
			switch(i){
				case 0:  NUM_USERS = Integer.parseInt(line); break;
				case 1:  NUM_FILES = Integer.parseInt(line); break;
				case 2:  NUM_TRANS = Integer.parseInt(line); break;
				case 3:  BAND_MAX = Integer.parseInt(line); break;
				case 4:  BAND_PER = Integer.parseInt(line); break;
				case 5:  WARMUP = Integer.parseInt(line); break;
				case 6:  ZIPF = Double.parseDouble(line); break;
				case 7:	 PRE_TRUSTED = Integer.parseInt(line); break;
				case 8:  USR_GOOD = Integer.parseInt(line); break;
				case 9: USR_PURE = Integer.parseInt(line); break;
				case 10: USR_FEED = Integer.parseInt(line); break;
				case 11: USR_PROV = Integer.parseInt(line); break;
				case 12: USR_DISG = Integer.parseInt(line); break;
				case 13: USR_SYBL = Integer.parseInt(line); break;
				case 14: SMART_GEN = Boolean.parseBoolean(line); break;
				case 15: this.generator_seed = Long.parseLong(line); break;
				default: break;
			} // Parse variables based solely on order in trace		
		} // This order was determined at time of Trace printing
		in.readLine(); // Read off blank separator
		
		this.GLOBALS = new Globals(NUM_USERS, NUM_FILES, NUM_TRANS, ZIPF, 
				PRE_TRUSTED, USR_GOOD, USR_PURE, USR_FEED, USR_PROV, USR_DISG, 
				USR_SYBL, BAND_MAX, BAND_PER, WARMUP, SMART_GEN);
		return this.GLOBALS;
	}
	
	/**
	 * Return the random seed used to to generate the trace file.
	 * @return the random seed used to to generate the trace file
	 */
	public long getGenSeed(){
		return this.generator_seed;
	}
	
	/**
	 * Parse user data from trace, use it to populate user portion of Network.
	 * @param nw The Network whose user data should be populated
	 * @throws IOException
	 */
	public void parseUsers(Network nw) throws IOException{
		String line, data_str;
		boolean pre_trusted;
		double cleanup_pct, honest_pct;
		User.Behavior model;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			line = in.readLine();
			data_str = line.substring(1, line.indexOf(','));
			cleanup_pct = Double.parseDouble(data_str);
			
			line = line.substring(line.indexOf(',')+1, line.length());
			data_str = line.substring(0, line.indexOf(','));
			honest_pct = Double.parseDouble(data_str);
			
			line = line.substring(line.indexOf(',')+1, line.length());
			data_str = line.substring(0, line.indexOf(','));
			model = User.IntToBehavior(Integer.parseInt(data_str));
			
			line = line.substring(line.indexOf(',')+1, line.length()-1);
			pre_trusted = Boolean.parseBoolean(line);
					
			nw.setUser(i, new User(model, cleanup_pct, honest_pct, 
					pre_trusted, nw.GLOBALS));
		} // We expect 'NUM_USERS' lines of data
		in.readLine(); // Read off blank separator
	}

	/**
	 * Parse file data from trace, use it to populate file portion of Network.
	 * @param nw The Network whose file data should be populated
	 * @throws IOException
	 */
	public void parseLibraries(Network nw) throws IOException{
		int user, file;
		boolean valid;
		String data_str, line = in.readLine();
		while(line.length() > 1){
			data_str = line.substring(1, line.indexOf(','));
			user = Integer.parseInt(data_str);
			line = line.substring(line.indexOf(',')+1, line.length());
			data_str = line.substring(0, line.indexOf(','));
			file = Integer.parseInt(data_str);
			data_str = line.substring(line.indexOf(',')+1, line.length()-1);
			valid = Boolean.parseBoolean(data_str);
			nw.addFile(user, file, valid);
			line = in.readLine();
		} // Parse lines pertaining to file libraries, add files accordingly
	}
	
	/**
	 * Parse next transaction from the trace file.
	 * @return A Transaction object wrapping read data
	 * @throws IOException
	 */
	public Transaction parseNextTransaction() throws IOException{
		int recv, file;
		String line = in.readLine();
		recv = Integer.parseInt(line.substring(1, line.indexOf(',')));
		line = line.substring(line.indexOf(',')+1, line.length()-1);
		file = Integer.parseInt(line);
		return (new Transaction(-1, -1, recv, file, true));
	}
	
	/**
	 * Shutdown (close) the input stream.
	 * @throws IOException
	 */
	public void shutdown() throws IOException{
		in.close();
	}
	
}

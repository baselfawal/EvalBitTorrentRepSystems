
package generator_lib;

import core_lib.*;

/**
 * The GeneratorUtils class assists the TraceGenerator driver program with 
 * the generation of users, file libraries, and transactions.
 */
public class GeneratorUtils{
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * The Network for which the generations are taking place.
	 */
	private Network nw;
	
	/**
	 * The Network parameterization object.
	 */
	private final Globals GLOBALS;
	
	/**
	 * Sum of all file Zipf frequencies.
	 */
	private double ZIPF_SUM;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a GeneratorUtils object.
	 * @param network The Network on which the utilities will operate
	 * @param GLOBALS The Network parameterization object
	 */
	public GeneratorUtils(Network network, Globals GLOBALS){
		this.nw = network;
		this.GLOBALS = GLOBALS;
		this.ZIPF_SUM = 0.0;
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Generate/populate the User library.
	 */
	public void generateUsers(){
		int total = 0;
		total += genUserType(User.Behavior.USR_PURE, GLOBALS.USR_PURE, total);
		total += genUserType(User.Behavior.USR_FEED, GLOBALS.USR_FEED, total);
		total += genUserType(User.Behavior.USR_PROV, GLOBALS.USR_PROV, total);
		total += genUserType(User.Behavior.USR_DISG, GLOBALS.USR_DISG, total);
		total += genUserType(User.Behavior.USR_SYBL, GLOBALS.USR_SYBL, total);
		genUserType(User.Behavior.USR_GOOD, GLOBALS.USR_GOOD, total);
	}
	
	/**
	 * Generate/populate the initial file library.
	 */
	public void generateInitLibs(){
		boolean valid;
		double usr_cleanup;
		for(int i=0; i < GLOBALS.NUM_USERS; i++){
			for(int j=0; j < GLOBALS.NUM_FILES; j++){
				if(GLOBALS.RAND.nextDouble() <= getZipf(j)){
					usr_cleanup = nw.getUser(i).getCleanup();					
					valid = (GLOBALS.RAND.nextDouble() <= usr_cleanup);
					nw.addFile(i, j, valid);
				} // Add file to library based on parameter thresholds				
			} // Each user can (probabilistically) own any file
		} // Initialize libraries for all users
		for(int i=0; i < GLOBALS.NUM_FILES; i++){ 
			this.ZIPF_SUM += getZipf(i);
		} // Calculate total ZIPF weight for all files
	}
	
	/**
	 * 'Intelligently' generate a transaction.
	 * @return A Transaction object containing the data generated
	 */
	public Transaction genTransactionSmart(){
		int recv, file_num;
		do{ // A receiver shouldn't already have a "full" library
			recv = GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
		} while(nw.getUser(recv).getNumFiles() == nw.availableFiles());
		
		boolean cond1, cond2;
		do{ // Receiver must not already have file, and it must be available
			file_num = inverseZipf(GLOBALS.RAND.nextDouble() * this.ZIPF_SUM);
			cond1 = nw.hasFile(recv, file_num);
			cond2 = nw.fileOwners(file_num) == 0;
		} while(cond1 || cond2);
		nw.addFile(recv, file_num, true);
		return (new Transaction(-1, -1, recv, file_num, false));
	}
	
	/**
	 * 'Naively' generate a transaction.
	 * @return A Transaction object containing the data generated
	 */
	public Transaction genTransactionNaive(){
		int recv = GLOBALS.RAND.nextInt(GLOBALS.NUM_USERS);
		int file_num = GLOBALS.RAND.nextInt(GLOBALS.NUM_FILES);
		return (new Transaction(-1, -1, recv, file_num, false));
	}
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Generate all users of a specified type, and place in library
	 * @param model The Behavior model of the generated users
	 * @param quantity The number of users of this type to generate
	 * @param prev Identifier where first such user will be placed in User lib.
	 * @return The number of users generated
	 */
	private int genUserType(User.Behavior model, int quantity, int prev){
		for(int i=0; i < quantity; i++){
			if((model == User.Behavior.USR_GOOD) && (i < GLOBALS.PRE_TRUSTED))
				nw.setUser(prev+i, new User(model, true, GLOBALS));
			else
				nw.setUser(prev+i, new User(model, false, GLOBALS));
		} // Initialize the specified number of Users, per model
		return quantity;
	}
	
	// *************************** ZIPF PRIVATES *****************************
	
	/**
	 * Return the Zipf frequency of a given file. 
	 * @param rank File number whose frequency is desired
	 * @return Fraction describing frequency rate on [0..1]
	 */
	private double getZipf(int rank){
		return ((1.0) / (Math.pow((rank + 2.0), GLOBALS.ZIPF)));
	}	
	
	/**
	 * Given an number on [0..ZIPF_SUM], map that value to a file identifier.
	 * This is performed such that if a number is randomly selected on that
	 * interval, the probability of a file identifier being returned corresponds
	 * to its Zipf frequency as calculated by the getZipf() method. I am 
	 * dissatisfied with the efficiency of this calculation.
	 * @param weight Number on the [0..ZIPF_SUM] interval
	 * @return Corresponding file identifier, per probability weighting
	 */
	private int inverseZipf(double weight){
		double total = 0.0;
		for(int i=0; i < GLOBALS.NUM_FILES; i++){
			total += getZipf(i);
			if(total > weight)
				return i;
		} // Iteratively sum frequencies
		return (GLOBALS.NUM_FILES - 1);
	}
	
}

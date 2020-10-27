
package trust_system_lib;

import core_lib.*;

/**
 * The NoneTM class conforms to the TrustAlg interface and simulates the lack
 * of a TM system. Essentially, random source selection is used.
 */
public class NoneTM implements TrustAlg{
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct a NoneTm object.
	 * @param nw Network which this EtIncTM will be managing
	 */
	public NoneTM(Network nw){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
				nw.getUserRelation(j, i).setTrust(0.0);
			} // Initialize all trust to an identical value 
		} // Do so for every relation in the network
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Interfaced: Text name of this trust algorithm (spaces are okay).
	 */
	public String algName(){
		return "None";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "none";
	}
	
	/**
	 * Interfaced: Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		// Do nothing, let values persist
	}

	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle){
		// Do nothing, let values persist
	}
	
}

package trust_system_lib;

import core_lib.*;

/**
 * The EtIncTM class conforms to the TrustAlg interface and is nearly identical 
 * to the implementation of EigenTM. The difference is this version uses
 * snapshot comparisons to avoid costly recalculation on every cycle. 
 */
public class EtIncTM extends EigenTM implements TrustAlg{

	
	// ************************** PRIVATE FIELDS *****************************

	/**
	 * Number of cycles to skip between each trust re-calculation.
	 */
	private int cur_skip = 1;
	
	/**
	 * Matrix storing trust values after the *previous* trust calculation.
	 */
	private double[] current, previous;
	
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct an EtIncTM object.
	 * @param nw Network which this EtIncTM will be managing
	 */
	public EtIncTM(Network nw){
		super(nw);
	}
	
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Interfaced: Text name of this trust algorithm (spaces are okay).
	 */
	public String algName(){
		return "EigenTrust-Incremental";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "etinc";
	}
	
	/**
	 * Interfaced: Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		super.update(trans);
	}
	
	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle){
		if(cycle == 0)
			previous = super.trustMultiply(user, 8);
		else if(cycle % cur_skip == 0){
			current = super.trustMultiply(user, 8);
			boolean converged = super.hasConverged(current, previous);
			if((converged) && (cur_skip != 64))
				cur_skip *= 2;
			else if((!converged) && cur_skip != 1)
				cur_skip /= 2;
			
			for(int i=0; i < super.nw.GLOBALS.NUM_USERS; i++){
				for(int j=0; j < super.nw.GLOBALS.NUM_USERS; j++)
					nw.getUserRelation(j, i).setTrust(current[i]);
			} // Set trust globally, not just one vector as in 'super'
			
			previous = current;
		} // Only recalculate every "cur_skip" cycles
	}
	
}

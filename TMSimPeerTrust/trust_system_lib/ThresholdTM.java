package trust_system_lib;

import core_lib.Network;
import core_lib.Relation;
import core_lib.Transaction;
import core_lib.User;

public class ThresholdTM implements TrustAlg {

	// ************************** PRIVATE FIELDS *****************************
	/**
	 * The Network which this TrustAlgorithm is managing.
	 */
	protected Network nw;
	
	/**
	 * Pre-trusted peer distribution and weighting vector
	 */
	private double[] pretrust;

	/**
	 * Leniency factor used to calculate Thersodl T1, 
	 */
	private final int FL = 3;
	
	
	/**
	 * Matrix storing persistent normalized values.
	 */
	private double[][] normalized;

	/**
	 * Construct a PeerTrustTM object.
	 * @param nw Network which this PeerTrust will be managing
	 */
	public ThresholdTM(Network nw) {
		this.nw = nw;
		//Define dimension of pretrust vector
		//and the normailzed matrix with the values from nw object
		pretrust = new double[nw.GLOBALS.NUM_USERS];
		normalized = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
		
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
		return "ThresholdTrust";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "Thresholdt";
	}
	
	/**
	 * Interfaced: Update the feedback after the transaction
	 * Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		//Get the peer ID of the file receiver for this transaction
		//then process the feedback and normailze feedback figure between 0-1
		int userid = trans.getRecv();
		
		//Calling normalizeVector will normalize one vector of the two dimension matrix 
		//the vector ID here is the user ID
		normalizeVector (userid);
	}
	
	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle) {
	    Double Tu=0.0; //calcuated trust
	    Relation rel;
	    //System.out.println();
	    //System.out.println("User:" + user);
	    
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				rel = nw.getUserRelation(user, i);	
				
			// Calculate PeerTrust T(u) as per the formual below
			// T(u) = ALPHA * Sum of S(u,i) * Cr(p(u,i) * TF(u,i) + BETA * CF(u)
			//System.out.println("calculate Tu");
				
			//Tu += normalized[i][user] * calcCred(user, i) + ( ContextFactor(rel));
			Tu += normalized[i][user];
			//Tu += 0;
			
			//System.out.print(Tu + " ");		
			nw.getUserRelation(user, i).setTrust(Tu);
		} // Import trust values back into Object form, duplicating vector
		
	  }
	
	// ************************** PRIVATE METHODS ****************************
	
	
	/**
	 * Normalize a single vector of the persistent matrix.
	 * @param user_vector The vector to be normalized
	 */
	private void normalizeVector(int user_vector){
		int fback_int, normalizer = 0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			fback_int = calcGlobalFBackInt(nw.getUserRelation(user_vector, i));
			normalizer += fback_int;
			normalized[i][user_vector] = fback_int;
		} // Calculate normalizing sum in first pass
		
		if(normalizer == 0){
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[i][user_vector] = pretrust[i];
		} else{ // If a user trusts no one, default to the pre_trust vector
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[i][user_vector] /= (normalizer*1.0);
		} // Else, do the normalizing division in a second pass
	}
	
	/**
	 * Calculate a 'feedback integer' using global feedback data.
	 * @param rel Relation whose 'feedback integer' needs calculated
	 * @return The calculated 'feedback integer'
	 */
	private int calcGlobalFBackInt(Relation rel){
		//System.out.println("Pos:" + rel.getPos() + " Neg: " + rel.getNeg());
		// Rp = Min (10, 10 * Du/Dd)
		int fback_int = rel.getPos() - rel.getNeg();
		//int fback_int = rel.getPos()/rel.getPos();
		if(fback_int < 0)
			fback_int = 0;
		return fback_int;
	}
	

}

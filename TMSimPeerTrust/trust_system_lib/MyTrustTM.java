package trust_system_lib;

import core_lib.Network;
import core_lib.Relation;
import core_lib.Transaction;
import core_lib.User;

public class MyTrustTM implements TrustAlg {

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
	 * Weighting constant for collective evaluation
	 */
	private final double ALPHA = 1.0;
	
	/**
	 * Weighting constant for community factor.
	 */
	private final double BETA = 0.0;
	
	/**
	 * Matrix storing persistent normalized (pre-multiplication) values.
	 */
	private double[][] normalized;

	/**
	 * Construct a MyTrustTM object.
	 * @param nw Network which this MycustmTrust will be managing
	 */
	public MyTrustTM(Network nw) {
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
		return "MyTrust";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "mytrust";
	}
	
	/**
	 * Interfaced: Update the feedback after the transaction
	 * Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		//Get the peer ID of the file receiver
		//and the process the feedback and normailze it to be between 0-1
		int user = trans.getRecv();
		//Calling normalizeVector will normalize one vector of the two dimension matrix 
		//the vector ID here is the user ID
		normalizeVector (user);
		
	}
	
	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle) {
	
	    Double Tu;
	    Relation rel ;
	    System.out.println();
	    System.out.println("User:" + user);
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
				rel = nw.getUserRelation(user, i);					
			// Calculate the PeerTrust 
			//T(u) = ALPHA * SIGMA S(u,i) * Cr(p(u,i) * TF(u,i) + BETA * CF(u)
			Tu = ALPHA * normalized[i][user] * calcCred(user, i) + (BETA * ContextFactor(rel));
			//System.out.print(Tu + " ");
			//System.out.println();
			//System.out.println(calcCred(user,i) + " ");
			nw.getUserRelation(user, i).setTrust(Tu);
		} // Import trust values back into Object form, duplicating vector

	  }
	
	// ************************** PRIVATE METHODS ****************************
	
	
	/**
	 * Normalize a single vector of the persistent matrix.
	 * @param uvector The vector to be normalized
	 */
	private void normalizeVector(int uvector){
		int fback_int, normalizer = 0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			fback_int = calcGlobalFBackInt(nw.getUserRelation(uvector, i));
			normalizer += fback_int;
			normalized[i][uvector] = fback_int;
		} // Calculate normalizing sum in first pass
		
		if(normalizer == 0){
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[i][uvector] = pretrust[i];
		} else{ // If a user trusts no one, default to the pre_trust vector
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[i][uvector] /= (normalizer*1.0);
		} // Else, do the normalizing division in a second pass
	}
	
	/**
	 * Calculate a 'feedback integer' using global feedback data.
	 * @param rel Relation whose 'feedback integer' needs calculated
	 * @return The calculated 'feedback integer'
	 */
	private int calcGlobalFBackInt(Relation rel){
		//System.out.println("Pos:" + rel.getPos() + " Neg: " + rel.getNeg());
		int fback_int = rel.getPos() - rel.getNeg();
		if(fback_int < 0)
			fback_int = 0;
		return fback_int;
	}
	/**
	 * Calculate the peer credibility
	 * @param rel the relation
	 * @return credibity of the peer 
	 */
	
	private double calcCred(int user, int i) {
		
		if(nw.getUser(i).getModel() != User.Behavior.USR_GOOD) 
			return 0.0;
		else return 0.5;
		
	}
	
	/**
	 * calculate the transaction context factor
	 * @param rel
	 * @return the transaction context factor
	 */
	private int ContextFactor (Relation rel) {
		
		return 1;
	}
	
}

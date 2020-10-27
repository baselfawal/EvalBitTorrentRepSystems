
package trust_system_lib;

import core_lib.*;

/**
 * The EigenTM class conforms to the TrustAlg interface and implements the
 * EigenTrust algorithm as described by Hector Garcia-molina, et. al.
 */
public class EigenTM implements TrustAlg{
	
	// ************************* PROTECTED FIELDS ****************************
	
	/**
	 * The Network which this EigenTM is managing.
	 */
	protected Network nw;
	
	
	// ************************** PRIVATE FIELDS *****************************
	
	/**
	 * Weighting constant making pre-trusted peers more powerful
	 */
	private final double ALPHA = 0.5;
	
	/**
	 * Acceptable error margin in convergence tests.
	 */
	private final double EPSILON = 0.001;
	
	/**
	 * Pre-trusted peer distribution and weighting vector
	 */
	private double[] pretrust;
	
	/**
	 * Scratch space vector for multiplication purposes.
	 */
	private double[] vectorA;
	
	/**
	 * Scratch space vector for multiplication purposes.
	 */
	private double[] vectorB;
	
	/**
	 * Matrix storing persistent normalized (pre-multiplication) values.
	 */
	private double[][] normalized;
	
	
	// *************************** CONSTRUCTORS ******************************

	/**
	 * Construct an EigenTM object.
	 * @param nw Network which this EigenTM will be managing
	 */
	public EigenTM(Network nw){
		this.nw = nw;
		pretrust = new double[nw.GLOBALS.NUM_USERS];
		vectorA = new double[nw.GLOBALS.NUM_USERS];
		vectorB = new double[nw.GLOBALS.NUM_USERS];
		normalized = new double[nw.GLOBALS.NUM_USERS][nw.GLOBALS.NUM_USERS];
				
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(nw.GLOBALS.PRE_TRUSTED > 0 && nw.getUser(i).isPreTrusted())
				pretrust[i] = (1.0 / nw.GLOBALS.PRE_TRUSTED);
			else if(nw.GLOBALS.PRE_TRUSTED > 0) // (and not pre-trusted)
				pretrust[i] = (0.0);
			else // (there are no pre-trusted users)
				pretrust[i] = (1.0 / nw.GLOBALS.NUM_USERS);
			
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++)
				normalized[i][j] = pretrust[i];
		} // Initialize pre-trusted vector, and persistent normalized values
	}
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Interfaced: Text name of this trust algorithm (spaces are okay).
	 */
	public String algName(){
		return "EigenTrust";
	}
	
	/**
	 * Interfaced: File extension placed on output files using this algorithm.
	 */
	public String fileExtension(){
		return "eigen";
	}
	
	/**
	 * Interfaced: Given coordinates of a feedback commitment, update as needed.
	 */
	public void update(Transaction trans){
		normalizeVector(trans.getRecv());
	}
	
	/**
	 * Interfaced: Compute trust, exporting trust values to Network.
	 */
	public void computeTrust(int user, int cycle){
		trustMultiply(user, 8);
	}
	
	
	// ************************* PROTECTED METHODS ***************************
	
	/**
	 * Perform matrix multiply as a means of aggregating global trust data.
	 * @param user Identifier of user performing trust computation
	 * @param max_iters Maximum number of multiplications to perform
	 * @return The converged global trust vector
	 */
	protected double[] trustMultiply(int user, int max_iters){
		vectorA = singleMultiply(pretrust);
		max_iters--;
		do{ // Multiply until convergence or maximum iterations reached
			vectorB = singleMultiply(vectorA);
			vectorA = singleMultiply(vectorB);
			max_iters -= 2;
		} while((max_iters > 0) && !hasConverged(vectorA, vectorB));
		
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			nw.getUserRelation(user, i).setTrust(vectorA[i]);
		} // Import trust values back into Object form, duplicating vector
		return vectorA.clone();
	}
	
	/**
	 * Test if the difference between two vectors is below some threshold.
	 * @param vec1 The first vector for comparison
	 * @param vec2 The second vector for comparison
	 * @return TRUE if variance < EPSILON at every position. FALSE otherwise.
	 */
	protected boolean hasConverged(double[] vec1, double[] vec2){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(Math.abs(vec1[i]-vec2[i]) > this.EPSILON)
				return false;
		} // Compare vector elements, examining delta change
		return true;
	}
	
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Normalize a single vector of the persistent matrix.
	 * @param new_vec The vector to be normalized
	 */
	private void normalizeVector(int new_vec){
		int fback_int, normalizer = 0;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			fback_int = calcGlobalFBackInt(nw.getUserRelation(new_vec, i));
			normalizer += fback_int;
			normalized[i][new_vec] = fback_int;
		} // Calculate normalizing sum in first pass
		
		if(normalizer == 0){
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[i][new_vec] = pretrust[i];
		} else{ // If a user trusts no one, default to the pre_trust vector
			for(int i=0; i < nw.GLOBALS.NUM_USERS; i++)
				normalized[i][new_vec] /= (normalizer*1.0);
		} // Else, do the normalizing division in a second pass
	}
	
	/**
	 * Calculate a 'feedback integer' using global feedback data.
	 * @param rel Relation whose 'feedback integer' needs calculated
	 * @return The calculated 'feedback integer'
	 */
	private int calcGlobalFBackInt(Relation rel){
		int fback_int = rel.getPos() - rel.getNeg();
		if(fback_int < 0)
			fback_int = 0;
		return fback_int;
	}
			
	/**
	 * Perform a single multiplication iteration per EigenTrust specification.
	 * @param prev_vector Result of the last multiplication iteration
	 * @return A vector closer to converged global trust than that passed in
	 */
	private double[] singleMultiply(double[] prev_vector){
		double[] lhs = vectorMatrixMult(prev_vector, normalized);
		lhs = constantVectorMult((1-ALPHA), lhs);
		double[] rhs = constantVectorMult(ALPHA, pretrust);
		return (vectorAdd(lhs,rhs));	
	}
		
		// Linear algebra methods; nothing really unique going on here

	/**
	 * Linear Algebra: Vector-matrix multiplication.
	 * @param vector Vector to be multiplied
	 * @param matrix Matrix to be multiplied
	 * @return The product vector*matrix, per standard matrix multiply
	 */
	private double[] vectorMatrixMult(double[] vector, double[][] matrix){
		double[] dest = new double[nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[i] = 0.0;
			for(int j=0; j < nw.GLOBALS.NUM_USERS; j++){
				dest[i] += (matrix[i][j] * vector[j]);
			} // Inner loop of matrix-vector multiplication
		} // Outer loop of matrix-vector multiplication
		return dest;
	}
	
	/**
	 * Linear Algebra: Constant-vector multiplication.
	 * @param constant Constant to be multiplied
	 * @param vector Vector to be multiplied
	 * @return The product constant*vector, per standard scalar multiply
	 */
	private double[] constantVectorMult(double constant, double[] vector){
		double[] dest = new double[nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[i] = vector[i] * constant;
		} // Just multiply every vector element by the constant
		return dest;
	}
	
	/**
	 * Linear Algebra: Vector-vector addition.
	 * @param vector1 First vector to be added
	 * @param vector2 Second vector to be added
	 * @return The sum vector1+vector2, per standard vector addition
	 */
	private double[] vectorAdd(double[] vector1, double[] vector2){
		double[] dest = new double[nw.GLOBALS.NUM_USERS];
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			dest[i] = vector1[i] + vector2[i];
		} // Just add the elements at corresponding positions
		return dest;
	}
	
}

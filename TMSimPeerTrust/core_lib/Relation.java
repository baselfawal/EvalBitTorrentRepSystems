
package core_lib;

/**
 * The Relation class describes the prior interaction between two users. It
 * stores both feedbacks and the critical trust value that is calculated
 * by a trust management system (via TrustAlg interface).
 */
public class Relation{
	
	// ************************** PUBLIC FIELDS ******************************
	
	/**
	 * The Copy enumeration differentiates between the different feedback
	 * histories which are being stored at the Relation level.
	 */
	public enum Copy{GLOBAL, HONEST}; 
	
	// ************************** PRIVATE FIELDS *****************************

	/**
	 * Describes feedback history currently activated on this Relation
	 */
	private Copy history;
	
	/**
	 * The number of globally broadcast positive/satisfactory feedbacks.
	 */
	private int global_pos;
	
	/**
	 * The number of globally broadcast negative/unsatisfactory feedbacks.
	 */
	private int global_neg;
	
	/**
	 * The number of truly positive/satisfactory interactions.
	 */
	private int honest_pos;
	
	/**
	 * The number of truly negative/unsatisfactory interactions.
	 */
	private int honest_neg;
	
	/**
	 * The trust value characterizing this user relationship.
	 */
	private double trust_val;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct an empty Relation object. Relations should be modified
	 * only according to the methods below, so no others are provided. 
	 */
	public Relation(){
		this.history = Relation.Copy.GLOBAL;
		this.global_pos = 0;
		this.global_neg = 0;
		this.honest_pos = 0;
		this.honest_neg = 0;
		this.trust_val = 0.0;
		return;
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Access method to the trust value.
	 * @return Trust value characterizing this user Relation
	 */
	public double getTrust(){
		return (this.trust_val);
	}
	
	/**
	 * Set the trust_value field to a new value.
	 * @param new_trust The new value for the trust_value field
	 */
	public void setTrust(double new_trust){
		this.trust_val = new_trust;
	}
	
	/**
	 * Set the history store activated for this Relation object.
	 * @param historyStyle History to be maintained, per Copy enumeration
	 */
	public void setHistory(Relation.Copy historyStyle){
		this.history = historyStyle;
	}
	
	/**
	 * Access method to number of positive feedbacks, per 'history' parameter.
	 * @return Global-positive feedbacks in this Relation
	 */
	public int getPos(){
		if(this.history == Relation.Copy.GLOBAL)
			return (this.global_pos);
		else // if(this.history == Relation.Copy.HONEST)
			return (this.honest_pos);
	}
	
	/**
	 * Access method to number of negative feedbacks, per 'history' parameter.
	 * @return Global-negative feedbacks in this Relation
	 */
	public int getNeg(){
		if(this.history == Relation.Copy.GLOBAL)
			return (this.global_neg);
		else // if(this.history == Relation.Copy.HONEST)
			return (this.honest_neg);
	}
	
		// *********************** GLOBAL TRUST ******************************
	
	/**
	 * Increment the global-positive feedback count by one
	 */
	public void incGlobalPos(){
		this.global_pos++;
	}
	
	/**
	 * Increment the global-negative feedback count by one
	 */
	public void incGlobalNeg(){
		this.global_neg++;
	}
	
		// *********************** HONEST TRUST ******************************
	
	/**
	 * Increment the actual positive interaction count by one
	 */
	public void incHonestPos(){
		this.honest_pos++;
	}
	
	/**
	 * Increment the actual negative interaction count by one
	 */
	public void incHonestNeg(){
		this.honest_neg++;
	}
	
}

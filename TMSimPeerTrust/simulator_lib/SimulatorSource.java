
package simulator_lib;

import core_lib.*;

/**
 * The SimulatorSource class assists the TraceSimulator driver program in
 * dynamic source selection based on user/file availability and trust values.
 */
public class SimulatorSource{
	
	// ************************** PUBLIC FIELDS ******************************

	/**
	 * The Strategy enumeration lists the source selection strategies.
	 */
	public enum Strategy{BEST, WORST, RAND};
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Given a user Behavior, return the source Strategy they should follow.
	 * @param model The behavior model of some User
	 * @return The source Strategy that user should apply
	 */
	public static Strategy pickStrategy(User.Behavior model){
		if(model == User.Behavior.USR_GOOD)
			return Strategy.BEST;
		else if(model == User.Behavior.USR_PURE)
			return Strategy.WORST;
		else if(model == User.Behavior.USR_FEED)
			return Strategy.RAND;
		else if(model == User.Behavior.USR_PROV)
			return Strategy.WORST;
		else if(model == User.Behavior.USR_DISG)
			return Strategy.RAND;
		else // if(model == User.Behavior.USR_SYBL)
			return Strategy.WORST;
	}
	
	/**
	 * Decide the source for a transaction, given current Network status.
	 * @param nw Network in which the transaction will take place
	 * @param cycle The current cycle
	 * @param recv Identifier of the user requesting the file
	 * @param file Identifier of the file being requested
	 * @param strategy Source selection Strategy being employed
	 * @return Identifier of source user, or -1 if no source exists
	 */
	public static int pickSource(Network nw, int cycle, int recv, int file, 
			Strategy strategy){
		if(strategy == Strategy.BEST)
			return (sourceBest(nw, cycle, recv, file));
		else if(strategy == Strategy.WORST)
			return (sourceWorst(nw, cycle, recv, file));
		else // if(strategy == Strategy.RAND)
			return (sourceRandom(nw, cycle, recv, file));
	}
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Choose the best (most trusted) available source for a transaction.
	 * @param nw Network in which transaction will take place
	 * @param cycle The current cycle
	 * @param recv Identifier of the user requesting the file
	 * @param file Identifier of the file being requested
	 * @return Identifier of source user, or -1 if no source exists
	 */ 
	private static int sourceBest(Network nw, int cycle, int recv, int file){
		double max_trust = Double.NEGATIVE_INFINITY;
		int pos_sources = 0;
		double[] scratch_vector = new double[nw.GLOBALS.NUM_USERS];
		
		boolean cond1, cond2;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			cond1 = nw.hasFile(i, file);
			cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
			if(!cond1 || !cond2)
				scratch_vector[i] = Double.NEGATIVE_INFINITY;
			else if(nw.getUserRelation(recv, i).getTrust() > max_trust){
				pos_sources = 1;
				max_trust = nw.getUserRelation(recv, i).getTrust();
				scratch_vector[i] = max_trust;
			} else if(nw.getUserRelation(recv, i).getTrust() == max_trust){
				pos_sources++;
				scratch_vector[i] = max_trust;
			} else
				scratch_vector[i] = Double.NEGATIVE_INFINITY;
		} // Count quantity of peers with maximum trust value
		
		if(pos_sources == 0)
			return -1; // If no sources available, report that fact
		
		int source_num = (int)((nw.GLOBALS.RAND.nextDouble()*pos_sources)+1.0);
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(scratch_vector[i] == max_trust)
				source_num--;
			if(source_num == 0)
				return i; // Break loop once we have randomly selected provider 
		} // Iterate over all peers
		return -1; // Unused
	}

	/**
	 * Choose the worse (least trusted) available source for a transaction.
	 * @param nw Network in which transaction will take place
	 * @param cycle The current cycle
	 * @param recv Identifier of the user requesting the file
	 * @param file Identifier of the file being requested
	 * @return Identifier of source user, or -1 if no source exists
	 */
	private static int sourceWorst(Network nw, int cycle, int recv, int file){
		double min_trust = Double.POSITIVE_INFINITY;
		int pos_sources = 0;
		double[] scratch_vector = new double[nw.GLOBALS.NUM_USERS];
		
		boolean cond1, cond2;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			cond1 = nw.hasFile(i, file);
			cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
			if(!cond1 || !cond2)
				scratch_vector[i] = Double.POSITIVE_INFINITY;
			else if(nw.getUserRelation(recv, i).getTrust() < min_trust){
				pos_sources = 1;
				min_trust = nw.getUserRelation(recv, i).getTrust();
				scratch_vector[i] = min_trust;
			} else if(nw.getUserRelation(recv, i).getTrust() == min_trust){
				pos_sources++;
				scratch_vector[i] = min_trust;
			} else
				scratch_vector[i] = Double.POSITIVE_INFINITY;
		} // Count quantity of peers with maximum trust value
		
		if(pos_sources == 0)
			return -1; // If no sources available, report that fact
		
		int source_num = (int)((nw.GLOBALS.RAND.nextDouble()*pos_sources)+1.0);
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(scratch_vector[i] == min_trust)
				source_num--;
			if(source_num == 0)
				return i; // Break loop once we have randomly selected provider 
		} // Iterate over all peers
		return -1; // Unused
	}
	
	/**
	 * Choose a random available source for a transaction.
	 * @param nw Network in which transaction will take place
	 * @param cycle The current cycle
	 * @param recv Identifier of the user requesting the file
	 * @param file Identifier of the file being requested
	 * @return Identifier of source user, or -1 if no source exists
	 */
	private static int sourceRandom(Network nw, int cycle, int recv, int file){
		int pos_sources = 0;
		double[] scratch_vector = new double[nw.GLOBALS.NUM_USERS];
		
		boolean cond1, cond2;
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			cond1 = nw.hasFile(i, file);
			cond2 = nw.getUser(i).BWidthAvailableUL(cycle);
			if(cond1 && cond2){
				pos_sources++;
				scratch_vector[i] = Double.POSITIVE_INFINITY;
			} else
				scratch_vector[i] = Double.NEGATIVE_INFINITY;
		} // Count quantity of potential sources
		
		if(pos_sources == 0)
			return -1; // If no sources exist, this should be noted
		
		int source_num = (int)((nw.GLOBALS.RAND.nextDouble()*pos_sources)+1.0);
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			if(scratch_vector[i] == Double.POSITIVE_INFINITY)
				source_num--;
			if(source_num == 0)
				return i; // Break loop once we have randomly selected provider 
		} // Iterate over all peers
		return -1; // Unused	
	}
	
}

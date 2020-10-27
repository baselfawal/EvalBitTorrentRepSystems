
package simulator_lib;

import java.util.*;
import trust_system_lib.*;
import core_lib.*;

/**
 * The SimulatorMalicious class is used to coordinate malicious user behaviors,
 * especially with regards to coordinated activity and feedback switching.
 */
public class SimulatorMalicious{
	
	// ************************* PRIVATE FIELDS *****************************

	/**
	 * The MAL_STRATEGY enumeration lists malicious tactics that can be used
	 */
	public enum MAL_STRATEGY{NAIVE, ISOLATED, COLLECTIVE}; 
	
	/**
	 * The malicious strategy being applied by this instance.
	 */
	private final MAL_STRATEGY strat;
	
	/**
	 * Network which this SimulatorMalicious object is being applied too.
	 */
	private final Network nw;
	
	/**
	 * List containing UserID's of those participating in malicious-collective
	 */
	private List<Integer> collective;
	
	// *************************** CONSTRUCTORS ******************************
	
	/**
	 * Construct a SimulatorMalicious object.
	 * @param nw Network over which this Object should operate
	 * @param strat Malicious strategy being applied in this instance
	 */
	public SimulatorMalicious(Network nw, MAL_STRATEGY strat){
		this.nw = nw;
		this.strat = strat;
	}
	
	// ************************** PUBLIC METHODS *****************************
	
	/**
	 * Compute trust according to some algorithm, over a set of feedback data.
	 * Which data is set is used is set according to this object.
	 * @param recv Identifier of user performing trust computation
	 * @param cycle The current cycle in the simulator framework
	 * @param ALG Algorithm being brought to bear on interaction data
	 */
	public void computeTrust(int recv, int cycle, TrustAlg ALG){
		
		if(cycle == 0) // We have to get this init'ed before update called
			ALG.computeTrust(recv, cycle);
		
		if(this.strat == MAL_STRATEGY.NAIVE)
			ALG.computeTrust(recv, cycle);
		
		else if(this.strat == MAL_STRATEGY.ISOLATED){	
			if(nw.getUser(recv).getModel() != User.Behavior.USR_GOOD){
				this.setVecRelations(Relation.Copy.HONEST, recv, ALG);
				ALG.computeTrust(recv, cycle);
				this.setVecRelations(Relation.Copy.GLOBAL, recv, ALG);
			} else
				ALG.computeTrust(recv, cycle);
			
		} else if(this.strat == MAL_STRATEGY.COLLECTIVE){
			if(cycle == 0){
				collective = new ArrayList<Integer>();
				for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
					if(nw.getUser(i).getModel() != User.Behavior.USR_GOOD)
						collective.add(i);		
				} // Add all non-'good' users to a collective
			} else{
				if(nw.getUser(recv).getModel() != User.Behavior.USR_GOOD){
					int cur;
					for(int i=0; i < collective.size(); i++){
						cur = collective.get(i);
						this.setVecRelations(Relation.Copy.HONEST, cur, ALG);
					} // Get all malicious peers to share honest data
					ALG.computeTrust(recv, cycle);	
					for(int i=0; i < collective.size(); i++){
						cur = collective.get(i);
						this.setVecRelations(Relation.Copy.GLOBAL, cur, ALG);
					} // Switch all settings back before exit
				} else
					ALG.computeTrust(recv, cycle);
			} // Setup strategy the first time, apply thereafter
		} // Change data set according to malicious strategy
	}
	
	// ************************** PRIVATE METHODS ****************************
	
	/**
	 * Set all relations to report a particular feedback type when queried.
	 * @param setting Feedback type which we want to be reported
	 */ /*
	private void setAllRelations(Relation.Copy setting, TrustAlg ALG){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			for(int j=0; j <nw.GLOBALS.NUM_USERS; j++){
				nw.getUserRelation(i, j).setHistory(setting);
				ALG.update(new Transaction(-1, i, j, -1, false));
			} // Appropriate TM structures must update at each change
		} // Set all relations to the desired feedback setting
	}*/
	
	/**
	 * Set all (user->x) relations in this Network to report a particular 
	 * feedback type when queried, where 'user' is a fixed User.
	 * @param setting Feedback type which we want to be reported
	 * @param vec User (numerical) vector whose relations are to be set 
	 */
	private void setVecRelations(Relation.Copy setting, int vec, TrustAlg ALG){
		for(int i=0; i < nw.GLOBALS.NUM_USERS; i++){
			nw.getUserRelation(vec, i).setHistory(setting);
			ALG.update(new Transaction(-1, i, vec, -1, false));
		} // Set all relations in some vector to a desired feedback setting
	} 

}

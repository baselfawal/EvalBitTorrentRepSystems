
package simulator_lib;

import core_lib.*;
import trust_system_lib.TrustAlg;

/**
 * The SimulatorUtils class assists the TraceSimulator driver. Its work
 * most pertains to the queuing and commitment of transactions (actually 
 * transferring files and deciding feedbacks).
 */
public class SimulatorUtils{
	
	// ************************** PUBLIC METHODS *****************************

	/**
	 * Simulate a transaction on a network.
	 * @param nw Network in which the transaction should be simulated
	 * @param cyc The current cycle
	 * @param trans The Transaction to be simulated
	 * @param mal Object coordinating malicious user activity
	 * @param ALG Trust algorithm instance managing Network 'nw'
	 */
	public void simTrans(Network nw, int cyc, Transaction trans, 
			SimulatorMalicious mal, TrustAlg ALG){
		transactionCommit(nw, cyc, ALG);
		transactionQueue(nw, cyc, trans, mal, ALG);
	}
	
	/**
	 * Commit all remaining transactions in a Network delay queue.
	 * @param nw The Network with outstanding (queued) transactions
	 * @param cycle Cycle from which to begin commitments 
	 * @param ALG Trust algorithm instance managing Network 'nw'
	 */
	public void commitRemaining(Network nw, int cycle, TrustAlg ALG){
		int inc = 0;
		while(nw.queueSize() != 0){
			transactionCommit(nw, (cycle+inc), ALG);
			inc++;
		} // Commit all remaining transactions in delay queue	
	}
	
	// ************************** PRIVATE METHODS ****************************

	/**
	 * If ready (complete), commit the Transaction atop the delay queue.
	 * @param nw Network whose queued Transaction will commit (if ready)
	 * @param cycle The current cycle
	 * @param ALG Trust algorithm instance managing Network 'nw'
	 * @return The committed transaction, or NULL if none was committed
	 */
	private Transaction transactionCommit(Network nw, int cycle, TrustAlg ALG){
		Transaction cur_trans = nw.peekQueue();
		if((cur_trans != null) && (cur_trans.getCommit() == cycle)){
			commitFile(nw, cur_trans);
			commitFBack(nw, cur_trans);
			ALG.update(cur_trans);
			nw.dequeueTrans();
			return cur_trans;
		} else // If transaction at queue head is ready to be committed, do so
			return null;
	}
	
	/**
	 * Transfer a file between two users.
	 * @param nw Network in which the transfer should take place
	 * @param trans Transaction detailing parties/parameters of transfer
	 */
	private void commitFile(Network nw, Transaction trans){
		int recv = trans.getRecv();
		double rand = nw.GLOBALS.RAND.nextDouble();
		if(!trans.getValid()){ 
			nw.STATS.NUM_INVAL_TRANS++;
			if(nw.getUser(recv).getModel() == User.Behavior.USR_GOOD)
				nw.STATS.NUM_GOOD_FAIL++;
			if(rand > nw.getUser(recv).getCleanup())
				nw.addFile(trans.getRecv(), trans.getFile(), trans.getValid());
		} else{ // If a bad file is received
			if(nw.getUser(recv).getModel() == User.Behavior.USR_GOOD){
				nw.STATS.NUM_GOOD_SUCC++;
				nw.addFile(trans.getRecv(), trans.getFile(), trans.getValid());
			} else if(rand > (1.0 - nw.getUser(recv).getCleanup()))
				nw.addFile(trans.getRecv(), trans.getFile(), trans.getValid());
		} // Else if a good file is received
		return;
	}
	
	/**
	 * Commit feedback upon transaction completion.
	 * @param nw Network in which feedback should be made
	 * @param trans Transaction detailing parties/parameters of feedback
	 */
	private void commitFBack(Network nw, Transaction trans){
		
		int send = trans.getSend();
		int recv = trans.getRecv();
		
		boolean a = nw.getUser(send).getModel() == User.Behavior.USR_SYBL;
		boolean b = nw.getUser(recv).getModel() == User.Behavior.USR_SYBL;
		if(a || b){
			nw.STATS.NUM_FBACK_SYBL++;
			return;
		} // If sender or receiver is Sybil, no feedback is recorded.
		
			// Store accurate interaction history 
		if(trans.getValid())
			nw.getUserRelation(recv, send).incHonestPos();
		else
			nw.getUserRelation(recv, send).incHonestNeg();
		
		double rand = nw.GLOBALS.RAND.nextDouble();
		if(rand > nw.getUser(recv).getHonesty()){
			nw.STATS.NUM_FBACK_LIES++;
			if(trans.getValid())
				nw.getUserRelation(recv, send).incGlobalNeg();
			else
				nw.getUserRelation(recv, send).incGlobalPos();
		} else{ // Some users will be dishonest in providing global-feedback
			nw.STATS.NUM_FBACK_TRUE++;
			if(trans.getValid())
				nw.getUserRelation(recv, send).incGlobalPos();
			else
				nw.getUserRelation(recv, send).incGlobalNeg();
		} // Whereas other users will provide truthful global-feedback
	}
	
	/**
	 * Begin a transaction between parties, queuing it for later commitment.
	 * @param nw  Network in which the transaction will take place 
	 * @param cycle The current cycle
	 * @param trans Transaction detailing parties/parameters of transaction
	 * @param mal Object coordinating malicious user activity
	 * @param ALG Trust algorithm instance managing Network 'nw'
	 */
	private void transactionQueue(Network nw, int cycle, Transaction trans, 
			SimulatorMalicious mal, TrustAlg ALG){
		
		int recv = trans.getRecv();
		int file = trans.getFile();
		boolean cond1 = nw.hasFile(recv, file);
		boolean cond2 = nw.getUser(recv).BWidthAvailableDL(cycle);
		if(cond1 || !cond2){
			nw.STATS.NUM_RECV_BLK_TR++;
			return;
		} // If receiving user already has file or no DL bandwidth, abort.
		
			// Setup the distributed malicious strategy, do trust computation
		mal.computeTrust(recv, cycle, ALG); 
		
			// Pick source user based on user model/availability
		User.Behavior model = nw.getUser(recv).getModel();
		SimulatorSource.Strategy strategy = SimulatorSource.pickStrategy(model);
		int send = SimulatorSource.pickSource(nw, cycle, recv, file, strategy);
		
		if(send == -1){
			nw.STATS.NUM_SEND_BLK_TR++;
			return;
		} // If query unanswered or no sources have b-width, abort
		
			// Transaction is proceeding, consume bandwidth
		nw.getUser(recv).BWidthConsumeDL(cycle);
		nw.getUser(send).BWidthConsumeUL(cycle);
		
		boolean valid = nw.fileCopyValid(file, send);
		int commit = cycle + nw.GLOBALS.BAND_PER;
		Transaction t = new Transaction(commit, send, recv, file, valid);
		nw.enqueueTrans(t);
	}	

}

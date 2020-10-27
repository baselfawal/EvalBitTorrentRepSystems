package peersim.bittorrent;

import peersim.core.*;

public class FeedbackMsg extends SimpleMsg {
	
	/**
	 *	The data value (an integer) contained in the message.
	 */
	protected int fbscore;
	
	protected int senderscore;
	
	/**
	 *	The data value (an integer) contained in the message.
	 */
	
	private Node peer;
	
	/**
	 *	The basic constructor of the message.
	 *	@param type the type of the message
	 *	@param sender The sender node
	 *	@param fbscore the feedback value submitted by sender
	 *  @param peer object node for which this feedback for
	 *  @param senderscore this is the sender reputation score who submitted the feedback
	 */
	public FeedbackMsg (int type, Node sender, int fbscore, Node peer, int senderscore){
		super.type = type;
		super.sender = sender;
		this.fbscore = fbscore;
		this.peer= peer;
		this.senderscore = senderscore;
		
	}
	
	
	/**
	 *	Gets the value contained in the message.
	 *	@return the integer value contained in the message
	 */
	public int getScore(){
		return this.fbscore;	
	}
	
	/**
	 *	Gets the feedback peer.
	 *	@return the object of feedback peer.
	 */
	public Node getfbPeer(){
		return this.peer;	
	}

	public int getSenderScore() {
		return this.senderscore;
	}
}

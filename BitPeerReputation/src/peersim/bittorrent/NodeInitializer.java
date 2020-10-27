/*
 * Copyright (c) 2007-2008 Fabrizio Frioli, Michele Pedrolli
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * --
 *
 * Please send your questions/suggestions to:
 * {fabrizio.frioli, michele.pedrolli} at studenti dot unitn dot it
 *
 */

package peersim.bittorrent;

import peersim.core.*;

import peersim.config.Configuration;

/**
 *	This class provides a way to initialize a single node of the network.
 *	The initialization is performed by choosing the bandwidth of the node
 *	and choosing how much the shared file has been downloaded.
 */
public class NodeInitializer{
	
	/**
	 *	The protocol to operate on.
	 *	@config
	 */
	private static final String PAR_PROT="protocol";
	
	/**
	 *	The percentage of nodes with no downloaded pieces.
	 *	@config
	 *	@see "The documentation for an example on how to properly set this parameter."
	 */
	private static final String PAR_NEWER_DISTR="newer_distr";
	
	/**
	 *	The percentage of seeders in the network.
	 *	@config
	 */
	private static final String PAR_SEEDER_DISTR="seeder_distr";

	/**
	 *	The percentage of slefish peers in the network.
	 *	@config
	 */
	private static final String PAR_SELFISH_DISTR="selfish_distr";
	
	/**
	 *	The percentage of nodes with no downloaded pieces,
	 *	as defined in {@see #PAR_NEWER_DISTR}.
	 */
	private int newerDistr;
	
	/**
	 *	The percentage of seeder nodes,
	 *	as defined in {@see #PAR_SEEDER_DISTR}.
	 */
	private int seederDistr;
	
	/**
	 *	The percentage of nodes with selfish behaviour,
	 *	as defined in {@see #PAR_SELFISH_DISTR}.
	 */
	private int selfishDistr;
	
	/**
	 *	The BitTorrent protocol ID.
	 */	
	private final int pid;
	
	
	/**
	 *	The basic constructor of the class, which reads the parameters
	 *	from the configuration file.
	 *	@param prefix the configuration prefix for this class
	 */
	public NodeInitializer(String prefix){
		pid = Configuration.getPid(prefix+"."+PAR_PROT);
		newerDistr = Configuration.getInt(prefix+"."+PAR_NEWER_DISTR);
		seederDistr = Configuration.getInt(prefix+"."+PAR_SEEDER_DISTR);
		selfishDistr = Configuration.getInt(prefix+"."+PAR_SELFISH_DISTR);
	}
	
	/**
	 *	Initializes the node <tt>n</tt> associating it
	 *	with the BitTorrent protocol and setting the reference to the tracker,
	 *	the status of the file and the bandwidth.
	 *	@param n The node to initialize
	 */
	public void initialize(Node n){
		Node tracker = Network.get(0);
		BitTorrent p;
		p = (BitTorrent)n.getProtocol(pid);
		p.setTracker(tracker);
		p.setThisNodeID(n.getID());
		setFileStatus(p);
		setBandwidth(p);
		//set the last node to be a selfish peer
		//if (p.getThisNodeID()== Network.size()-1) {
			//p.setSelfish(); //make the peer selfish
		//}
	}

	/**
	 *	Sets the status of the shared file according to the
	 *	probability value given by {@link #getProbability()}.
	 *	@param p The BitTorrent protocol
	 */
	private void setFileStatus(BitTorrent p){
		int percentage = getProbability();
		choosePieces(percentage, p);
	}
	
	/**
	 *	Set the maximum bandwidth for the node, choosing
	 *	uniformly at random among 4 values.
	 *	<p>
	 *	The allowed bandwidth speed are 640 Kbps, 1 Mbps, 2 Mbps and 4 Mbps.
	 *	</p>
	 *	@param p The BitTorrent protocol
	 */
	private void setBandwidth(BitTorrent p){
		int value = CommonState.r.nextInt(4);
		switch(value){
			case 0: p.setBandwidth(1024);break; //640Kbps
			case 1: p.setBandwidth(1024);break;// 1Mbps
			case 2: p.setBandwidth(1024);break;// 2Mbps
			case 3: p.setBandwidth(1024);break; //4Mbps
		}
	}
	
	
	/**
	 *	Sets the completed pieces for the given protocol <tt>p</tt>.
	 *	@parm percentage The percentage of the downloaded pieces, according to {@link #getProbability()}
	 *	@param p the BitTorrent protocol
	 */
	private void choosePieces(int percentage, BitTorrent p){
		double temp = ((double)p.nPieces/100.0)*percentage; // We use a double to avoid the loss of precision
												 // during the division operation
		int completed = (int)temp; //integer number of piece to set as completed
							  //0 if the peer is a newer
		if (completed == -1) 
				completed =0; //the perecentage here has no meaning considerd 0 like a new leecher
		//print to verfify
		//System.out.println("node id: " + p.getThisNodeID() + " percentage: " + percentage + " temp: " + temp + " completed: "+ completed);
		p.setCompleted(completed);
		if(percentage == 100)
			p.setPeerStatus(1);
		
		if (percentage == -1)
			p.setSelfish(); //it is a selfish peer
		
		int tmp;
		while(completed!=0){
			tmp = CommonState.r.nextInt(p.nPieces);
			if(p.getStatus(tmp)!=16){
				p.setStatus(tmp, 16);
				completed--;
			}
		}
	}
	
	/**
	 *	Gets a probability according with the parameter <tt>newer_distr</tt>
	 *	defined in the configuration file.
	 *	@return the probabilty value, where 0 means that the peer is new and no pieces has been downloaded,
	 *			100 means that the peer is a seeder; other values defines a random probability.
	 *	@see #PAR_NEWER_DISTR
	 */
	private int getProbability(){
		int value = CommonState.r.nextInt(100);
		System.out.println(" " + value + " ");
		int range1 = seederDistr;
		int range2 = seederDistr + selfishDistr;		
				
		//System.out.println("range1 " + range1 + " range2 " + range2 + " range3 " + range3);
		
		if((value+1)<=range1)
			return 100;
		
		//value is between range 1 and range 2 this is the selfish peer distribution
		if (( (value +1) > range1) && ((value+1) <= range2))
			return -1; // -1 is a denotation of selfish peer, value between seederDist and seederDistr + selfishDistr
		
		//value is greater than range3 
		//now the rest is the distiribution of leechers 
		//leechers contains peers with 0 pieces and with partial pieces
		//I have chosen to make 25% of the leecher has partial pieces.
			int PartialLeecherDistr = CommonState.r.nextInt(100);
			if (PartialLeecherDistr +1 <= 75 ) return 0; // A newer peer, with probability newer_distr
		    else { //this is a leecher that has some parts, perecentage of parts
			value = CommonState.r.nextInt(9);
			return (value+1)*10;
		    }
		 
	}
}
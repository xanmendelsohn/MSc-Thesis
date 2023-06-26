
package vecDeffuant;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import uchicago.src.reflector.DescriptorContainer;
import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.*;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.util.Random;

//The class DeffuantAgent defines the possible configuration for each site and how sites/agents interact with one another.

public class DeffuantAgent implements Drawable {

  	private int featureCount; // Number of cultural features.
  	public int feature;
  	private int threshold;    // the interaction threshold. Must be between 0 and featureCount
  	private RegionCounter regionCounter;
  	private double p;		// The probability that the interaction result is in favour of the agent with config 1 when when two agent interact w.r.t. a certain feature 
	private AgentColour siteColour;	// Object that determines the colour of the displayed agent.
  	private Grid space;		// The grid in which the agents are situated.
	private int[] traitCounts;	// The number of traits possessed by each feature. Allows for variable numbers of traits, but all are set to two in the vec. Deff. model.
	private int[] traits;	// The array of current configuration (\in {0,1}^featureCount).
	private double dissociating; //Corresponds to the kappa in the probability generator of the dissociating vec. Deff. Model. Default set to 1.0 (standard vec. Deff. Model)

	/** Indicates whether this site has been processed in analysing regions and zones.	*/
	protected boolean done;	// N.B. accessed directly from RegionCounter class.

	/** Agent's grid coordinates.	*/
	protected int x, y;	// N.B. accessed directly from RegionCounter class.

	/**
		Create the agent.

		@param x, y: the grid coordinates for agent.
		@param space: the grid in which the agents are situated.
		@param featureCount: number of cultural features possessed by the agent.
		@param traitCount: number of traits possessed by all features.
		@param initialTraits: initial configuration.
		@param siteColour: the object that determines the colour of the displayed agent.
	*/
	public DeffuantAgent(int x, int y, Grid space, int featureCount, int traitCount,
			int[] initialTraits, AgentColour siteColour, int threshold, double p, double dissociating) {
		this.x = x;
		this.y = y;
		this.featureCount = featureCount;
		this.threshold = threshold;
		this.p = p;
		this.dissociating = dissociating;
     	this.space = space;
   		this.siteColour = siteColour;
    	this.traitCounts = new int[featureCount];
    	this.traits = new int[featureCount];
		for( int i = 0; i < featureCount; i++ ) {
			this.traits[i] = initialTraits[i];
			this.traitCounts[i] = 2;
		}
	}

	/**
		Implements Drawable interface. Agents are drawn with different colors to identify their
		current configuration.
	*/
  	public void draw(SimGraphics g) {
    	Color c = siteColour.getColour(traits);
    	g.drawFastRect(c);
    }

  	// get/set methods allowing the agent's state to be probed.
  	// --------------------------------------------------------
	public int getX() {return x;}
	public int getY() {return y;}
	public String getTraits() {return traitsToString();}
	public void setTraits(String newTraits) {
		boolean traitsChanged = false;
		int i, t;
		String s;
		StringTokenizer st = new StringTokenizer( newTraits, " ,\t\n\r\f" );
		if(st.countTokens() != featureCount ) {
			System.out.println("Incorrect number of traits.");
			return;
		}
		int[] nt = new int[featureCount];
		for( i = 0; i < featureCount; i++ ) {
			s = st.nextToken();
			try {
				t = Integer.parseInt(s);
				if( t < 0 || t >= 2 ) {
					System.out.println("Configuration "+t+" is invalid.");
					return;
				}
				nt[i] = t;
			} catch(NumberFormatException ex) {
				System.out.println("Configuration "+s+" is invalid.");
				return;
			}
		}
		for( i = 0; i < featureCount; i++ ) {
			if(traits[i] != nt[i]) {
				traitsChanged = true;
				traits[i] = nt[i];
			}
		}
		if(traitsChanged)
			System.out.println("Site ("+x+","+y+") config. changed to "+newTraits+".");
	}

	/**
		Returns the number of features in this site that are different from the specified site.
		(Used to count regions.) 'Distance' implies the Hamming distance on the state space.

		@param site another site.
		@return cultural distance.
		@throws IllegalArgumentException if sites have different numbers of features.
	*/
	public int distance(DeffuantAgent site) {
		int count = 0;
		if(this.featureCount != site.featureCount)
			throw new IllegalArgumentException("Incompatible sites.");
		for( int i = 0; i < featureCount; i++ )
			if(this.traits[i] != site.traits[i]) ++count;
		return count;
	}
	
	
	public int getTrait(int feature) {
		if(feature < 0 ||  feature > featureCount)
			throw new IllegalArgumentException("feature not in range");
		return this.traits[feature];
	}

	/**
	 	interactStandard() defines interaction btw two agents in the standard vec. Deff. Model
		Convergent interaction with a neighbouring site.
		Select at random a feature on which this agent and its neighbour
		differ (if there is one) and let a Bernoulli random variable with density p (p=0.5 default) decide who 
		assimilates.

		@param neighbour a neighbouring agent.
		@param winner stores the value of the Bernoulli random variable
		@param featuresDiffer stores the features which differ between agent and neighbour in a list
		@return true if a change took place; false otherwise.
		
	*/
	
	private boolean interactStandard( DeffuantAgent neighbour) {
        int featureTry;		// bit being tried looking for dissimilarity.
        double winner;
        ArrayList<Integer> featuresDiffer = new ArrayList<Integer>();
        
        for( int i = 0; i < featureCount; i++ ){
			if(this.traits[i] != neighbour.traits[i])
			{featuresDiffer.add(i);}
        }
        
        if(featuresDiffer.isEmpty()){
        	return false;
        }
	else{
    	  
		Collections.shuffle(featuresDiffer);
		featureTry = featuresDiffer.get(0);	
    	winner = Random.uniform.nextDoubleFromTo(0,1);
      	
 			if( traits[featureTry] > neighbour.traits[featureTry] ) {
				if(winner <= p ){
					neighbour.traits[featureTry] = traits[featureTry];
					} else{
				traits[featureTry] = neighbour.traits[featureTry];
					}
				return true;
				}
 			else{
				if(winner <= p ){
	 				traits[featureTry] = neighbour.traits[featureTry];
						} else{
							neighbour.traits[featureTry] = traits[featureTry];
					}
				return true;
 			}
      } 
	}
	
	/**
 	interactDissociating() defines interaction btw two agents in the dissociating vec. Deff. Model
	Convergent interaction with a neighbouring site.
	Select at random a feature on which this agent and its neighbour
	differ (if there is one). Which agent has to assimilate now depends on their current configuration
	and a Bernoulli random variable with density q (p=dissociating/(1+dissociating)). 
	Several cases have to be checked to make interaction correspond to the flip rates of the diss. vec. Deff. Model

	@param agent a randomly selected agent.
	@param neighbour a neighbouring agent.
	@param winner stores the value of the Bernoulli random variable
	@param featuresDiffer stores the features which differ between agent and neighbour in a list
	@return true if a change took place; false otherwise.
	
*/
	
	private boolean interactDissociating(DeffuantAgent agent, DeffuantAgent neighbour) {
        int bitTry;		// bit being tried looking for dissimilarity.
        double winner;
        DeffuantAgent zeros;
        ArrayList<Integer> featuresDiffer = new ArrayList<Integer>();
        for( int i = 0; i < featureCount; i++ ){
			if(this.traits[i] != neighbour.traits[i])
			{featuresDiffer.add(i);}
        }
        int[] zeroTraits = new int[featureCount];
		for(int i = 0; i < featureCount; i++) zeroTraits[i] = 0;
		zeros = new DeffuantAgent(-1, -1, null, featureCount, 2, zeroTraits, null, threshold, p, dissociating);
		
		double centralPosition = (double) featureCount/2;
		double agentPosition = agent.distance(zeros);
		double neighbourPosition = neighbour.distance(zeros);
		double q = dissociating/(1+dissociating);
		
	if( !featuresDiffer.isEmpty()){
     if(agentPosition <= centralPosition && neighbourPosition<=centralPosition && agentPosition < neighbourPosition){ 
     		
  			Collections.shuffle(featuresDiffer);
  			bitTry = featuresDiffer.get(0);
  			winner = Random.uniform.nextDoubleFromTo(0,1);
      	
 			if(traits[bitTry] > neighbour.traits[bitTry] ) {
				if(winner <= 0.5){
					neighbour.traits[bitTry] = traits[bitTry];
					} else{
				traits[bitTry] = neighbour.traits[bitTry];
					}
				return true;
				}
 			else{
				if(winner <= 1-q){
	 				traits[bitTry] = neighbour.traits[bitTry];
						} else{
							neighbour.traits[bitTry] = traits[bitTry];
					}
				return true;
 			}
    }
       if(agentPosition <= centralPosition && neighbourPosition<=centralPosition && agentPosition > neighbourPosition){ 
    	      		
    	   		Collections.shuffle(featuresDiffer);
    	   		bitTry = featuresDiffer.get(0);
    	     	winner = Random.uniform.nextDoubleFromTo(0,1);
    	      	
    	 			if(traits[bitTry] > neighbour.traits[bitTry] ) {
    					if(winner <= 1-q){
    						neighbour.traits[bitTry] = traits[bitTry];
    						} else{
    					traits[bitTry] = neighbour.traits[bitTry];
    						}
    					return true;
    					}
    	 			else{
    					if(winner <= 0.5){
    		 				traits[bitTry] = neighbour.traits[bitTry];
    							} else{
    								neighbour.traits[bitTry] = traits[bitTry];
    						}
    					return true;
    	 			}
    	    }
       if(agentPosition >= centralPosition && neighbourPosition>=centralPosition && agentPosition < neighbourPosition){ 
   
    	   		Collections.shuffle(featuresDiffer);
    	   		bitTry = featuresDiffer.get(0);
    	    	winner = Random.uniform.nextDoubleFromTo(0,1);
    	      	
    	 			if(traits[bitTry] > neighbour.traits[bitTry] ) {
    					if(winner <= 0.5){
    						neighbour.traits[bitTry] = traits[bitTry];
    						} else{
    					traits[bitTry] = neighbour.traits[bitTry];
    						}
    					return true;
    					}
    	 			else{
    					if(winner <= q){
    		 				traits[bitTry] = neighbour.traits[bitTry];
    							} else{
    								neighbour.traits[bitTry] = traits[bitTry];
    						}
    					return true;
    	 			}
    	    }
    	   if(agentPosition >= centralPosition && neighbourPosition>=centralPosition && agentPosition > neighbourPosition){ 
    		   
   	   			Collections.shuffle(featuresDiffer);
   	   			bitTry = featuresDiffer.get(0);
   	   			winner = Random.uniform.nextDoubleFromTo(0,1);
    	    	      	
    	    	 			if(traits[bitTry] > neighbour.traits[bitTry] ) {
    	    					if(winner <= q){
    	    						neighbour.traits[bitTry] = traits[bitTry];
    	    						} else{
    	    					traits[bitTry] = neighbour.traits[bitTry];
    	    						}
    	    					return true;
    	    					}
    	    	 			else{
    	    					if(winner <= 0.5){
    	    		 				traits[bitTry] = neighbour.traits[bitTry];
    	    							} else{
    	    								neighbour.traits[bitTry] = traits[bitTry];
    	    						}
    	    					return true;
    	    	 			}
    	    	    }
    	   else{
		
			Collections.shuffle(featuresDiffer);
  			bitTry = featuresDiffer.get(0);
  			winner = Random.uniform.nextDoubleFromTo(0,1);
    	
			if(traits[bitTry] > neighbour.traits[bitTry] ) {
				if(winner <= 0.5 ){
					neighbour.traits[bitTry] = traits[bitTry];
					} else{
				traits[bitTry] = neighbour.traits[bitTry];
					}
				return true;
				}
			else{
				if(winner <= 0.5 ){
	 				traits[bitTry] = neighbour.traits[bitTry];
						} else{
							neighbour.traits[bitTry] = traits[bitTry];
					}
				return true;
			}
		}
	}
	else {return false;}
 }

	/**
		Returns the number of characters in an integer number.
		The sign of the number is included.

		@return the length of n.
	*/
	private static int lengthOf(int n)
		{ return(Integer.toString(n).length()); }

	/** Performs a mutation. A features configuration is randomly changed.	*/
	public void mutate() {
       	int oldAllele, newAllele, bit, allelemax;

    	bit = Random.uniform.nextIntFromTo(0,featureCount-1);	// Randomly choose a feature to mutate.
        oldAllele = traits[bit];
 		allelemax = 2;
 		do	{
    		newAllele = Random.uniform.nextIntFromTo(0,allelemax-1);
		} while(newAllele == oldAllele);
       	traits[bit] = newAllele;
	}

	/**
		Executes one step of the RePast simulation.
		This method only attempts one interaction event, which may not require updating the display etc.
		Within one tick the RePast simulation will attempt up to 10000 steps until an interaction is successful.

		@return true if an interaction took place; false otherwise.
	*/
	public boolean step(DeffuantAgent agent) {
		DeffuantAgent neighbour;
   		neighbour = (DeffuantAgent) space.getNeighbour(x,y);	// Randomly choose a neighbour.

   			if(dissociating == 1){
		if(this.distance(neighbour) <= threshold && this.distance(neighbour) != 0)
    		{return(interactStandard( neighbour));}
    	else return false;
   			}
   			else{
   		if(this.distance(neighbour) <= threshold && this.distance(neighbour) != 0)
   	    	{return(interactDissociating( agent,  neighbour));}
   	    else return false;
   			}
 	}

	/** Return the current configuration as a string of numbers.	*/
	public String traitsToString() {
		StringBuffer sb = new StringBuffer(featureCount*2);
		for( int i = 0; i < featureCount; i++ ) {
			for( int j = 0; j < lengthOf(1)-lengthOf(traits[i]); j++ )
				sb.append(" ");
			sb.append(traits[i]+" ");
		}
		return sb.toString();
	}
}



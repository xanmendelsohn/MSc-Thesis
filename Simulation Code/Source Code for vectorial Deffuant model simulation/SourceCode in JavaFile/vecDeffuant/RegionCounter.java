package vecDeffuant;


import java.util.*;

public class RegionCounter {
	
	/**
 	This class defines functions to analyse the global distribution of configurations.
*/

	private Vector<DeffuantAgent> readyQueue = new Vector<DeffuantAgent>(20,10);	
  	private Vector myNeighborhood;

	int featureCount;
	int threshold;
	double dissociating;
	double impactIntensity;
	ArrayList agentList;
	Grid space;

	/**
		Create the region counter.

		@param featureCount the number of features in the cultural model.
		@param agentList the agents in the cultural model.
		@param space the grid of agents in the cultural model.
	*/
	public RegionCounter(int featureCount, ArrayList agentList, Grid space, int threshold, double dissociating) {
		this.featureCount = featureCount;
		this.agentList = agentList;
		this.space = space;
		this.threshold = threshold;
		this.dissociating = dissociating;
 	}

	/**
		@param maxDistance if the number of features that are different exceed this,
			then sites are in different zones.
		@return number of regions that have more than maxDistance features that are different.
	*/
	private int analyzeZones(int maxDistance) {
		// count number of regions (maxDistance=0) or zones(maxDistance = threshold).
        // based on breadth first search. See Stubbs and Webre, Data Structures, 359ff.
        int i;
        int regions = 0;
        readyQueue.clear();
 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			agent.done = false;
		}

 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			if(!agent.done)	{// if not done then record it and visit it.
            	++regions;	// count regions
              	visit(agent, maxDistance);	// 0 is min distance allowed for regions.
 			}
		}

      	return regions;
 	}
	
	private float analyzeAveZonesSize(int maxDistance) {
		// Gives the average size of regions (maxDistance=0) or zones(maxDistance = threshold).
        // based on breadth first search. See Stubbs and Webre, Data Structures, 359ff.
        int i;
        int regions = 0;
        int sumOfRegionSizes =0;
        readyQueue.clear();
 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			agent.done = false;
		}

 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			if(!agent.done)	{// if not done then record it and visit it.
            	++regions;	// count regions
            	sumOfRegionSizes += visit(agent, maxDistance);
              	visit(agent, maxDistance);	// 0 is min distance allowed for regions.
 			}
		}

      	return (float) sumOfRegionSizes/regions;
 	}
	
	private int analyzeDisagreements(int maxDistance) {
		// count number of regions, and types.
        // based on breadth first search. See Stubbs and Webre, Data Structures, 359ff.
        int i;
        int disagreements = 0;
 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
            disagreements += visitD(agent, maxDistance);
		}

      	return  disagreements/2;
 	}
	
	private double analyzeSocialPopularity(int feature) {
		// Gives the number of agents with a configuration 
		// where the specified feature (@param feature) is set to 1 minus (totalNumberSites/2).
        int i;
        int sites = 0;
        int totalNumberSites = space.getSizeX()*space.getSizeY();
        readyQueue.clear();
 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			agent.done = false;
		}

 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			if(!agent.done)	{// if not done then record it and visit it.
            	sites += agent.getTrait(feature);
 			}
		}
      	return (double) sites-(totalNumberSites/2);
 	}
	

	
	private int spreadCount(int j) {
		// Gives the number of agents with a configuration which has the Hamming j 
		// from the configuration (0,...,0)
		int i;
		DeffuantAgent zeroConfig;
        int sites = 0;
        
        int[] broadcastTraits = new int[featureCount];
		for( i = 0; i < featureCount; i++) broadcastTraits[i] = 0;
		zeroConfig = new DeffuantAgent(-1, -1, null, featureCount, 2, broadcastTraits, null, threshold, 0.5, dissociating);
		
        readyQueue.clear();
 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			if(agent.distance(zeroConfig)==j){
			agent.done = false;}
		}

 		for (i = 0; i < agentList.size(); i++) {
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			if(!agent.done)	{// if not done then record it and visit it.
            	++sites;	// count regions
              	agent.done= true;	
 			}
		}

      	return sites;
 	}
	
	/**
		Count the number of regions.

		@return the number of regions.
	*/
	public int countRegions() {return(analyzeZones(0));}

	/**
		Count the number of cultural zones. A cultural zone is a set of contiguous sites,
		each of which has a neighbour with a "compatible" culture.

		@return the number of zones.
	*/
	public int countZones() {return(analyzeZones(threshold));}
	
	/**
	Count the number of sites with j features. 

	@return the number of ones, twos, threes, ....
*/
	public int countSpread(int i) {return(spreadCount(i));}
	
	public int countDisagreements() {return(analyzeDisagreements(threshold));}
	
	public double socialPopularity(int i) {return(analyzeSocialPopularity(i));}
	
	public float aveZonesSize() {return(analyzeAveZonesSize(threshold));}
	
	public float aveRegionSize() {return(analyzeAveZonesSize(0));}

  	private int visit( DeffuantAgent agent, int criticalDist) {
  		//Gives the size of the region/zone surrounding an agent.
        int regionSize = 0;
   		DeffuantAgent active, neighbour;

		readyQueue.add(agent);	 // Add node to ready queue.
        while(!readyQueue.isEmpty()) {	// while ready queue not empty...
            // Get node from end of ready queue.
       		active = (DeffuantAgent) readyQueue.lastElement();
           	readyQueue.removeElementAt(readyQueue.size()-1);

        	// Add to ready queue the neighbours who are legal, not done, and 0 dist.
    		myNeighborhood = space.getVonNeumannNeighbors(active.x, active.y, false);	// If space not torus, can return nulls?

        	for(int i = 0; i < myNeighborhood.size(); i++ ) {
   				neighbour = (DeffuantAgent) myNeighborhood.get(i);
                if( neighbour.done ) continue;
                if(active.distance(neighbour) <= criticalDist )	//crit distance = 0 for region, bitmax-1 for zone}
                	{
                  	++regionSize;
                  	neighbour.done = true;
					readyQueue.add(neighbour);
                  	}
       		}
      	}
        if(regionSize == 0) regionSize = 1;	// Needed for 1 x 1 regions which have no valid neighbours.
        return regionSize;
	}
  	
 	private int visitD( DeffuantAgent agent, int criticalDist) {
 		//Gives the number of neighbours a specified agent disagrees with.
        int disagreements = 0;
   		DeffuantAgent neighbour;

    		myNeighborhood = space.getVonNeumannNeighbors(agent.x, agent.y, false);	// If space not torus, can return nulls?

        	for(int i = 0; i < myNeighborhood.size(); i++ ) {
   				neighbour = (DeffuantAgent) myNeighborhood.get(i);
                if( neighbour.done ) continue;
                if(agent.distance(neighbour) > criticalDist )	
                	{
                  	++disagreements;
                  	}
       		}
        return disagreements;
	}

}

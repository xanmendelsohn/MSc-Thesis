package vecDeffuant;

import java.util.*;

import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.analysis.*;
import uchicago.src.sim.engine.*;
import uchicago.src.sim.space.*;
import uchicago.src.sim.util.Random;

// This class does the same as DeffuantModel without loading the GUI graphical simulation and terminates the simulation after 100000000 ticks. Used for Monte Carlo simulations.
public class DeffuantBatchModel extends SimModelImpl {

	private ArrayList<DeffuantAgent> agentList = new ArrayList<DeffuantAgent>();
	private OpenSequenceGraph graph;	
	private int regionCount, zoneCount, disagreementCount;	
	private float aveRegionSize, aveZoneSize;
  	private RegionCounter regionCounter;	
	private DataRecorder recorder; 	
	private Schedule schedule;
	private Grid space; 
	private long start;	

	/** The number of features possessed by each agent.	*/
	protected int featureCount = 2;
	
	protected int threshold =1;
	
	protected double p=0.5;
	
	protected double dissociating = 1;

	/** Height of territory.	*/
	protected int gridHeight = 100;

	/** Width of territory.	*/
	protected int gridWidth = 100;

	/**
		Mutation rate for cultural drift. Mean of Poisson distribution from which random number
		of mutations is generated.
	*/
	protected double mutationProbability = 0.0;

	/** Size of neighbourhood. */
	protected int neighbourhoodExtent = 1;

	/** Type of neighbourhood. */
	protected int neighbourhoodType = Grid.VON_NEUMANN;

	/** If true, the territory "wraps around" so that no agent is on an edge. */
	protected boolean torus = true;

	/** The number of traits possessed by each feature.	*/
	protected int traitCount = 2;	


	/** Number of ticks between output of data to Output window.	*/
	protected int outputInterval = 1000;

	public int getFeatureCount() { return featureCount; }
	public void setFeatureCount(int newFeatureCount) {featureCount = Math.max(1,newFeatureCount);}
	public int getGridWidth() { return gridWidth; }
	public void setGridWidth(int newGridWidth) { gridWidth = newGridWidth; }
	public int getGridHeight() { return gridHeight; }
	public void setGridHeight(int newGridHeight) { gridHeight = newGridHeight; }
	public double getMutationProbability() { return mutationProbability; }
	public void setMutationProbability(double newMutationProbability) {
		mutationProbability = newMutationProbability;
		if(mutationProbability < 0) mutationProbability = 0;
		else if(mutationProbability > 1) mutationProbability = 1;
	}
	public int getNeighbourhoodExtent() {return neighbourhoodExtent;}
	public void setNeighbourhoodExtent(int neighbourhoodExtent) {this.neighbourhoodExtent = neighbourhoodExtent;}
	public int getNeighbourhoodType() {return neighbourhoodType;}
	public void setNeighbourhoodType(int neighbourhoodType) {this.neighbourhoodType = neighbourhoodType;}
	public int getOutputInterval() { return outputInterval; }
	public void setOutputInterval(int newOutputInterval) { outputInterval = newOutputInterval; }
	public int getTraitCount() { return traitCount; }
	public void setTraitCount(int newTraitCount) { traitCount = newTraitCount; }
	public int getThreshold() { return threshold; }
	public void setThreshold(int newThreshold) { threshold = newThreshold; }
	public double isDissociating() { return dissociating; }
	public double getDissociating() { return dissociating; }
	public void setDissociating(double newDissociating){
		dissociating = newDissociating;
		if(dissociating < 0) dissociating = 1;
		else if(dissociating > 2) dissociating = 2;
	}
	public double getP() { return p; }
	public void setP(double newP) {
		p = newP;
		if(p < 0) p = 0;
		else if(p > 1) p = 1;
	}
	public boolean isTorus() { return torus; }
	public void setTorus(boolean b) { torus = b; }

	
	public void begin() {
		buildModel();
		buildSchedule();
		start = System.currentTimeMillis();	// for timing.
	}

	
	protected void buildModel() {
		int i;

		BaseController controller = (BaseController) this.getController();
		long seed = controller.getRandomSeed();
		this.setRngSeed(seed);	

		Random.createUniform();
		space = new Grid(gridWidth, gridHeight, torus, neighbourhoodType, neighbourhoodExtent);
		AgentColour siteColour = new AgentColour(featureCount, traitCount);

		int[] randomTraits = new int[featureCount];
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {

				for (i = 0; i < featureCount; i++) {
					randomTraits[i] = Random.uniform.nextIntFromTo(0, traitCount-1);	
				}

				DeffuantAgent agent = new DeffuantAgent(x, y, space, featureCount, traitCount, randomTraits, /*negate,*/ siteColour, threshold, p, dissociating);
				agentList.add(agent);
				space.putObjectAt(x, y, agent);
			}
		}

		regionCounter = new RegionCounter(featureCount, agentList, space, threshold, dissociating);	
		initDataRecorder();
	}

	private void buildSchedule() {
		schedule.scheduleActionBeginning(0, new Interaction() );	

		if( mutationProbability > 0.0 )
			schedule.scheduleActionBeginning(0, new Mutation() );

		CountAction countAction = new CountAction();
		OutputAction outputAction = new OutputAction();
		StopAction stopAction = new StopAction();

		ActionGroup actionGroup = new ActionGroup();
		actionGroup.addAction(countAction);
		actionGroup.addAction(outputAction);
		actionGroup.addAction(stopAction);
		schedule.scheduleActionAt( 1, actionGroup, 1 );
		schedule.scheduleActionAtInterval( outputInterval, actionGroup );
		schedule.scheduleActionAtEnd(recorder, "record");
		schedule.scheduleActionAtEnd(recorder, "writeToFile");
	}

	public String[] getInitParam() {
		
		String[] params = { "gridWidth", "gridHeight", "torus", "neighbourhoodType", "neighbourhoodExtent",
			"featureCount","threshold", "traitCount", "mutationProbability","p", "dissociating",
			"displayInterval", "outputInterval", "loadGui" };
		return params;
	}

	public String getName() { return "vec. Deffuant Batch model"; }

	public int getRegionCount() { return regionCount; }

	public Schedule getSchedule() { return schedule; }

	
	public String getTraits() {
		int n = agentList.size();
		DeffuantAgent agent;
		StringBuffer sb = new StringBuffer(13+n*(1+featureCount*2));	
		sb.append("\nTrait values");
		for(int i = 0; i < n; i++ ) {
			agent = (DeffuantAgent) agentList.get(i);
			sb.append("\n"+agent.traitsToString());
		}
		return sb.toString();
	}

	public int getZoneCount() { return zoneCount; }
	
	public float getZoneSize() { return aveZoneSize; }
	
	public float getRegionSize() { return aveRegionSize; }
	
	public int getDisagreementCount() {return disagreementCount;}
	
	/** Writes simulated data to file.	*/
	protected void initDataRecorder() {
		String header = "vec. Deffuant batch model\nRandom seed: "+getRngSeed();
		recorder = new DataRecorder("./models/vec. DeffuantBatch.txt", this, header );
		recorder.createNumericDataSource(" ", this, "getRegionCount", -1, -1);
		recorder.createNumericDataSource(" ", this, "getZoneCount", -1, -1);
		recorder.createNumericDataSource(" ", this, "getRegionSize", -1, -1);
		recorder.createNumericDataSource(" ", this, "getZoneSize", -1, -1);
		recorder.createNumericDataSource(" ", this, "getDisagreementCount", -1, -1);
	}

	public void setup() {
	
		schedule = null;

		System.gc();

		schedule = new Schedule(1);
		agentList = new ArrayList<DeffuantAgent>();
		space = null;
		recorder = null;
	}

	public static void main(String[] args) {
		SimInit init = new SimInit();
		DeffuantBatchModel model = new DeffuantBatchModel();
		init.loadModel(model, null, false);
	}

	class CountAction extends BasicAction {
		public void execute() {
			regionCount = regionCounter.countRegions();
			zoneCount = regionCounter.countZones();
			aveZoneSize = regionCounter.aveZonesSize();
			aveRegionSize = regionCounter.aveRegionSize();
			disagreementCount = regionCounter.countDisagreements();
			
			}
		}

	class Interaction extends BasicAction {
		public void execute() {
		boolean event;
		int bitCount=1;
			do{
			int i = Random.uniform.nextIntFromTo(0, agentList.size()-1);	// Colt method.
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			
			if(++bitCount < 1000) event =  agent.step(agent);
    		else event = true;
			
			}while(event = false);
		}
	}

	class Mutation extends BasicAction {
		public void execute() {
			if( Random.uniform.nextDoubleFromTo(0, 1) <= mutationProbability ) {
				DeffuantAgent agent = (DeffuantAgent) agentList.get(Random.uniform.nextIntFromTo(0, agentList.size()-1));
				agent.mutate();
			}
 		}
	}

	class OutputAction extends BasicAction {
		public void execute() {
			System.out.println((long)getTickCount()+" ticks: "+regionCount+" regions, "+zoneCount+" zones, "+aveZoneSize+" aveZoneSize, "+aveRegionSize+" aveRegionSize, "+disagreementCount+" disagreements ");
		}
	}

	/*
		Stops the simulation when the model converges to stable regions (zones)
		or 100000000 ticks is reached.
		The final number of regions/zones is output.
	*/
	class StopAction extends BasicAction {
		public void execute() {
			if(regionCount == zoneCount) {
				System.out.println((long)getTickCount()+" ticks: "+regionCount+" regions, "+zoneCount+" zones ");
				long stop = System.currentTimeMillis();
				System.out.println("Converged: elapsed time = "+(stop-start)/1000+" secs");
				stop();
				}
			else if((long)getTickCount()== 1000000000) {
				System.out.println((long)getTickCount()+" ticks: "+regionCount+" regions, "+zoneCount+" zones ");
				System.out.println("Reached 1000000000 ticks");
				stop();
				}
		}
	}
 }

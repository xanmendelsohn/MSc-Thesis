
package vecDeffuant;


import java.awt.Color;
import java.util.*;

import uchicago.src.reflector.ListPropertyDescriptor;
import uchicago.src.sim.analysis.*;
import uchicago.src.sim.engine.*;
import uchicago.src.sim.gui.*;
import uchicago.src.sim.space.*;
import uchicago.src.sim.util.Random;

// This class defines the overall evolution of the system and the output data.

public class DeffuantModel extends SimModelImpl {

	private ArrayList<DeffuantAgent> agentList = new ArrayList<DeffuantAgent>();
	private DisplaySurface dsurf;	// Handles the drawing of the grid and creation of movies.
	private OpenSequenceGraph graph; // A graph that plots numbers of regions and zones.
	private int regionCount, zoneCount, disagreementCount;	// Current number of regions & zones and disagreements(i.e. blocked edges).
	private float aveRegionSize, aveZoneSize; // Current average size of regions and zones.
  	private RegionCounter regionCounter;	// Object that counts regions/zones and their size.
	private DataRecorder recorder; 	// Use a DataRecorder object to record any relevant data - writes to file.
	private Schedule schedule;
	private Grid space; // The agents operate in a grid space defined by this object.
	private long start;	// For calculating elapsed time.

	// Model parameters and their default values
	// -----------------------------------------
	
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
		Mutation rate for cultural drift, i.e. random mutation of agents. Mean of Poisson distribution from which random number
		of mutations is generated.
	*/
	protected double mutationProbability = 0.0;

	/** Size of neighbourhood. */
	protected int neighbourhoodExtent = 1;

	/** Type of neighbourhood. The Von Neumann neighbourhood corresponds to the integer lattice graph */
	protected int neighbourhoodType = Grid.VON_NEUMANN;

	/** If true, the territory "wraps around" so that no agent is on an edge. */
	protected boolean torus = true;

	/** The number of traits possessed by each feature.	*/
	protected int traitCount = 2;

	// Simulation control parameters and their default values
	// ------------------------------------------------------
	/** Number of ticks between updates of display.	*/
	protected int displayInterval = 1000;

	/** If true, load GUI elements. */
	protected boolean loadGui = true;

	/** Number of ticks between output of data to Output window.	*/
	protected int outputInterval = 1000;

	public DeffuantModel() {
		Hashtable<Integer, String> h1 = new Hashtable<Integer, String>();
  		h1.put(new Integer(Grid.VON_NEUMANN), "Von Neumann");
  		h1.put(new Integer(Grid.MOORE), "Moore");
    	h1.put(new Integer(Grid.GLOBAL_UNIFORM), "Global uniform");
		ListPropertyDescriptor pd = new ListPropertyDescriptor("NeighbourhoodType", h1);
  		descriptors.put("NeighbourhoodType", pd);
		addSimEventListener(new PauseListener());
	}

	// get/set methods allowing for graphical and batch manipulation of the model & simulation parameters.
	// --------------------------------------------------------------------------------------------------
	public int getDisplayInterval() { return displayInterval; }
	public void setDisplayInterval(int newDisplayInterval) { displayInterval = newDisplayInterval; }
	public int getFeatureCount() { return featureCount; }
	public void setFeatureCount(int newFeatureCount) {featureCount = Math.max(1,newFeatureCount);}
	public int getGridWidth() { return gridWidth; }
	public void setGridWidth(int newGridWidth) { gridWidth = newGridWidth; }
	public int getGridHeight() { return gridHeight; }
	public void setGridHeight(int newGridHeight) { gridHeight = newGridHeight; }
	public boolean isLoadGui() {return loadGui;}
	public void setLoadGui(boolean b) {this.loadGui = b;}
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
	public int getThreshold() { return threshold; }
	public void setThreshold(int newThreshold) { threshold = newThreshold; }
	public double getP() { return p; }
	public void setP(double newP) {
		p = newP;
		if(p < 0) p = 0;
		else if(p > 1) p = 1;
	}
	public boolean isTorus() { return torus; }
	public void setTorus(boolean b) { torus = b; }
	public double getDissociating() { return dissociating; }
	public void setDissociating(double newDissociating){
		dissociating = newDissociating;
		if(dissociating < 0) dissociating = 1;
		else if(dissociating > 2) dissociating = 2;
	}

	/**
		Begins a simulation run. All initialization, building the model, display, etc. takes 
		place here. This method is called whenever the Start button (or the Step button if the run
		has not yet begun) is clicked.
		If running in batch mode this is called to kick off a new simulation run.
	*/
	public void begin() {
		buildModel();
		if (loadGui) {
			buildDisplay();
			graph.display();
		}
		buildSchedule();
		if (dsurf != null && loadGui)
			dsurf.display();
		start = System.currentTimeMillis();	// for timing.
	}

	// Builds the display
	private void buildDisplay() {
		Object2DDisplay agentDisplay = new Object2DDisplay(space);
		agentDisplay.setObjectList(agentList);

		dsurf.addDisplayableProbeable(agentDisplay, "Agents");
		dsurf.setBackground(java.awt.Color.white);
		addSimEventListener(dsurf);

		// Set up graph.
		graph.addSequence("Regions", new Sequence() {
  			public double getSValue() { return regionCount; }},
  			Color.red, OpenSequenceGraph.FILLED_CIRCLE);
		graph.addSequence("Zones", new Sequence() {
  			public double getSValue() { return zoneCount; }},
  			Color.blue, OpenSequenceGraph.CIRCLE);
		graph.setAxisTitles("Time", "Counts");
		graph.setXRange(0, 50000);
		graph.setYRange(0, agentList.size());
		graph.setSize(600, 400);
	}

	/**
		Builds the model. Called from begin().
	*/
	protected void buildModel() {
		int i;

		// Get the displayed random seed value from the RePast Parameters panel and use it to initialize the random number generator.
		BaseController controller = (BaseController) this.getController();
		long seed = controller.getRandomSeed();
		this.setRngSeed(seed);	// same effect as Random.setSeed(seed).

		Random.createUniform();	// Creates Colt object: static cern.jet.random.Uniform uniform.
		space = new Grid(gridWidth, gridHeight, torus, neighbourhoodType, neighbourhoodExtent);
		AgentColour siteColour = new AgentColour(featureCount, traitCount);	// Create object for colouring the agents.

		int[] randomTraits = new int[featureCount];
		for (int x = 0; x < gridWidth; x++) {
			for (int y = 0; y < gridHeight; y++) {

				// Create random trait values for the agent.
				for (i = 0; i < featureCount; i++) {
					//randomTraits[i] =0;
					randomTraits[i] = Random.uniform.nextIntFromTo(0, traitCount-1);	// Colt method.
				}

				// Create the agent and add it to the space.
				DeffuantAgent agent = new DeffuantAgent(x, y, space, featureCount, traitCount, randomTraits, /*negate,*/ siteColour, threshold, p, dissociating);
				agentList.add(agent);
				space.putObjectAt(x, y, agent);
			}
		}

		regionCounter = new RegionCounter(featureCount, agentList, space, threshold, dissociating);	// Create the object that counts the regions and zones.

		initDataRecorder();
	}

	// Builds the simulation schedule. Called from begin().
	private void buildSchedule() {
		schedule.scheduleActionBeginning(0, new Interaction() );	// Core interaction of the vec. Deffuant model is executed at every tick.

		if( mutationProbability > 0.0 )
			schedule.scheduleActionBeginning(0, new Mutation() );

		CountAction countAction = new CountAction();
		OutputAction outputAction = new OutputAction();
		StopAction stopAction = new StopAction();

		// Create ActionGroup to ensure that regions/zones are counted before outputted.
		ActionGroup actionGroup = new ActionGroup();
		actionGroup.addAction(countAction);
		if(loadGui) {
			actionGroup.addAction(outputAction);
			actionGroup.addAction(new BasicAction() {
				public void execute() {graph.step();}} );
		}
		actionGroup.addAction(stopAction);
		schedule.scheduleActionAt( 1, actionGroup, 1 );
		schedule.scheduleActionAtInterval( outputInterval, actionGroup );
		schedule.scheduleActionAtEnd(recorder, "record");
		schedule.scheduleActionAtEnd(recorder, "writeToFile");
	}

	public String[] getInitParam() {
		// Note order of strings determines non-alpha order of parameters in Repast model settings window.
		String[] params = { "gridWidth", "gridHeight", "torus", "neighbourhoodType", "neighbourhoodExtent",
			"featureCount","threshold", "traitCount", "mutationProbability","p", "dissociating",
			"displayInterval", "outputInterval", "loadGui" };
		return params;
	}

	public String getName() { return "vec. Deffuant model"; }

	public int getRegionCount() { return regionCount; }

	public Schedule getSchedule() { return schedule; }

	/**
		Returns all configurations of all agents in a single string.
	*/
	public String getTraits() {
		int n = agentList.size();
		DeffuantAgent agent;
		StringBuffer sb = new StringBuffer(13+n*(1+featureCount*2));	// Intitial Size??
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
		String header = "vec. Deffuant model\nRandom seed: "+getRngSeed();
		recorder = new DataRecorder("./models/vec. Deffuant.txt", this, header );
		recorder.createNumericDataSource(" ", this, "getRegionCount", -1, -1);
		recorder.createNumericDataSource(" ", this, "getZoneCount", -1, -1);
		recorder.createNumericDataSource(" ", this, "getRegionSize", -1, -1);
		recorder.createNumericDataSource(" ", this, "getZoneSize", -1, -1);
		recorder.createNumericDataSource(" ", this, "getDisagreementCount", -1, -1);
	}

	/**
		Called whenever the Setup Model button (2 arrows) is clicked.
		Also called when the model is first loaded.
	*/
	public void setup() {
		if (dsurf != null) {dsurf.dispose();}

		dsurf = null;
		schedule = null;
		if (graph != null) graph.dispose();
		graph = null;

		System.gc();

		String displayTitle = "vec. Deffuant Model Display";
		String plotTitle = "vec. Deffuant Time Series Plot";
		if (loadGui) {
			dsurf = new DisplaySurface(this, displayTitle);
			this.registerDisplaySurface(displayTitle, dsurf);
			graph = new OpenSequenceGraph(plotTitle, this);
			this.registerMediaProducer(plotTitle, graph); // ??
		}
		schedule = new Schedule(1);
		agentList = new ArrayList<DeffuantAgent>();
		space = null;
		recorder = null;
	}

	public static void main(String[] args) {
		SimInit init = new SimInit();
		DeffuantModel model = new DeffuantModel();
		init.loadModel(model, null, false);
	}

	// Counts the number of regions and zones.
	class CountAction extends BasicAction {
		public void execute() { // Count the number of regions and zones.
			regionCount = regionCounter.countRegions();
			zoneCount = regionCounter.countZones();
			aveZoneSize = regionCounter.aveZonesSize();
			aveRegionSize = regionCounter.aveRegionSize();
			disagreementCount = regionCounter.countDisagreements();
			}
		}


	/*
		This implements the core interaction of the vec. Deffuant model.
		An agent is picked at random and its step() method is called.
		The step() method picks a neighbouring agent at random and initiates interaction
		according to the vec. Deffuant Model.
		A total of 10000 interaction is attempted until the configuration of a site
		changes (successful interaction).
		This action is executed at every tick of the simulation.
	*/
	class Interaction extends BasicAction {
		public void execute() {
		boolean event= false;
		int bitCount=1;
			do{
			int i = Random.uniform.nextIntFromTo(0, agentList.size()-1);	// Colt method.
			DeffuantAgent agent = (DeffuantAgent) agentList.get(i);
			
			if(++bitCount < 10000) event = agent.step(agent);
    		else event = true;
			
			}while(event = false);

			// Update grid screen display as necessary.
			if(dsurf != null && loadGui ) {
				if( displayInterval > 1 ) {
					if((DeffuantModel.this.getTickCount() % displayInterval) == 0) dsurf.updateDisplay();
				}
				else if(event) dsurf.updateDisplay();
			}
		}
	}

	/*
		This implements random mutation (cultural drift) in the vec. Deffuant model.
	*/
	class Mutation extends BasicAction {
		public void execute() {
			if( Random.uniform.nextDoubleFromTo(0, 1) <= mutationProbability ) {
				// Pick an agent at random and mutate it.
				DeffuantAgent agent = (DeffuantAgent) agentList.get(Random.uniform.nextIntFromTo(0, agentList.size()-1));
				agent.mutate();
			}
 		}
	}

	// Outputs the number of ticks, regions and zones to the Output window.
	class OutputAction extends BasicAction {
		public void execute() {
			System.out.println((long)getTickCount()+" ticks: "+regionCount+" regions, "+zoneCount+" zones, "+aveZoneSize+" aveZoneSize, "+aveRegionSize+" aveRegionSize, "+disagreementCount+" disagreements ");
		}
	}

	// Updates the agent display when user clicks on pause or stop button.
	class PauseListener implements SimEventListener {
		public void simEventPerformed(SimEvent evt) {
			if( evt.getId() == SimEvent.PAUSE_EVENT || evt.getId() == SimEvent.STOP_EVENT)
				if (dsurf != null && loadGui)
					dsurf.updateDisplay();
		}
	}

	/*
		Stops the simulation when the model converges to stable regions (zones).
		The final number of regions/zones is output.
		The elapsed time is calculated and output.
	*/
	class StopAction extends BasicAction {
		public void execute() {
			if(regionCount < zoneCount) {
				System.out.println((long)getTickCount()+" ticks: "+regionCount+" regions, "+zoneCount+" zones ");
				long stop = System.currentTimeMillis();
				System.out.println("Converged: elapsed time = "+(stop-start)/1000+" secs");
				stop();
				}
		}
	}
 }

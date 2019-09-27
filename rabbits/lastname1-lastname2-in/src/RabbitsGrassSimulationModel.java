import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.engine.SimInit;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {		
	// Default values
	private static final int NUM_RABBITS_INIT = 100;
	private static final int GRID_SIZE = 20;
	private static final int TOTAL_GRASS = 1000;
	private static final int RABBIT_ENERGY_INIT = 50;
		
	private int numRabbits = NUM_RABBITS_INIT;
	private int gridSize = GRID_SIZE;
	private int grass = TOTAL_GRASS;
	private int rabbitEnergyInit = RABBIT_ENERGY_INIT;
	
	private Schedule schedule;
	private RabbitsGrassSimulationSpace rgsSpace;
	private ArrayList rabbitsList;
	private DisplaySurface displaySurf;
	
	public static void main(String[] args) {
		
		System.out.println("Rabbit skeleton");

		SimInit init = new SimInit();
		RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
		// Do "not" modify the following lines of parsing arguments
		if (args.length == 0) // by default, you don't use parameter file nor batch mode 
			init.loadModel(model, "", false);
		else
			init.loadModel(model, args[0], Boolean.parseBoolean(args[1]));
		
	}
	
	public void setup() {
		System.out.println("Running setup");
		rgsSpace = null;
		rabbitsList = new ArrayList();
		// we want to run the object in time steps with interval 1
		schedule = new Schedule(1);
		
		if (displaySurf != null) {
			displaySurf.dispose();
		}
		displaySurf = null;
		displaySurf = new DisplaySurface(this, "Rabbit Grass Simulation Model, Window 1");
		
		registerDisplaySurface("Rabbit Grass Simulation Model, Window 1", displaySurf);
	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();
		
		displaySurf.display();
		
	}
	
	public void buildModel() {
		System.out.println("Running BuildModel");
		rgsSpace = new RabbitsGrassSimulationSpace(gridSize);
		rgsSpace.spreadGrass(grass);
		
		for (int i = 0; i < numRabbits; i++) {
			addNewRabbit();
		}
		
		// Reporting
		for (int i = 0; i < rabbitsList.size(); i++) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
			rgsa.report();
		}
	}
	
	public void buildSchedule() {
		System.out.println("Running BuildSchedule");
		
		// Add step action in schedule
		class RabbitGrassSimulationStep extends BasicAction {
			public void execute() {
				SimUtilities.shuffle(rabbitsList);
				for (int i = 0; i < rabbitsList.size(); i++) {
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
					rgsa.step();
				}
				
				int deadRabbits = reapDeadRabbits();
				for (int i = 0; i < deadRabbits; i++) {
					addNewRabbit();
				}
				
				displaySurf.updateDisplay();
			}
		}
		schedule.scheduleActionBeginning(0, new RabbitGrassSimulationStep());
		
		// Add count living rabbits action in schedule 
		class RabbitGrassSimulationCountLiving extends BasicAction {
			public void execute() {
				countLivingRabbits();
			}
		}
		schedule.scheduleActionAtInterval(10, new RabbitGrassSimulationCountLiving());
		
	}
	
	public void buildDisplay() {
		System.out.println("Running BuildDisplay");
		
		ColorMap map = new ColorMap();

	    for(int i = 1; i<16; i++){
	    	map.mapColor(i, new Color((int)(i * 8 + 127), 0, 0));
	    }
	    map.mapColor(0, Color.white);

	    // Grass
	    Value2DDisplay displayGrass =
	        new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);
	    // Rabbit
	    Object2DDisplay displayRabbits = 
	    	new Object2DDisplay(rgsSpace.getCurrentGrassSpace());
	    
	    // Grass
	    displaySurf.addDisplayable(displayGrass, "Grass");
	    // Agents
	    displaySurf.addDisplayable(displayRabbits, "Rabbits");

	}
	
	private void addNewRabbit() {
		RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(rabbitEnergyInit);
		rabbitsList.add(r);
		rgsSpace.addRabbit(r);
	}
	
	private int reapDeadRabbits(){
		int count = 0;
		
		for(int i = (rabbitsList.size() - 1); i >= 0 ; i--) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
		    
			if (rgsa.getEnergy() < 1){
		        rgsSpace.removeRabbitAt(rgsa.getX(), rgsa.getY());
		        rabbitsList.remove(i);
		        count++;
		    }
		}
		return count;
	}

	public String[] getInitParam() {
		// TODO Auto-generated method stub
		// Parameters to be set by users via the Repast UI slider bar
		// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
		String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold"};
		return params;
	}
	
	private int countLivingRabbits() {
	    int livingRabbits = 0;
	    for(int i = 0; i < rabbitsList.size(); i++){
	      RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
	      if(rgsa.getEnergy() > 0) livingRabbits++;
	    }
	    System.out.println("Number of living rabbits is: " + livingRabbits);

	    return livingRabbits;
	}

	public String getName() {
		return "Rabbits Grass Simulation Model";
	}

	public Schedule getSchedule() {
		return schedule;
	}
	
    public int getNumRabbits(){
	    return numRabbits;
	}

	public void setNumRabbits(int nr){
		numRabbits = nr;
	}
	
    public int getGridSize(){
	    return gridSize;
	}

	public void setGridSize(int gs){
	    gridSize = gs;
	}


	public int getGrass() {
		return grass;
	}

	public void setGrass(int i) {
		grass = i;
	}

	public int getRabbitEnergyInit() {
		return rabbitEnergyInit;
	}

	public void setRabbitEnergyInit(int i) {
		rabbitEnergyInit = i;
	}
		
}

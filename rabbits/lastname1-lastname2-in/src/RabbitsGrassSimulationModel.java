import java.awt.Color;
import java.util.ArrayList;

import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
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
	private static final int RABBIT_ENERGY_INIT = 10;
	private static final int BIRTH_THRESHOLD = 20;
	private static final int GRASS_GROWTH_RATE = 100;

		
	private int numInitRabbits = NUM_RABBITS_INIT;
	private int gridSize = GRID_SIZE;
	private int numInitGrass = TOTAL_GRASS;
	private int rabbitEnergyInit = RABBIT_ENERGY_INIT;
	private int birthThreshold = BIRTH_THRESHOLD;
	private int grassGrowthRate = GRASS_GROWTH_RATE;
	
	private Schedule schedule;
	private RabbitsGrassSimulationSpace rgsSpace;
	private ArrayList<RabbitsGrassSimulationAgent> rabbitsList;
	private DisplaySurface displaySurf;
	private OpenSequenceGraph plots;
	
	class GrassInSpace implements DataSource, Sequence {
		@Override
		public Object execute() {
			return new Double(getSValue());
		}
		
		@Override
		public double getSValue() {
			return (double) rgsSpace.getTotalGrass(); 
		}
	}
	
	class RabbitsInSpace implements DataSource, Sequence {
		@Override
		public Object execute() {
			return new Double(getSValue());
		}
		
		@Override
		public double getSValue() {
			return (double) rabbitsList.size(); 
		}
	}

	
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
		rgsSpace = null;
		rabbitsList = new ArrayList<RabbitsGrassSimulationAgent>();
		// We want to run the object in time steps with interval 1
		schedule = new Schedule(1);
		
		// Tear down Displays
		if (displaySurf != null) {
			displaySurf.dispose();
		}
		displaySurf = null;
		
		// Tear down Plots
		if (plots != null){
			plots.dispose();
	    }
	    plots = null;
		
		// Create Displays
		displaySurf = new DisplaySurface(this, "Rabbit Grass Simulation Model, Window 1");
		plots = new OpenSequenceGraph("Plots", this);
		
		// Register Displays
		registerDisplaySurface("Rabbit Grass Simulation Model, Window 1", displaySurf);
		this.registerMediaProducer("Plot", plots);
	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();
		
		displaySurf.display();
		plots.display();
	}
	
	public void buildModel() {
		// Initializing the space
		rgsSpace = new RabbitsGrassSimulationSpace(gridSize);
		
		// Spreading the initial number of grass over the initialized space
		rgsSpace.spreadGrass(numInitGrass);
		
		// Spreading the initial number of rabbits over the initialized space
		for (int i = 0; i < numInitRabbits; i++) {
			addNewRabbit();
		}
		
		// Reporting
		for (int i = 0; i < rabbitsList.size(); i++) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
			rgsa.report();
		}
	}
	
	public void buildSchedule() {
		// Add step action in schedule
		class RabbitGrassSimulationStep extends BasicAction {
			public void execute() {
				SimUtilities.shuffle(rabbitsList);
				for (int i = 0; i < rabbitsList.size(); i++) {
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
					// Report with every step as well
					rgsa.report();
					// Make a step in the simulation
					rgsa.step();
				}
				
				// Remove dead rabbits
				reapDeadRabbits();
				// Reproduce the rabbits
				reproduceRabbits();
				// Spread new grass over the space
				rgsSpace.spreadGrass(grassGrowthRate);
				
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
		schedule.scheduleActionAtInterval(5.0, new RabbitGrassSimulationCountLiving());
		
		// Add new actions
		class RabbitsGrassSimulationUpdateGrassInSpace extends BasicAction {
			public void execute() {
				plots.step();
		    }
		}
		schedule.scheduleActionAtInterval(10.0, new RabbitsGrassSimulationUpdateGrassInSpace());
	}
	
	public void buildDisplay() {
		ColorMap map = new ColorMap();

		// TODO: check the number 16
		for(int i = 1; i < 16; i++) {
			// Shades of green color (R, G, B) = (0, num, 0)
	    	map.mapColor(i, new Color(0, (int)(i * 8 + 127), 0));
	    }
		// White color is when there is no grass in the cell (background color)
	    map.mapColor(0, Color.white);

	    // Grass
	    Value2DDisplay displayGrass =
	        new Value2DDisplay(rgsSpace.getCurrentGrassSpace(), map);
	    // Rabbit
	    Object2DDisplay displayRabbits = 
	    	new Object2DDisplay(rgsSpace.getCurrentRabbitSpace());
	    
	    // Grass (allows user to click on display surface and get the info)
	    displaySurf.addDisplayableProbeable(displayGrass, "Grass");
	    // Agents (allows user to click on display surface and get the info)
	    displaySurf.addDisplayableProbeable(displayRabbits, "Rabbits");

	    // Add grass in space plot
	    plots.addSequence("Grass In Space", new GrassInSpace());
	    // Add rabbits in space plot
	    plots.addSequence("Rabbits In Space", new RabbitsInSpace());
	}
	
	private void addNewRabbit() {
		RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(rabbitEnergyInit);
		rabbitsList.add(r);
		rgsSpace.addRabbit(r);
	}
	
	private void reapDeadRabbits(){
		for(int i = (rabbitsList.size() - 1); i >= 0 ; i--) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
		    
			if (rgsa.getEnergy() < 1){
		        rgsSpace.removeRabbitAt(rgsa.getX(), rgsa.getY());
		        rabbitsList.remove(i);
		    }
		}
	}
	
	private void reproduceRabbits() {
		for (int i = (rabbitsList.size() - 1); i >= 0; i--) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
			
			if (rgsa.getEnergy() > birthThreshold) {
				rgsa.setEnergy(rgsa.getEnergy() - rabbitEnergyInit);
				addNewRabbit();
			}
		}
	}

	public String[] getInitParam() {
		// Parameters to be set by users via the Repast UI slider bar
		// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
		String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold"};
		return params;
	}
	
	private int countLivingRabbits() {
	    int livingRabbits = 0;
	    for(int i = 0; i < rabbitsList.size(); i++) {
	    	RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
	    	if (rgsa.getEnergy() > 0) livingRabbits++;
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
	
    public int getNumInitRabbits(){
	    return numInitRabbits;
	}

	public void setNumInitRabbits(int nr){
		numInitRabbits = nr;
	}
	
    public int getGridSize(){
	    return gridSize;
	}

	public void setGridSize(int gs){
	    gridSize = gs;
	}


	public int getNumInitGrass() {
		return numInitGrass;
	}

	public void setNumInitGrass(int i) {
		numInitGrass = i;
	}

	public int getRabbitEnergyInit() {
		return rabbitEnergyInit;
	}

	public void setRabbitEnergyInit(int i) {
		rabbitEnergyInit = i;
	}
	
	public int getBirthThreshold() {
		return birthThreshold;
	}

	public void setBirthThreshold(int bt) {
		birthThreshold = bt;
	}

	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}

	public void setGrassGrowthRate(int ggr) {
		grassGrowthRate = ggr;
	}

}

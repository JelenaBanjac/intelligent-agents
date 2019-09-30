import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


import uchicago.src.reflector.RangePropertyDescriptor;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.event.SliderListener;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.SimUtilities;
import uchicago.src.sim.engine.SimInit;

import javax.imageio.ImageIO;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * The model's dynamics are straightforward: a space
 * is populated with rabbits. Grass is distributed on
 * the landscape; rabbits move and eat the grass.
 * There is no collisions between two rabbits.
 * Rabbits have limited lifespans based on the energy they have
 * so if the energy is 0, the rabbit is dead. Also, after certain
 * energy level, the new rabbit is born.
 * 
 * @author 
 * Swiss Federal Institute of Technology in Lausanne (EPFL), Switzerland
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
	private int rabbitReproductionEnergy = grassGrowthRate;
	
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


		// Load rabbit graphical image
		BufferedImage rabbitImg;
		try {
			rabbitImg = ImageIO.read(new File("img/rabbit.png"));
		} catch (IOException e) {
			rabbitImg = null;
		}
		RabbitsGrassSimulationAgent.setRabbitImg(rabbitImg);
	}
	
	/**
	 * Tear down any existing pieces of the model and
	 * prepare for a new run.
	 */	
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

		// Sliders in Parameters Tab
		addRangePropertyDescriptor("GridSize", 0, 100, 20);
		addRangePropertyDescriptor("NumInitRabbits", 0, 400, 100);
		addRangePropertyDescriptor("NumInitGrass", 0, 1000, 200);
		addRangePropertyDescriptor("BirthThreshold", 0, 1000, 200);
		addRangePropertyDescriptor("GrassGrowthRate", 0, 1000, 200);
		addRangePropertyDescriptor("RabbitEnergyInit", 0, 50, 10);
		
		// Create Displays
		displaySurf = new DisplaySurface(this, "Rabbit Grass Simulation Model, Window 1");
		plots = new OpenSequenceGraph("Plots", this);
		
		// Register Displays
		registerDisplaySurface("Rabbit Grass Simulation Model, Window 1", displaySurf);
		this.registerMediaProducer("Plot", plots);
		
	}

	/**
	 * Initialize the model by building the separate elements that make
	 * up the model
	 */
	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();
		
		displaySurf.display();
		plots.display();
	}
	
	/**
	 * Initialize the basic model by creating the space
	 * and populating it with grass and rabbits.
	 */
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
	
	/**
	 * Create the schedule object(s) that will be executed
	 * during the running of the model
	 */
	public void buildSchedule() {
		// Add step action in schedule
		class RabbitGrassSimulationStep extends BasicAction {
			public void execute() {
				SimUtilities.shuffle(rabbitsList);
				for (int i = 0; i < rabbitsList.size(); i++) {
					RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
					// Report with every step as well
					//rgsa.report();
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
				System.out.println("Number of total grass: " + rgsSpace.getTotalGrass());
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
	
	/**
	 * Build the display elements for this model.
	 */
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
	
	/**
	 * Add a new rabbit to this model's rabbit list and rabbit space
	 */
	private void addNewRabbit() {
		RabbitsGrassSimulationAgent r = new RabbitsGrassSimulationAgent(rabbitEnergyInit);
		rabbitsList.add(r);
		rgsSpace.addRabbit(r);
	}
	
	/**
	 * Remove all dead rabbits from the rabbit list and rabbit space
	 */
	private void reapDeadRabbits(){
		for(int i = (rabbitsList.size() - 1); i >= 0 ; i--) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
		    
			if (rgsa.getEnergy() < 1){
		        rgsSpace.removeRabbitAt(rgsa.getX(), rgsa.getY());
		        rabbitsList.remove(i);
		    }
		}
	}
	
	/**
	 * Reproduce new rabbits if any of the living ones reached the
	 * birth threshold in energy level
	 */
	private void reproduceRabbits() {
		for (int i = (rabbitsList.size() - 1); i >= 0; i--) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
			
			if (rgsa.getEnergy() > birthThreshold) {
				rgsa.setEnergy(rgsa.getEnergy() - rabbitReproductionEnergy);
				addNewRabbit();
			}
		}
	}
	
	/**
	 * Get a count of the living rabbits on the model's rabbit list.
	 * @return count of the living rabbits on the rabbit list
	 */
	private int countLivingRabbits() {
	    int livingRabbits = 0;
	    for(int i = 0; i < rabbitsList.size(); i++) {
	    	RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitsList.get(i);
	    	if (rgsa.getEnergy() > 0) livingRabbits++;
	    }
	    System.out.println("Number of living rabbits is: " + livingRabbits);

	    return livingRabbits;
	}

	/**
	 * Get a String that serves as the name of the model
	 * @return the name of the model
	 */
	public String getName() {
		return "Rabbits Grass Simulation Model";
	}

	/**
	 * Returns the Schedule object for this model; for use
	 * internally by RePast
	 * @return the Schedule object for this model
	 */
	public Schedule getSchedule() {
		return schedule;
	}
	
	/**
	 * Get the string array that lists the initialization parameters
	 * for this model
	 * @return a String array that includes the names of all variables
	 * that can be modified by the RePast user interface
	 */
	public String[] getInitParam() {
		// Parameters to be set by users via the Repast UI slider bar
		// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
		String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold",
							"RabbitEnergyInit"};
		return params;
	}
	
	/**
	 * Get the parameter indicating the number of rabbits in this model
	 * @return the number of rabbits in the model
	 */
	public int getNumInitRabbits() {
		return numInitRabbits;
	}

	/**
	 * Set the parameter indicating the initial number of rabbits for this
	 * model.
	 * @param na new value for initial number of rabbits
	 */
	public void setNumInitRabbits(int nr) {
		numInitRabbits = nr;
	}
	
	/**
	 * Get the size of the space in the model
	 * @return the size of the space object in the model
	 */
	public int getGridSize(){
		return gridSize;
	}

	/**
	 * Set the size of the space in the model
	 * @param gs the size of the space object in the model
	 */
	public void setGridSize(int gs){
	    gridSize = gs;
	}

	/**
	 * Get the value of the parameter initializing the total amount
	 * of grass in this model
	 * @return the initial value for the total amount of grass in the
	 * model
	 */
	public int getNumInitGrass() {
		return numInitGrass;
	}

	/**
	 * Set the new value for the total amount of grass to be used when
	 * initializing the simulation
	 * @param i the new value for the total amount of grass
	 */
	public void setNumInitGrass(int i) {
		numInitGrass = i;
	}

	/**
	 * Get the value of the parameter initializing the initial
	 * energy of the rabbit in this model
	 * @return the initial energy level of the rabbit in the
	 * model
	 */
	public int getRabbitEnergyInit() {
		return rabbitEnergyInit;
	}

	/**
	 * Set the new value for the initial energy of the rabbit to be used when
	 * initializing the simulation
	 * @param i the new value for the initial energy of the rabbit
	 */
	public void setRabbitEnergyInit(int i) {
		rabbitEnergyInit = i;
	}
	
	/**
	 * Get the value energy level at which rabbits reproduce
	 * @return the energy level at which rabbits reproduce
	 */
	public int getBirthThreshold() {
		return birthThreshold;
	}

	/**
	 * Set the new value for the energy at which rabbits reproduce
	 * @param i the new value for the energy at which rabbits reproduce
	 */
	public void setBirthThreshold(int bt) {
		birthThreshold = bt;
	}

	/**
	 * Get the value of grass growth rate
	 * @return the value of grass growth rate
	 */
	public int getGrassGrowthRate() {
		return grassGrowthRate;
	}

	/**
	 * Set the new value for the grass growth rate
	 * @param i the new value for the grass growth rate
	 */
	public void setGrassGrowthRate(int ggr) {
		grassGrowthRate = ggr;
	}

	/**
	 * Add slider for a parameter to GUI.
	 * @param s the name of the parameter
	 * @param i the lower end of the scale
	 * @param i1 the upper end of the scale
	 * @param i2 the step size
	 */
	public void addRangePropertyDescriptor(String s, int i, int i1, int i2) {
		RangePropertyDescriptor d = new RangePropertyDescriptor(s, i, i1, i2);
		descriptors.put(s, d);
	}
}

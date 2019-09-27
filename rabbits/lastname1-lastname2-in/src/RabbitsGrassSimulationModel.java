import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimModelImpl;
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
	private static final int NUM_INIT_RABBITS = 100;
	private static final int GRID_SIZE = 20;
		
	private int numInitRabbits = NUM_INIT_RABBITS;
	private int gridSize = GRID_SIZE;
	
	private Schedule schedule;
	
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
	}

	public void begin() {
		buildModel();
		buildSchedule();
		buildDisplay();
		
	}
	
	public void buildModel() {
		System.out.println("Running BuildModel");
	}
	
	public void buildSchedule() {
		System.out.println("Running BuildSchedule");
	}
	
	public void buildDisplay() {
		System.out.println("Running BuildDisplay");
	}

	public String[] getInitParam() {
		// TODO Auto-generated method stub
		// Parameters to be set by users via the Repast UI slider bar
		// Do "not" modify the parameters names provided in the skeleton code, you can add more if you want 
		String[] params = { "GridSize", "NumInitRabbits", "NumInitGrass", "GrassGrowthRate", "BirthThreshold"};
		return params;
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

	
		
}

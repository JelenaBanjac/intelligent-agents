import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 *
 * The rabbit agent includes basic internal variables, methods
 * for modifying a rabbit's position and direction,
 * ID variables, etc.
 *
 * @author
 * Swiss Federal Institute of Technology in Lausanne (EPFL), Switzerland
 */
public class RabbitsGrassSimulationAgent implements Drawable {
	// Rabbits coordinates
	private int x;
	private int y;
	// Rabbits on the move
	private int vX;
	private int vY;
	// private int grass;
	private int energy;
	// private int stepsToLive;
	private static int IDNumber = 0;
	private int ID;
	private RabbitsGrassSimulationSpace rgsSpace;
	
    /**
     * Constructor that takes the initial energy of the agent
     * @param energyInit Initial rabbit energy
     */
	public RabbitsGrassSimulationAgent(int energyInit) {
		x = -1;
		y = -1;
		energy = energyInit;
		setVxVy();
		// stepsToLive = (int)((Math.random() * (maxLifespan - minLifespan)) + minLifespan);
		IDNumber++;
		ID = IDNumber;
	}
	
    /**
     * Set a new X and Y position for the agent.
     * Note that this affects only the agent's internal
     * assessment of its own location, and thus should
     * be called only after the Space object has confirmed
     * that this location is acceptable.
     * @param newX
     * @param newY
     */
	public void setXY(int newX, int newY) {
		x = newX;
		y = newY;
	}
	
    /**
     * Set this agent's velocity in the X and Y direction
     * Actually chooses a new velocity randomly; velocity
     * will be one of the 8 possible variations where
     * X and Y are -1, 0, or 1 and Y but both are not zero 
     */
	public void setVxVy() {
		vX = 0;
		vY = 0;
		//while ((vX == 0) && (vY == 0) ) {
		while (!((vX == 0 && vY != 0) || (vX != 0 && vY == 0))) {
			vX = (int)Math.floor(Math.random() * 3) - 1;
		    vY = (int)Math.floor(Math.random() * 3) - 1;
		}
	}
	
    /**
     * Set this agent's pointer to the space object
     * in which it resides.
     * @param rgss The space object into which the agent is
     * being placed
     */
	public void setRabbitGrassSimulationSpace(RabbitsGrassSimulationSpace rgss) {
		// Agents learn their place
		rgsSpace = rgss;
	}
	
    /**
     * Get this agent's internal unique ID
     * @return a String representing the unique ID for this agent;
     * this will be in the form "Rabbit-101"
     */
	public String getID() {
		return "Rabbit-" + ID;
	}
	
    /**
     * Get the amount of energy held by this agent
     * @return the amount of energy this agent has
     */
	public int getEnergy() {
		return energy;
	}
	
	/**
	 * Set the amount of energy to the agent
	 * @param e New energy that will be given to the agent
	 */
	public void setEnergy(int e) {
		energy = e;
	}
	
	/**
	 * Prints a report on this agent's status variables to
	 * the System output
	 */
	public void report() {
		System.out.println(getID() + 
						   " at (" + 
						   x + ", " + y +
						   ") has " + 
						   getEnergy() + " energy to live.");
	}

	/**
	 * Get this agent's X position
	 * @return the agent's X position
	 */
	public int getX() {
		return x;
	}

	/**
	 * Get this agent's Y position
	 * @return the agent's Y position
	 */
	public int getY() {
		return y;
	}
	
	/**
	 * Draw this agent to the RePast graphics
	 * object. Depending on the energy level, the agent will be
	 * colored with different colors.
	 * @param G the graphics object to which this agent
	 * will be drawn
	 */
	public void draw(SimGraphics G) {
		if (energy > 10) {
			G.drawFastRoundRect(Color.blue);
		} else if ( (5 < energy) && (energy < 10) ) {
			G.drawFastRoundRect(Color.yellow);
		} else {
			G.drawFastRoundRect(Color.red);
		}
	}
	
	/**
	 * A basic 'step' for this agent- the actions it
	 * takes when it is the agent's 'turn' in the simulation
	 */
	public void step() {
		int newX = x + vX;
		int newY = y + vY;
		
		Object2DGrid grid = rgsSpace.getCurrentRabbitSpace();
		newX = (newX + grid.getSizeX()) % grid.getSizeX();
		newY = (newY + grid.getSizeY()) % grid.getSizeY();

	    if (tryMove(newX, newY)){
	    	// Every step of schedule, rabbit eats the grass if there is any
			energy += rgsSpace.eatGrassAt(x, y);
			// TODO: check
			//rgsSpace.takeGrassAt(x, y);
	    }
	    else
	    {
	    	// If there was a collision between 2 rabbits, we would implement it here
	    	// However, by the task specification, we don't have collisions
	        setVxVy();
	    }
	    
		// Every step of schedule, rabbit loses energy
		energy--;
		
	}
	
	/**
	 * Attempt a move to a new location.
	 * @param newX the intended destination's X coordinate
	 * @param newY the intended destination's Y coordinate
	 * @return true if the move was successfully completed,
	 * false otherwise
	 */
	private boolean tryMove(int newX, int newY){
		return rgsSpace.moveRabbitAt(x, y, newX, newY);
	}


}

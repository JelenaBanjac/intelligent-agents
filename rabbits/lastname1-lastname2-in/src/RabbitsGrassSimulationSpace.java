import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * 
 * It includes the functions for placing the rabbits into the space,
 * moving rabbits, removing rabbbits, etc.
 * 
 * @author 
 * Swiss Federal Institute of Technology in Lausanne (EPFL), Switzerland
 */

public class RabbitsGrassSimulationSpace {
	// Grass in Space
	private Object2DGrid grassSpace;
	// Rabbits in Space
	private Object2DGrid rabbitSpace;
	
	// Constraint of grass quantity in one cell
	public static final int MAX_GRASS_IN_CELL = 50;
	
	/**
	  * Constructor that takes as arguments to size
	  * of the space to be created
	  * @param size is the size of the square grid
	  */
	public RabbitsGrassSimulationSpace(int size) {
		grassSpace = new Object2DGrid(size, size);
		rabbitSpace = new Object2DGrid(size, size);
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
	}
	
	/**
	 * Randomly distributes an amount of grass around
	 * the landscape
	 * @param grass the (total) amount of grass to be distributed
	 */
	public void spreadGrass(int grass) {
		// Randomly place grass in grassSpace
		//int totalGrass = getTotalGrass();
		
		for(int i = 0; i < grass; i++) {

			// Choose coordinates
			int x = (int)(Math.random()*(grassSpace.getSizeX()));
			int y = (int)(Math.random()*(grassSpace.getSizeY()));
		
			// Get the value of the object at those coordinates
			int currentValue = Math.min(getGrassAt(x, y), MAX_GRASS_IN_CELL - 1); // getGrassAt(x, y); 
			// Replace the Integer object with another one with the new value
			grassSpace.putObjectAt(x, y, new Integer(currentValue + 1));
		}
	}
	
	/**
	 * Get the amount of grass currently stored at
	 * the cell location specified
	 * @param x X coordinate of the desired cell
	 * @param y Y coordinate of the desired cell
	 * @return amount of grass stored at cell X,Y
	 */
	public int getGrassAt(int x, int y) {
		int i;
		if (grassSpace.getObjectAt(x, y) != null) {
			i = ((Integer) grassSpace.getObjectAt(x, y)).intValue();
		} else {
			i = 0;
		}
		
		return i;
	}
	
	/**
	 * Get the 'grass space' object
	 * @return the Object2DGrid object in which grass is stored
	 */
	public Object2DGrid getCurrentGrassSpace() {
		return grassSpace;
	}
	
	/**
	 * Get the 'rabbit space' object
	 * @return the Object2DGrid object in which rabbits are stored
	 */
	public Object2DGrid getCurrentRabbitSpace() {
		return rabbitSpace;
	}
	
	/**
	 * Determine if a given cell is occupied
	 * @param x X coordinate of the desired cell
	 * @param y Y coordinate of the desired cell
	 * @return True if there is a rabbit at X,Y, false otherwise
	 */
	public boolean isCellOccupied(int x, int y) {
		boolean retVal = false;
		if (rabbitSpace.getObjectAt(x, y) != null) retVal = true;
		return retVal;
	}
	
	/**
	 * Add a rabbit to this space.
	 * Will place the rabbit in an unoccupied space.
	 * Note that this will attempt to place the rabbit
	 * randomly, making 10*XSize*YYSize tries before
	 * giving up.
	 * @param rabbit The rabbit to be placed
	 * @return True if the rabbit was successfully placed,
	 * false if not
	 */
	public boolean addRabbit(RabbitsGrassSimulationAgent rabbit) {
		boolean retVal = false;
		int count = 0;
		int countLimit = 10 * rabbitSpace.getSizeX() * rabbitSpace.getSizeY();
		
		while ( (retVal == false) && (count < countLimit) ) {
			int x = (int)(Math.random() * (rabbitSpace.getSizeX()));
			int y = (int)(Math.random() * (rabbitSpace.getSizeY()));
		    
			if (isCellOccupied(x,y) == false) {
		    	rabbitSpace.putObjectAt(x, y, rabbit);
		        rabbit.setXY(x,y);
		        rabbit.setRabbitGrassSimulationSpace(this);  // Rabbits learn their space
		        retVal = true;
		    }
		    count++;	
		}
	
		return retVal;
	}
	
	/**
	 * Removes the rabbit from the specified location.
	 * @param x the X coordinate of the cell from which the rabbit is to be removed
	 * @param y the Y coordinate of the cell from which the rabbit is to be removed
	 */
	public void removeRabbitAt(int x, int y) {
		// Remove dead rabbits
		rabbitSpace.putObjectAt(x, y, null);
	}
	
	/**
	 * Removes the grass found at the specified location
	 * @param x the X coordinate of the cell from which the grass is to be removed
	 * @param y the Y coordinate of the cell from which the grass is to be removed
	 * @return the amount of grass collected from the cell
	 */
	public int eatGrassAt(int x, int y) {
		// Rabbits eat grass
		int grass = getGrassAt(x, y);
		grassSpace.putObjectAt(x, y, new Integer(0));
		return grass;
	}
	
	/**
	 * Moves a rabbit from one location to another.
	 * Note that this will not fail if there is no
	 * rabbit at the original location; it will only
	 * fail if there is already a rabbit at the new location.
	 * (If there is no rabbit at the original location,
	 * all that happens is that nulls get moved around).
	 * @param x the X coordinate of the original location
	 * @param y the Y coordinate of the original location
	 * @param newX the X coordinate of the destination location
	 * @param newY the Y coordinate of the destination location
	 * @return true if the move was successful, false otherwise
	 */
	public boolean moveRabbitAt(int x, int y, int newX, int newY) {
		// Rabbits on the move
		boolean retVal = false;
		if (!isCellOccupied(newX, newY)) {
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitSpace.getObjectAt(x, y);
		    removeRabbitAt(x,y);
		    rgsa.setXY(newX, newY);
		    rabbitSpace.putObjectAt(newX, newY, rgsa);
		    retVal = true;
		}
		return retVal;
	}
	
	/**
	 * Return the total grass found in the landscape
	 * @return total grass found in the landscape
	 */
	public int getTotalGrass() {
		// Used for making plots
		int totalGrass = 0;
		for(int i = 0; i < grassSpace.getSizeX(); i++){
			for(int j = 0; j < grassSpace.getSizeY(); j++){
				totalGrass += getGrassAt(i, j);
		    }
		}
		return totalGrass;
	}
}

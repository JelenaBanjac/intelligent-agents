import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	// Grass in Space
	private Object2DGrid grassSpace;
	// Rabbits in Space
	private Object2DGrid rabbitSpace;
	
	// Max number of Grass in Space
	public static final int MAX_GRASS = 50;
	
	public RabbitsGrassSimulationSpace(int size) {
		grassSpace = new Object2DGrid(size, size);
		rabbitSpace = new Object2DGrid(size, size);
		
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				grassSpace.putObjectAt(i, j, new Integer(0));
			}
		}
	}
	
	public void spreadGrass(int grass) {
		// Randomly place grass in grassSpace
		for(int i = 0; i < grass; i++) {

			// Choose coordinates
			int x = (int)(Math.random()*(grassSpace.getSizeX()));
			int y = (int)(Math.random()*(grassSpace.getSizeY()));
		
			// Get the value of the object at those coordinates
			int currentValue = Math.min(getGrassAt(x, y), MAX_GRASS - 1); // getGrassAt(x, y);
			// Replace the Integer object with another one with the new value
			grassSpace.putObjectAt(x, y, new Integer(currentValue + 1));
		}
	}
	
	public int getGrassAt(int x, int y) {
		int i;
		if (grassSpace.getObjectAt(x, y) != null) {
			i = ((Integer) grassSpace.getObjectAt(x, y)).intValue();
		} else {
			i = 0;
		}
		
		return i;
	}
	
	public Object2DGrid getCurrentGrassSpace() {
		return grassSpace;
	}
	
	public Object2DGrid getCurrentRabbitSpace() {
		return rabbitSpace;
	}
	
	public boolean isCellOccupied(int x, int y) {
		boolean retVal = false;
		if (rabbitSpace.getObjectAt(x, y) != null) retVal = true;
		return retVal;
	}
	
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
	
	public void removeRabbitAt(int x, int y) {
		// Remove dead rabbits
		rabbitSpace.putObjectAt(x, y, null);
	}
	
	public int takeGrassAt(int x, int y) {
		// Rabbits eat grass
		int grass = getGrassAt(x, y);
		grassSpace.putObjectAt(x, y, new Integer(0));
		return grass;
	}
	
	public boolean moveRabbitAt(int x, int y, int newX, int newY) {
		// Rabbits on the move
		boolean retVal = false;
		if(!isCellOccupied(newX, newY)){
			RabbitsGrassSimulationAgent rgsa = (RabbitsGrassSimulationAgent) rabbitSpace.getObjectAt(x, y);
		    removeRabbitAt(x,y);
		    rgsa.setXY(newX, newY);
		    rabbitSpace.putObjectAt(newX, newY, rgsa);
		    retVal = true;
		}
		return retVal;
	}
	
	public int getTotalGrass() {
		// Used for making plots
		int totalGrass = 0;
		for(int i = 0; i < rabbitSpace.getSizeX(); i++){
			for(int j = 0; j < rabbitSpace.getSizeY(); j++){
				totalGrass += getGrassAt(i, j);
		    }
		}
		return totalGrass;
	}
}

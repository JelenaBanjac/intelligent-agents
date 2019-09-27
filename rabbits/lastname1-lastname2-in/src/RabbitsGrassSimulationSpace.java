import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
	
	private Object2DGrid grassSpace;
	// Agents in Space
	private Object2DGrid rabbitSpace;
	
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
			int currentValue = getGrassAt(x, y);
			// Replace the Integer object with another one with the new value
			grassSpace.putObjectAt(x, y, new Integer(currentValue + 1));
		}
	}
	
	public int getGrassAt(int x, int y) {
		int i;
		if (grassSpace.getObjectAt(x, y) != null) {
			i = ((Integer)grassSpace.getObjectAt(x, y)).intValue();
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
			int x = (int)(Math.random()*(rabbitSpace.getSizeX()));
			int y = (int)(Math.random()*(rabbitSpace.getSizeY()));
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
}

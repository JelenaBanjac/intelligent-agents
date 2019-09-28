import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 *
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
	
	public RabbitsGrassSimulationAgent(int energyInit) {
		x = -1;
		y = -1;
		energy = energyInit;
		setVxVy();
		// stepsToLive = (int)((Math.random() * (maxLifespan - minLifespan)) + minLifespan);
		IDNumber++;
		ID = IDNumber;
	}
	
	
	
	public void setXY(int newX, int newY) {
		x = newX;
		y = newY;
	}
	
	public void setVxVy() {
		vX = 0;
		vY = 0;
		//while ((vX == 0) && (vY == 0) ) {
		while (!((vX == 0 && vY != 0) || (vX != 0 && vY == 0))) {
			vX = (int)Math.floor(Math.random() * 3) - 1;
		    vY = (int)Math.floor(Math.random() * 3) - 1;
		}
	}
	
	public void setRabbitGrassSimulationSpace(RabbitsGrassSimulationSpace rgss) {
		// Agents learn their place
		rgsSpace = rgss;
	}
	
	public String getID() {
		return "Rabbit-" + ID;
	}
	
	public int getEnergy() {
		return energy;
	}
	
	public void setEnergy(int e) {
		energy = e;
	}
	
	public void report() {
		System.out.println(getID() + 
						   " at " + 
						   x + ", " + y +
						   " has " + 
						   getEnergy() + " energy to live.");
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public void draw(SimGraphics G) {
//		if (energy > 10) {
//			G.drawFastRoundRect(Color.blue);
//		} 
//		else if ( (5 < energy) && (energy < 10) )
//		{
//			G.drawFastRoundRect(Color.yellow);
//		} 
//		else 
//		{
//			G.drawFastRoundRect(Color.red);
//		}
		if (energy > 0) {
			G.drawOval(Color.blue);
		}

		
	}
	
	public void step() {
		int newX = x + vX;
		int newY = y + vY;
		
		Object2DGrid grid = rgsSpace.getCurrentRabbitSpace();
		newX = (newX + grid.getSizeX()) % grid.getSizeX();
		newY = (newY + grid.getSizeY()) % grid.getSizeY();

	    if (tryMove(newX, newY)){
	    	// Every step of schedule, rabbit eats the grass if there is any
			energy += rgsSpace.takeGrassAt(x, y);
			// TODO: check
			rgsSpace.takeGrassAt(x, y);
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
	
	private boolean tryMove(int newX, int newY){
		return rgsSpace.moveRabbitAt(x, y, newX, newY);
	}


}

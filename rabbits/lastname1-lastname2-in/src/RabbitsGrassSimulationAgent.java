import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;


/**
 * Class that implements the simulation agent for the rabbits grass simulation.
 *
 *
 * @author
 * Swiss Federal Institute of Technology in Lausanne (EPFL), Switzerland
 */

public class RabbitsGrassSimulationAgent implements Drawable {

	private int x;
	private int y;
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
		// stepsToLive = (int)((Math.random() * (maxLifespan - minLifespan)) + minLifespan);
		IDNumber++;
		ID = IDNumber;
	}
	
	
	
	public void setXY(int newX, int newY) {
		x = newX;
		y = newY;
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
		if (energy > 10) {
			G.drawFastRoundRect(Color.blue);
		} 
		else if ( (5 < energy) && (energy < 10) )
		{
			G.drawFastRoundRect(Color.yellow);
		} 
		else 
		{
			G.drawFastRoundRect(Color.red);
		}
		
	}
	
	public void step() {
		// Every step of schedule, rabbit loses energy
		energy--;
		// Every step of schedule, rabbit eats the grass if there is any
		energy += rgsSpace.takeGrassAt(x, y);
	}

}

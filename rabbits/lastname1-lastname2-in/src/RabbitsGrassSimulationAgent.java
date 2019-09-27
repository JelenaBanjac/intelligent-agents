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
	private int grass;
	private int energy;
	// private int stepsToLive;
	
	public RabbitsGrassSimulationAgent(int energyInit) {
		x = -1;
		y = -1;
		energy = energyInit;
		// stepsToLive = (int)((Math.random() * (maxLifespan - minLifespan)) + minLifespan);
		
	}
	
	public void draw(SimGraphics arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void setXY(int newX, int newY) {
		x = newX;
		y = newY;
	}

	public int getX() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getY() {
		// TODO Auto-generated method stub
		return 0;
	}

}

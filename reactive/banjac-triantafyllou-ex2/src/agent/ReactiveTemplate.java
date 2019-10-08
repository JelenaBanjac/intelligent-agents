package agent;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;


public class ReactiveTemplate implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;

	/**
	 * The setup method is called exactly once, before the simulation starts and
	 * before any other method is called. The agent argument can be used to
	 * access important properties of the agent.
	 * 
	 * @ topology
	 * @ td
	 * @ agent
	 */
	@Override
	public void setup(Topology topology, TaskDistribution td, Agent agent) {

		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		Double discount = agent.readProperty("discount-factor", 
											 Double.class,
											 0.95);

		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
	}

	
	/**
	 * This method is called every time the agent arrives in a new city and is not
	 * carrying a task. The agent can see at most one available task in the city and
	 * has to decide whether or not to accept the task. It is possible that there is
	 * no task in which case availableTask is null.
	 *	• If the agent decides to pick up the task, the platform will take over the
	 *	control of the vehicle and deliver the task on the shortest path. The
	 *	next time this method is called the vehicle will have dropped the task
	 *	at its destination.
	 *	
	 *  • If the agent decides to refuse the task, it chooses a neighboring city to
	 *	move to. A refused task disappears and will not be available the next
	 *	time the agent visits the city.
	 *	
	 * Note: If multiple tasks are available then the LogistPlatform randomly selects
	 * the task that is shown to the agent. If no task is available then the agent
	 * must return a move action.
	 * 
	 * @param vehicle - our reactive agent
	 * @param availableTask - task in the city, can be null
	 * 
	 * @return action - whether the task was picked up or it left it in the city
	 */
	@Override
	public Action act(Vehicle vehicle, Task availableTask) {
		
		Action action;

		if (availableTask == null || random.nextDouble() > pPickup) {
			City currentCity = vehicle.getCurrentCity();
			action = new Move(currentCity.randomNeighbor(random));
			System.out.println("Leave the task and MOVE to neighbor city");
		} else {
			// platform takes over the vehicle and delivers task in shortest path
			action = new Pickup(availableTask);
			System.out.println("PICKUP the task");
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+ numActions +
							   " actions is " + myAgent.getTotalProfit()+
							   " (average profit: " + (myAgent.getTotalProfit() / (double)numActions)+")");
		}
		
		// Write to CSV file for evaluation of the performance
		FileWriter csvWriter = null;
		try {
			//File csvFile = new File("data/performance.csv");
			csvWriter = new FileWriter("data/switzerland-ex2/performance1.csv", true);
			csvWriter.append(myAgent.name()+";"+
							numActions+";"+
							myAgent.getTotalProfit()+";"+
							(myAgent.getTotalProfit() / (double)numActions)+ ";"+
							(myAgent.getTotalReward() / (double)myAgent.getTotalDistance())+"\n");
			csvWriter.flush();
			csvWriter.close();
		} catch (IOException e) {
			try {
				csvWriter.flush();
				csvWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			e.printStackTrace();
		}
				
		numActions++;
		
		return action;
	}
}

package agent;

import java.util.HashMap;
import java.util.Random;

import action.RouteAction;
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
import state.RouteState;
import tables.Table;
import tables.TableProbability;
import tables.Tables;


public class Reactive implements ReactiveBehavior {

	private Random random;
	private double pPickup;
	private int numActions;
	private Agent myAgent;
	private Topology topology;
	private TaskDistribution td;
	private Double discount;
	private Double epsilon;
	
	private Table<RouteState, RouteAction, Double> Q; 
	private Table<RouteState, RouteAction, Double> V; 
	private Table<RouteState, RouteAction, Double> V0; 
	private Table<RouteState, RouteAction, Double> R; 
	private TableProbability<RouteState, RouteAction, RouteState, Double> T;
	
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
		System.out.println("Seting up...");
		// Reads the discount factor from the agents.xml file.
		// If the property is not present it defaults to 0.95
		this.discount = agent.readProperty("discount-factor", 
									  	   Double.class,
									  	   0.95);
		this.epsilon = 0.01;
		
		this.random = new Random();
		this.pPickup = discount;
		this.numActions = 0;
		this.myAgent = agent;
		
		this.topology = topology;
		this.td = td;
		
		RouteState.initializeStates(this.topology.cities());
		RouteAction.initializeActions(this.topology.cities());
		
		this.Q = Tables.initializeQ();

		this.V = Tables.initializeV();

		this.R = Tables.initializeR(this.myAgent, this.td);

		this.T = Tables.initializeT(this.td);
		
		System.out.println("Do RLA algorithm...");
		offlineReinforcementLearning();
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
		numActions++;
		
		return action;
	}
	
	public void offlineReinforcementLearning() {
		
		while (true) {
			V0 = V; 
			for (RouteState state : RouteState.getStates()) {
				for (RouteAction action : RouteAction.getActions()) {
					double value = R.getValue(state, action) + discountedSum(state, action);
					Q.put(state, action, value);
				}
				RouteAction bestAction = Q.getValues(state).entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
				Double bestValue = Q.getValues(state).get(bestAction);
				
				V.put(state, bestAction, bestValue);
			}
			if (goodEnough(V0, V)) break;
		}
	}
	
	public double discountedSum(RouteState state, RouteAction action) {
		
		double sum = 0.0;
		for (RouteState state0 : RouteState.getStates()) {
			sum += T.getProbability(state, action, state0) * V.getValue(state0);
		}
		sum *= discount;
		return sum;
	}
	
	public HashMap<RouteAction, Double> maxQ(HashMap<RouteAction, Double> actionValues) {
		RouteAction action = actionValues.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
		Double value = actionValues.get(action);
		
		HashMap<RouteAction, Double> bestActionValue = new HashMap<RouteAction, Double>();
		bestActionValue.put(action, value);
		return bestActionValue;
	}
	
	public boolean goodEnough(Table<RouteState, RouteAction, Double> v0, Table<RouteState, RouteAction, Double> v) {
		double maxDiff = 0.0;
		for (RouteState state : v0.getKeys()) {
			double diff = Math.abs(v0.getValue(state) - v.getValue(state));
			if (diff > maxDiff) {
				maxDiff = diff;
			}
		}
		
		return maxDiff < 2*epsilon*discount/(1-discount);
	}
}

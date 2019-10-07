package agent;

import java.util.HashMap;
import java.util.Map;

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
import state.RouteState;
import tables.ActionValue;
import tables.Tables;


public class Reactive implements ReactiveBehavior {

	private int numActions;
	private Agent myAgent;
	private Topology topology;
	private TaskDistribution td;
	private Double discount;
	private Double epsilon;
	
	private Map<RouteState, HashMap<RouteAction, Double>> Q;
	private HashMap<RouteState, ActionValue<RouteAction>> V;
	private Map<RouteState, HashMap<RouteAction, Double>> R;

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
		this.epsilon = 0.001;
		this.numActions = 0;
		this.myAgent = agent;
		
		this.topology = topology;
		this.td = td;
		
		RouteState.initializeStates(this.topology.cities());
		RouteAction.initializeActions(this.topology.cities());
		
		this.Q = Tables.initializeQ();

		this.V = Tables.initializeV();

		this.R = Tables.initializeR(this.myAgent, this.td);

		System.out.println("Start RLA...");
		rla();
		
		printV();
	}
	
	private void printV() {
		System.out.println("V vector:");
		for (RouteState state : V.keySet()) {
			ActionValue<RouteAction> vdata = V.get(state);
			System.out.println(state + " -> " + vdata.action + " (" + vdata.value + ")");
		}
	}

	private RouteAction Best(RouteState state) {
		return V.get(state).action;
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

		RouteState state = RouteState.find(vehicle.getCurrentCity(), availableTask == null ? null : availableTask.deliveryCity);
		System.out.println("State: " + state);
		
		RouteAction bestAction = Best(state);
		System.out.println("Best action: " + bestAction);
		
		System.out.println(state.hasTask()? "has task": "no task");
		
		if (state.hasTask()) {
			action = new Pickup(availableTask);
		} else {
			
			action = new Move(bestAction.getNeighborCity());
		}
		
		if (numActions >= 1) {
			System.out.println("The total profit after "+ numActions +
							   " actions is " + myAgent.getTotalProfit()+
							   " (average profit: " + (myAgent.getTotalProfit() / (double)numActions)+")");
		}
		numActions++;
		
		return action;
	}
	
	public void rla() {
		int iter = 0;
		
		while (true) {
			iter++;
			
			HashMap<RouteState, ActionValue<RouteAction>> V0 = new HashMap<RouteState, ActionValue<RouteAction>> (V);

			for (RouteState state : Q.keySet()) {
				for (RouteAction action : Q.get(state).keySet()) {			
					double value = R.get(state).get(action) + discountedSum(state, action);
					Q.get(state).put(action, value);
				}
				RouteAction bestAction = Q.get(state).entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();
				Double bestValue = Q.get(state).get(bestAction);
				ActionValue<RouteAction> av = new ActionValue<RouteAction>(bestAction, bestValue);

				V.put(state, av);
			}
			
			if (goodEnough(V0, V)) break;
		}
		System.out.println("Number of iterations: " + iter);
	}

	
	public double discountedSum(RouteState state, RouteAction action) {
		double sum = 0.0;
		for (RouteState state0 : RouteState.getStates()) {
			sum += transition(state, action, state0);
		}
		sum = discount * sum;
		return sum;
	}
	
	public double transition(RouteState state, RouteAction action, RouteState state0) {
		double probability = 0.0;
		
		if (state.getFromCity()==state.getToCity() ||       //
				state.getFromCity()==state0.getFromCity() ||
				state0.getFromCity()==action.getNeighborCity() || //-
				state0.getToCity()!=action.getNeighborCity()||    //-
				state0.getFromCity()==state0.getToCity() ||     //
				state.getToCity() != state0.getFromCity()) {
				probability = 0.0;
		} else {
			if (state0.hasTask()) {
				probability = td.probability(state0.getFromCity(), state0.getToCity());  // delivery
			} else {
				probability = 1.0/topology.cities().size();
			}
		}
		
		return probability;
	}
	
	
	public boolean goodEnough(HashMap<RouteState, ActionValue<RouteAction>> v0, HashMap<RouteState, ActionValue<RouteAction>> v) {
		double maxDiff = 0.0;
		for (RouteState state : v0.keySet()) {
			double diff = Math.abs(v0.get(state).value - v.get(state).value);
			if (diff > maxDiff) {
				maxDiff = diff;
			}
		}
		
		return maxDiff < epsilon; //2*epsilon*discount/(1-discount);
	}
}

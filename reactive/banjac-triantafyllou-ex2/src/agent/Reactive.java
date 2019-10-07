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
	
	// Q-table
	private Map<RouteState, HashMap<RouteAction, Double>> Q;
	// Values of states
	private HashMap<RouteState, ActionValue<RouteAction>> V;
	// Reward table
	private Map<RouteState, HashMap<RouteAction, Double>> R;

	/**
	 * The setup method is called exactly once, before the simulation starts and
	 * before any other method is called. The agent argument can be used to
	 * access important properties of the agent.
	 * 
	 * @ topology - Map of the world from which we can get cities.
	 * @ td - Task distribution from where we get rewards and probabilities of tasks.
	 * @ agent - platform agent, in our case 1 agent -> 1 vehicle
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
	
	/**
	 * From the policy (strategy) table, it finds the best action to do 
	 * when the agent is in state `state`.
	 * 
	 * @param state - State for which we want to get the best action
	 * @return - action that is best to do in this state
	 */
	private RouteAction Best(RouteState state) {
		return V.get(state).action;
	}
	
	/**
	 * Performing a Reinforcement Learning Algorithm for construction of the
	 * policy (strategy) that will help us decide what is the best action to do
	 * in any state that we get into. The algorithm follows the structure of `value iteration`
	 * algorithm. First, we fill in the Q-table (column=state, row=action). 
	 * Then we find the max value of each column in Q-table and that max value corresponds to
	 * the best action that can be performed in that state. We loop until it is good enough. 
	 * Good enough in our case means that the difference between 2 successive iterations is
	 * smaller than some epsilon we defined above.
	 */
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

	
	/**
	 * Calculate discounted future. Discount is the gamma in the algorithm of `value iteration`.
	 * This transition is also the probability that we get to any state, say `state0` given
	 * that we are currently in state `state` and implement action `action`.
	 * 
	 * @param state - state from which the transition happens
	 * @param action - action we do in the state
	 * @return sum - discounted sum, i.e. discounted future effect on the value of state
	 */
	public double discountedSum(RouteState state, RouteAction action) {
		double sum = 0.0;
		for (RouteState state0 : RouteState.getStates()) {
			sum += transition(state, action, state0) * V.get(state0).value;
		}
		sum = discount * sum;
		return sum;
	}
	
	/**
	 * Transition from state `state` with action `action` to state `state0`.
	 * This transition is also represented as the probability to get into state `state0`
	 * given we are in the state `state` and perform action `action`.
	 * 	
	 * @param state - Transition from this state
	 * @param action - Action that initiates transition
	 * @param state0 - Transition to this state
	 * @return probability - Probability we get into state `state0` when in state `state` we
	 * perform the action `action`.
	 */
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
	
	
	/**
	 * Condition that says if the algorithm is good enough. It calculates the difference 
	 * between two successive iterations of Value of State vectors V.
	 * 
	 * @param v0 - Previous iteration value of Value of State vector.
	 * @param v - Current iteration value of Value of State vector.
	 * @return true - if it is good enough, false - if it is not yet good enough.
	 */
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

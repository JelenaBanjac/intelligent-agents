package tables;

import java.util.HashMap;
import java.util.Map;

import action.RouteAction;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import state.RouteState;

public class Tables {

	// Value of State
	private static HashMap<RouteState, ActionValue<RouteAction>> V = new HashMap<RouteState, ActionValue<RouteAction>>();
	// Reward table
	private static Map<RouteState, HashMap<RouteAction, Double>> R = new HashMap<RouteState, HashMap<RouteAction, Double>>();
	// Q-table
	private static Map<RouteState, HashMap<RouteAction, Double>> Q = new HashMap<RouteState, HashMap<RouteAction, Double>>();
	

	/**
	 * Reward table
	 * Represented as a map of maps. First level are states, second level are states and
	 * third level are values representing the reword the agent will get when performing an action
	 * in that state.
	 * @param agent
	 * @param td
	 * @return
	 */
	public static Map<RouteState, HashMap<RouteAction, Double>> initializeR(Agent agent, TaskDistribution td) {
		// TODO: check
		for (RouteState state : RouteState.getStates()) {
			R.put(state, new HashMap<RouteAction, Double>());
			
			for (RouteAction action : RouteAction.getActions()) {
				if (skip(state, action)) {
					continue;
				}
				
				Vehicle vehicle = agent.vehicles().iterator().next();  // get the first one for the Reactive Behavior
				double reward = 0.0;

				if (state.hasTask()) {	// delivery cost
					reward += td.reward(state.getToCity(), action.getNeighborCity());  // reward for taking this action being in state
					reward -= state.getToCity().distanceTo(action.getNeighborCity()) * vehicle.costPerKm();   // move after delivery cost
				} else {
					reward -= state.getFromCity().distanceTo(action.getNeighborCity()) * vehicle.costPerKm();
				}
				R.get(state).put(action, reward);
				
			}
		}
		return R;
	}
		
	/**
	 * Q-table
	 * Represented as a map of maps. First level are states, second level are actions and
	 * third level are values. Here it is just initialized with zero values.
	 * @return
	 */
	public static Map<RouteState, HashMap<RouteAction, Double>> initializeQ() {
		
		for (RouteState state : RouteState.getStates()) {
			Q.put(state, new HashMap<RouteAction, Double>());
			
			for (RouteAction action : RouteAction.getActions()) {
				
				if (skip(state, action)) {
					continue;
				}
				
				Q.get(state).put(action, 0.0);
			}
		}
		return Q;
	}
	
	/**
	 * Value of State vector
	 * Represented as a map of tuples (kindof). Here we just initialize it with zeros.
	 * @return
	 */
	public static HashMap<RouteState, ActionValue<RouteAction>> initializeV() {
		for (RouteState state : RouteState.getStates()) {
			ActionValue<RouteAction> av = new ActionValue<RouteAction>(null, 0.0);
			V.put(state, av);
		}
		return V;
	}
	
	private void printPolicy() {
		System.out.println("Policy - Strategy - V :");
		for (RouteState state : V.keySet()) {
			ActionValue<RouteAction> vdata = V.get(state);
			System.out.println(state + " -> " + vdata.action + " (" + vdata.value + ")");
		}
	}
	
	/**
	 * Some combinations of states and actions are not possible, so we discard them here.
	 * For example: 
	 * - the vehicle cannot stay in the same city when in state;
	 * - when vehicle is not delivering, it should move only to its neighbors (not any action possible).
	 * @param state
	 * @param action
	 * @return
	 */
	public static boolean skip(RouteState state, RouteAction action) {
		return (state.getFromCity() == state.getToCity() ||
			  ((!state.getFromCity().neighbors().contains(action.getNeighborCity())) && 
					  (!state.hasTask())));
	}
	
}


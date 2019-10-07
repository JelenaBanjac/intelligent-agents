package tables;

import java.util.HashMap;
import java.util.Map;

import action.RouteAction;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import state.RouteState;

public class Tables {

	private static HashMap<RouteState, ActionValue<RouteAction>> V = new HashMap<RouteState, ActionValue<RouteAction>>();
	private static Map<RouteState, HashMap<RouteAction, Double>> R = new HashMap<RouteState, HashMap<RouteAction, Double>>();
	private static Map<RouteState, HashMap<RouteAction, Double>> Q = new HashMap<RouteState, HashMap<RouteAction, Double>>();
	

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
	
	public static HashMap<RouteState, ActionValue<RouteAction>> initializeV() {
		for (RouteState state : RouteState.getStates()) {
			ActionValue<RouteAction> av = new ActionValue<RouteAction>(null, 0.0);
			V.put(state, av);
		}
		return V;
	}
	
	public static boolean skip(RouteState state, RouteAction action) {
		return (state.getFromCity() == state.getToCity() ||
			  ((!state.getFromCity().neighbors().contains(action.getNeighborCity())) && 
					  (!state.hasTask())));
	}
	
}


package tables;

import action.RouteAction;
import action.RouteAction.RouteActionType;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import state.RouteState;

public class Tables {

	// Q-table
	private static Table<RouteState, RouteAction, Double> Q = new Table<RouteState, RouteAction, Double>();
	// Reward table
	private static Table<RouteState, RouteAction, Double> R = new Table<RouteState, RouteAction, Double>();
	// Transitions table
	private static TableProbability<RouteState, RouteAction, RouteState, Double> T = new TableProbability<RouteState, RouteAction, RouteState, Double>();
	// V table
	private static Table<RouteState, RouteAction, Double> V = new Table<RouteState, RouteAction, Double>();
	
	public static Table<RouteState, RouteAction, Double> initializeR(Agent agent, TaskDistribution td) {
		// TODO: check
		for (RouteState state : RouteState.getStates()) {
			for (RouteAction action : RouteAction.getActions()) {
				Vehicle vehicle = agent.vehicles().iterator().next();  // get the first one for the Reactive Behavior
				double reward = 0.0;
				
				if (state.hasToCity() && action.hasNeighborCity()) {
					reward = - state.getFromCity().distanceTo(state.getToCity()) * vehicle.costPerKm() -
							state.getToCity().distanceTo(action.getNeighborCity()) * vehicle.costPerKm();
				} else if (!state.hasToCity() && action.hasNeighborCity()) {
					reward = - state.getFromCity().distanceTo(action.getNeighborCity()) * vehicle.costPerKm();
				} else if (state.hasToCity() && !action.hasNeighborCity()) {
					reward = td.reward(state.getFromCity(), state.getToCity()) - state.getFromCity().distanceTo(state.getToCity()) * vehicle.costPerKm();
				} else {
					reward = 0;  // TODO
				}
				R.put(state, action, reward);
				
			}
		}
		
		return R;
	}
	

	public static TableProbability<RouteState, RouteAction, RouteState, Double> initializeT(TaskDistribution td) {
		for (RouteState state : RouteState.getStates()) {
			for (RouteAction action : RouteAction.getActions()) {
				for (RouteState state0 : RouteState.getStates()) {
					double probability = 0.0;
					
					if (state.getToCity() != state0.getFromCity()) {
						probability = 0.0;
					} else {
						if (action.getRouteActionType() == RouteActionType.PICKUP_AND_DELIVER) {
							if (state0.hasToCity()) {
								probability = td.probability(state.getToCity(), state0.getToCity());
							} else {
								//TODO
							}
						} else {
							if (state.getToCity().neighbors().contains(state0.getToCity())) {
								probability = 1.0 / state.getToCity().neighbors().size();
							} else {
								probability = 0.0;
							}
						}
					}
					
					T.put(state, action, state0, probability);
				}
			}
		}
		return T;
	}
	

	public static Table<RouteState, RouteAction, Double> initializeQ() {
		for (RouteState state : RouteState.getStates()) {
			for (RouteAction action : RouteAction.getActions()) {
				Q.put(state, action, 0.0);
			}
		}
		return Q;
	}
	
	public static Table<RouteState, RouteAction, Double> initializeV() {
		for (RouteState state : RouteState.getStates()) {
			V.put(state, RouteAction.getActions().get(0), 0.0);
		}
		return V;
	}
	

	
	
	
}


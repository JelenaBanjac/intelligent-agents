package action;

import java.util.ArrayList;
import java.util.List;

import logist.topology.Topology.City;
import state.RouteState;

public class RouteAction {

//	public enum RouteActionType { 
//		REFUSE_AND_MOVE, 	 // we need to handle where the agent moves when it refused the task
//		PICKUP_AND_DELIVER   // platform handles the moving of agent in this case, it moves the shortest path, so we don't know the `toCity`
//	} 
	
	private City neighborCity;
	private static ArrayList<RouteAction> actions = new ArrayList<RouteAction>();
	
	
	public RouteAction(City neighborCity) {
		this.neighborCity = neighborCity;
	}
	
	public static void initializeActions(List<City> cities) {
		// ~N actions for each city
		for (City city : cities) {
			for (City neighborCity : city.neighbors()) {
				RouteAction.actions.add(new RouteAction(neighborCity));
			}
		}
//		RouteAction.actions.add(new RouteAction(null));
	}

	@Override
	public String toString() {
		return "RouteAction [neighborCity=" + neighborCity + "]";
	}

	public boolean isDelivery() {
		return neighborCity == null;
	}
	
	public City getNeighborCity() {
		return neighborCity;
	}

	public void setNeighborCity(City neighborCity) {
		this.neighborCity = neighborCity;
	}

	public static ArrayList<RouteAction> getActions() {
		return actions;
	}

	public static void setActions(ArrayList<RouteAction> actions) {
		RouteAction.actions = actions;
	}
	
	
}

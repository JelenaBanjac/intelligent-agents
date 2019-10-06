package action;

import java.util.ArrayList;
import java.util.List;

import logist.topology.Topology.City;
import state.RouteState;

public class RouteAction {

	public enum RouteActionType { 
		REFUSE_AND_MOVE, 	 // we need to handle where the agent moves when it refused the task
		PICKUP_AND_DELIVER   // platform handles the moving of agent in this case, it moves the shortest path, so we don't know the `toCity`
	} 
	
	private City neighborCity;
	private RouteActionType routeActionType;
	private static ArrayList<RouteAction> actions = new ArrayList<RouteAction>();
	
	
	public RouteAction(City neighborCity, RouteActionType routeActionType) {
		this.neighborCity = neighborCity;
		this.routeActionType = routeActionType;
	}
	
	public static void initializeActions(List<City> cities) {
		// ~N actions for each city
		for (City city : cities) {
			for (City neighborCity : city.neighbors()) {
				RouteAction.actions.add(new RouteAction(neighborCity, RouteActionType.REFUSE_AND_MOVE));
			}
		}
		RouteAction.actions.add(new RouteAction(null, RouteActionType.PICKUP_AND_DELIVER));
	}

	@Override
	public String toString() {
		return "RouteAction [neighborCity=" + neighborCity + ", routeActionType=" + routeActionType + "]";
	}

	public boolean hasNeighborCity() {
		return neighborCity != null;
	}
	
	public City getNeighborCity() {
		return neighborCity;
	}

	public void setNeighborCity(City neighborCity) {
		this.neighborCity = neighborCity;
	}

	public RouteActionType getRouteActionType() {
		return routeActionType;
	}

	public void setRouteActionType(RouteActionType routeActionType) {
		this.routeActionType = routeActionType;
	}

	public static ArrayList<RouteAction> getActions() {
		return actions;
	}

	public static void setActions(ArrayList<RouteAction> actions) {
		RouteAction.actions = actions;
	}
	
	
}

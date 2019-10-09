package action;

import java.util.ArrayList;
import java.util.List;
import logist.topology.Topology.City;


public class RouteAction {

	private City neighborCity;
	private static ArrayList<RouteAction> actions = new ArrayList<RouteAction>();
		
	public RouteAction(City neighborCity) {
		this.neighborCity = neighborCity;
	}
	
	/**
	 * Number of actions in our implementation is N, where N stands for the number of
	 * cities in the topology. We don't have city null. In any state, the agent can have the action
	 * that says which city it visits next.
	 * 
	 * ~N actions
	 * 
	 * @param cities
	 */
	public static void initializeActions(List<City> cities) {
		for (City city : cities) {
			for (City neighborCity : city.neighbors()) {
				RouteAction.actions.add(new RouteAction(neighborCity));
			}
		}
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

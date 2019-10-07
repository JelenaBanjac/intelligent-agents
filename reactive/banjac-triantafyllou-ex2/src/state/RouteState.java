package state;

import java.util.ArrayList;
import java.util.List;

import logist.topology.Topology.City;

public class RouteState {

	public static int staticId = 0;
	private int id;
	private City fromCity;
	private City toCity;
	private static ArrayList<RouteState> states = new ArrayList<RouteState>();
	
	public RouteState(City fc, City tc) {
		id = staticId++;
		fromCity = fc;
		toCity = tc;
	}

	/**
	 * In our interpretation of the world we use routes as states.
	 * Number of states is N^2, where N is the number of cities in the topology. 
	 * 
	 * ~N^2
	 * 
	 * @param cities
	 */
	public static void initializeStates(List<City> cities) {
		// N^2 states
		for (City fromCity : cities) {
			for (City toCity : cities) {
				if (fromCity == toCity) {
					toCity = null;
				}
				RouteState.states.add(new RouteState(fromCity, toCity));	
			}
		}
	}
	
	public static RouteState find(City fromCity, City toCity) {
		for (RouteState state : states) {
			if (state.getFromCity() == fromCity && state.getToCity() == toCity) {
				return state;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "RouteState [id=" + id + ", fromCity=" + fromCity + ", toCity=" + toCity + "]";
	}
	
	public boolean hasTask() {
		return toCity != null;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public City getFromCity() {
		return fromCity;
	}

	public void setFromCity(City fromCity) {
		this.fromCity = fromCity;
	}

	public City getToCity() {
		return toCity;
	}

	public void setToCity(City toCity) {
		this.toCity = toCity;
	}

	public static ArrayList<RouteState> getStates() {
		return states;
	}

	public static void setStates(ArrayList<RouteState> states) {
		RouteState.states = states;
	}
	
	
}

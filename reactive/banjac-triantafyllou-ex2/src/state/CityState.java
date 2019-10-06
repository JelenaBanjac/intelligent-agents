package state;

// import java.util.ArrayList;

import logist.topology.Topology.City;

public class CityState {

	public static int staticId = 0;
	private int id;
	private City city;
//	private static ArrayList<RouteState> states = new ArrayList<RouteState>();
	
	public CityState(City c) {
		id = staticId++;
		city = c;
	}

//	public static void initializeStates(ArrayList<City> cities) {
//		for (City fromCity : cities) {
//			for (City toCity : cities) {
//				if (fromCity == toCity) {
//					toCity = null;
//				}
//				RouteState.states.add(new RouteState(fromCity, toCity));	
//			}
//		}
//	}
	
//	public static RouteState find(City fromCity, City toCity) {
//		for (RouteState state : states) {
//			if (state.getFromCity() == fromCity && state.getToCity() == toCity) {
//				return state;
//			}
//		}
//		return null;
//	}
	
	@Override
	public String toString() {
		return "CityState [id=" + id + ", city=" + city + "]";
	}

	public int getId() {
		return id;
	}	

	public void setId(int id) {
		this.id = id;
	}

	public City getCity() {
		return city;
	}

	public void setCity(City city) {
		this.city = city;
	}

	
	
//	public static ArrayList<RouteState> getStates() {
//		return states;
//	}
//
//	public static void setStates(ArrayList<RouteState> states) {
//		RouteState.states = states;
//	}
	
	
}

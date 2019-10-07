package tables;

import action.RouteAction;

public class ActionValue<T> {
	
	public RouteAction action;
	public double value;
	
	public ActionValue(RouteAction routeAction, double d) {
		this.action = routeAction;
		this.value = d;
	}
}

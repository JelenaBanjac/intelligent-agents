package tables;

import action.RouteAction;

/**
 * We use this class to represent the tuples where
 * the first element is action and second the value.
 *
 * @param <T>
 */
public class ActionValue<T> {
	
	public RouteAction action;
	public double value;
	
	public ActionValue(RouteAction routeAction, double d) {
		this.action = routeAction;
		this.value = d;
	}
}

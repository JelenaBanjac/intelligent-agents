package state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;
import action.Action;



public class State implements Comparable<State> {
	public City vehiclePosition;
	public TaskSet tasksLeft;
	public TaskSet tasksCarried;
	public List<Action> actions;
	public double remainingCapacity;
	public double cost;
	private double heuristic = -1;
	
	public State() {
		this.tasksLeft = null;
		this.tasksCarried = null;
		this.vehiclePosition = null;
		this.actions = new ArrayList<Action>();
		this.cost = 0.0;
		this.remainingCapacity = 0.0;
	}
	
	public State(Vehicle vehicle, TaskSet tasks) {
		this.tasksLeft = tasks.clone();
		this.tasksCarried = vehicle.getCurrentTasks().clone();
		this.vehiclePosition = vehicle.getCurrentCity();
		this.actions = new ArrayList<Action>();
		this.cost = 0.0;
		this.remainingCapacity = vehicle.capacity();
	}
	
	private List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		for (Task task : tasksCarried) {
			actions.add(new Action(Action.Type.DELIVER, task));
			if (task.deliveryCity.equals(vehiclePosition)) {
				return Arrays.asList(new Action(Action.Type.DELIVER, task));
			}
		}
		for (Task task : tasksLeft) {
			if (task.weight <= remainingCapacity) {
				actions.add(new Action(Action.Type.PICKUP, task));
				if (task.pickupCity.equals(vehiclePosition)) {
					return Arrays.asList(new Action(Action.Type.PICKUP, task));
				}
			}
		}
		return actions;
	}
	
	public List<State> getSuccessors() {
		List<State> successorStates = new ArrayList<State>();
		
		for (Action action : getActions()) {
			TaskSet tasksLeft = this.tasksLeft.clone();
			TaskSet tasksCarried = this.tasksCarried.clone();
			List<Action> actions = new ArrayList<Action>(this.actions);
			State n = new State();
			actions.add(action);
			
			if (action.type == Action.Type.PICKUP) {	
				tasksLeft.remove(action.task);
				tasksCarried.add(action.task);
				n.vehiclePosition = action.task.pickupCity;
				n.remainingCapacity = this.remainingCapacity - action.task.weight;
			}
			else if (action.type == Action.Type.DELIVER) {
				tasksCarried.remove(action.task);
				n.vehiclePosition = action.task.deliveryCity;
				n.remainingCapacity = this.remainingCapacity + action.task.weight;
			}
			n.tasksLeft = tasksLeft;
			n.tasksCarried = tasksCarried;
			n.actions = actions;
			n.cost = this.cost + this.vehiclePosition.distanceTo(n.vehiclePosition);
			successorStates.add(n);
		}
		return successorStates;
	}
	
	
	public double getHeuristic() {
		if (heuristic != -1) {
			return cost + heuristic;
		}
		heuristic = 0;
		for (Task task : tasksLeft) {
			double cost = vehiclePosition.distanceTo(task.pickupCity) + task.pathLength();
			if (cost > heuristic) {
				heuristic = cost;
			}
		}
		for (Task task : tasksCarried) {
			double cost = vehiclePosition.distanceTo(task.deliveryCity);
			if (cost > heuristic) {
				heuristic = cost;
			}
		}
		return cost + heuristic;
	}
	
    public boolean isAlreadyDiscoveredAs(State other) {
        List<Boolean> checks = new ArrayList<>();
        // Is in the same city
        checks.add(this.vehiclePosition.equals(other.vehiclePosition));
        // Carrying the same tasks
        checks.add(this.tasksLeft.equals(other.tasksLeft));
        // Having completed the same deliveries
        checks.add(this.tasksCarried.equals(other.tasksCarried));
        // Having equal or greater cost
        checks.add(this.cost >= other.cost);

        for (Boolean c : checks) {
            if (!c) return false;
        }
        return true;
    }
	

	public boolean isFinal() {
		return tasksLeft.isEmpty() && tasksCarried.isEmpty();
	}
	
	@Override
	public String toString() {
		return this.vehiclePosition.name + " " + actions;
	}
	
	

	public City getVehiclePosition() {
		return vehiclePosition;
	}

	public void setVehiclePosition(City vehiclePosition) {
		this.vehiclePosition = vehiclePosition;
	}

	@Override
	public int compareTo(State s0) {
		return Double.compare(this.getHeuristic(), s0.getHeuristic());
	}
}

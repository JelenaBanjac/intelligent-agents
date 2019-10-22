package state;

//import logist.plan.Action;
//import logist.plan.Action.Move;
//import logist.plan.Action.Pickup;
//import logist.plan.Action.Delivery;
import action.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class State implements Comparable<State> {
    // The city in which the vehicle currently is
    private City currentLocation;

    // Tasks currently being carried (excl. tasks delivered in the city)
    private TaskSet tasksToDeliver;

    // Tasks left to pick up
    private TaskSet tasksAvailable;

    // Remaining capacity of the vehicle (after deliveries)
    private int remainingCapacity;

    // Cost of the path until now
    private double cost;

    // Actions to achieve the current state
    public List<Action> actions;

    // Future states
    private List<State> successors;
    
    // Calculating the heuristic
    private double heuristic = -1;

	public State() {
		this.tasksToDeliver = null;
		this.tasksAvailable = null;
		this.currentLocation = null;
		this.actions = new ArrayList<Action>();
		this.cost = 0.0;
		this.remainingCapacity = 0;
	}
    
    public State(Vehicle vehicle, TaskSet tasks, TaskSet carriedTasks) {
        this.currentLocation = vehicle.getCurrentCity();
        this.tasksToDeliver = carriedTasks.clone(); //new ArrayList<>(carriedTasks);
        this.tasksAvailable = tasks.clone(); //new ArrayList<>(tasks);
        this.remainingCapacity = vehicle.capacity();
        this.cost = 0;
        this.actions = new ArrayList<>();
    }

//    public State(City currentLocation, TaskSet tasksToDeliver, TaskSet tasksAvailable, int remainingCapacity,
//                 double cost, List<Action> actions) {
//        this.currentLocation = currentLocation;
//        this.tasksToDeliver = tasksToDeliver; //new ArrayList<>(tasksToDeliver);
//        this.tasksAvailable = tasksAvailable; //new ArrayList<>(tasksAvailable);
//        this.remainingCapacity = remainingCapacity;
//        this.cost = cost;
//        this.actions = actions;
//    }
    
    public double getHeuristic() {
		if (heuristic != -1) {
			return cost + heuristic;
		}
		heuristic = 0;
		for (Task task : tasksAvailable) {
			double cost = currentLocation.distanceTo(task.pickupCity) + task.pathLength();
			if (cost > heuristic) {
				heuristic = cost;
			}
		}
		for (Task task : tasksToDeliver) {
			double cost = currentLocation.distanceTo(task.deliveryCity);
			if (cost > heuristic) {
				heuristic = cost;
			}
		}
		return cost + heuristic;
	}

    public double getCost() {
        return this.cost;
    }

    public List<State> getSuccessors() {
        return this.successors;
    }

    
	private List<Action> getActions() {
		List<Action> actions = new ArrayList<Action>();
		for (Task task : tasksToDeliver) {
			actions.add(new Action(Action.Type.DELIVER, task));
			if (task.deliveryCity.equals(currentLocation)) {
				return Arrays.asList(new Action(Action.Type.DELIVER, task));
			}
		}
		for (Task task : tasksAvailable) {
			if (task.weight <= remainingCapacity) {
				actions.add(new Action(Action.Type.PICKUP, task));
				if (task.pickupCity.equals(currentLocation)) {
					return Arrays.asList(new Action(Action.Type.PICKUP, task));
				}
			}
		}
		return actions;
	}
	
	
	public void generateSuccessors() {
		List<State> successorStates = new ArrayList<State>();
		
		for (Action action : getActions()) {
			TaskSet tasksAvailable = this.tasksAvailable.clone();
			TaskSet tasksToDeliver = this.tasksToDeliver.clone();
			
			List<Action> actions = new ArrayList<Action>(this.actions);
			actions.add(action);
			
			State n = new State();
			if (action.type == Action.Type.PICKUP) {	
				tasksAvailable.remove(action.task);
				tasksToDeliver.add(action.task);
				
				n.currentLocation = action.task.pickupCity;
				n.remainingCapacity = this.remainingCapacity - action.task.weight;
				n.tasksAvailable = tasksAvailable;
				n.tasksToDeliver = tasksToDeliver;
				n.actions = actions;
				n.cost = this.cost + this.currentLocation.distanceTo(n.currentLocation);
			}
			else if (action.type == Action.Type.DELIVER) {
				tasksToDeliver.remove(action.task);
				
				n.currentLocation = action.task.deliveryCity;
				n.remainingCapacity = this.remainingCapacity + action.task.weight;
				n.tasksAvailable = tasksAvailable;
				n.tasksToDeliver = tasksToDeliver;
				n.actions = actions;
				n.cost = this.cost + this.currentLocation.distanceTo(n.currentLocation);
			}
			
			successorStates.add(n);
		}
		this.successors = successorStates;
	}

    public boolean isAlreadyDiscoveredAs(State other) {
        List<Boolean> checks = new ArrayList<>();
        // Is in the same city
        checks.add(this.currentLocation.equals(other.currentLocation));
        // Carrying the same tasks
        checks.add(this.tasksToDeliver.equals(other.tasksToDeliver));
        // Having completed the same deliveries
        checks.add(this.tasksAvailable.equals(other.tasksAvailable));
        // Having equal or greater cost
        checks.add(this.cost >= other.cost);

        for (Boolean c : checks) {
            if (!c) return false;
        }
        return true;
    }

    public void printState() {
        System.out.println("Vehicle at " + this.currentLocation);
        System.out.println("# tasks being carried: " + this.tasksToDeliver.size());
        System.out.println("# tasks left: " + this.tasksAvailable.size());
        System.out.println("remaining capacity: " + this.remainingCapacity);
        System.out.println("cost: " + this.cost);
        System.out.println();
    }

	public City getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(City currentLocation) {
		this.currentLocation = currentLocation;
	}
	
	public boolean isFinal() {
		return tasksAvailable.isEmpty() && tasksToDeliver.isEmpty();
	}

	@Override
	public int compareTo(State s0) {
		return Double.compare(this.getHeuristic(), s0.getHeuristic());
	}
    
    
}

package state;

import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.plan.Action.Delivery;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.ArrayList;
import java.util.List;

public class State {
    // The city in which the vehicle currently is
    private City currentLocation;

    // Tasks currently being carried (excl. tasks delivered in the city)
    private List<Task> tasksToDeliver;

    // Tasks left to pick up
    private List<Task> tasksAvailable;

    // Remaining capacity of the vehicle (after deliveries)
    private int remainingCapacity;

    // Cost of the path until now
    private double cost;

    // Actions to achieve the current state
    private List<Action> actions;

    // Future states
    private List<State> successors;

    public State(Vehicle vehicle, TaskSet tasks) {
        this.currentLocation = vehicle.homeCity();
        this.tasksToDeliver = new ArrayList<>();
        this.tasksAvailable = new ArrayList<>(tasks);
        this.remainingCapacity = vehicle.capacity();
        this.cost = 0;
        this.actions = new ArrayList<>();
    }

    public State(City currentLocation, List<Task> tasksToDeliver, List<Task> tasksAvailable, int remainingCapacity,
                 double cost) {
        this.currentLocation = currentLocation;
        this.tasksToDeliver = new ArrayList<>(tasksToDeliver);
        this.tasksAvailable = new ArrayList<>(tasksAvailable);
        this.remainingCapacity = remainingCapacity;
        this.cost = cost;
        this.actions = new ArrayList<>();
    }

    public double getCost() {
        return this.cost;
    }

    public List<State> getSuccessors() {
        return this.successors;
    }

    public void generateSuccessors() {
        List<State> successors = new ArrayList<>();

        // Option 1: If there are tasks to deliver
        for (Task dtask : this.tasksToDeliver) {
            State n = new State(dtask.deliveryCity, this.tasksToDeliver, this.tasksAvailable,
                    this.remainingCapacity + dtask.weight,
                    this.cost + this.currentLocation.distanceTo(dtask.deliveryCity)
            );
            n.tasksToDeliver.remove(dtask);
            n.actions.add(new Move(dtask.deliveryCity));
            n.actions.add(new Delivery(dtask));
            successors.add(n);
        }

        // Option 2: If there are tasks to pick up
        for (Task ptask : this.tasksAvailable) {
            if (ptask.weight <= this.remainingCapacity) {
                State n = new State(ptask.pickupCity, this.tasksToDeliver, this.tasksAvailable,
                        this.remainingCapacity - ptask.weight,
                        this.cost + this.currentLocation.distanceTo(ptask.pickupCity)
                );
                n.tasksToDeliver.add(ptask);
                n.tasksAvailable.remove(ptask);
                n.actions.add(new Move(ptask.pickupCity));
                n.actions.add(new Pickup(ptask));
                successors.add(n);
            }
        }

        // If both tasksToDeliver and tasksAvailable are empty, then this is a final state
        this.successors = successors;
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
}

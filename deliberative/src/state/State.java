package state;

import com.sun.org.apache.xpath.internal.operations.Bool;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {
    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    private City vehiclePosition;
    private Map<Task, City> taskPositions;
    private List<Task> tasksCarried;
    private List<Task>  tasksLeft;
    private int remainingCapacity;
    private double cost;
    private List<State> successors;

    public List<State> getSuccessors() {
        return successors;
    }

    public void setSuccessors(List<State> successors) {
        this.successors = successors;
    }

    public State(Vehicle vehicle, TaskSet tasks) {
        this.vehiclePosition = vehicle.homeCity();

        this.taskPositions = new HashMap<>();
        for (Task t : tasks)
            this.taskPositions.put(t, t.pickupCity);

        this.tasksCarried = new ArrayList<>();
        this.tasksLeft = new ArrayList<>(tasks);
        this.remainingCapacity = vehicle.capacity();
        this.cost = 0;
    }

    public State(City vehiclePosition, Map<Task, City> taskPositions,
                 List<Task> tasksCarried, List<Task> tasksLeft, int remainingCapacity,
                 double cost) {
        this.vehiclePosition = vehiclePosition;
        this.taskPositions = new HashMap<>(taskPositions);
        this.tasksCarried = new ArrayList<>(tasksCarried);
        this.tasksLeft = new ArrayList<>(tasksLeft);
        this.remainingCapacity = remainingCapacity;
        this.cost = cost;
    }

    public void printState() {
        System.out.println("Vehicle at " + this.vehiclePosition);
        System.out.println("Task positions: " + this.taskPositions.toString());
        System.out.println("# tasks carried: " + this.tasksCarried.size());
        System.out.println("# tasks left: " + this.tasksLeft.size());
        System.out.println("remaining capacity: " + this.remainingCapacity);
        System.out.println("cost: " + this.cost);
        System.out.println();
    }

    public void generateSuccessors() {
        List<State> successors = new ArrayList<>();

        for (City neighbor : this.vehiclePosition.neighbors()) {
            double cost = this.cost + this.vehiclePosition.distanceTo(neighbor);

            if (!(this.tasksCarried.isEmpty() && this.tasksLeft.isEmpty())) {
                // Generate successor where no task is picked up
                successors.add(
                        new State(neighbor, this.taskPositions, this.tasksCarried, this.tasksLeft,
                                this.remainingCapacity, cost)
                );

                // Generate successors where a task is picked up from the current city
                for (Task task : this.tasksLeft) {
                    if (task.pickupCity == this.vehiclePosition && task.weight <= this.remainingCapacity) {
                        State succ = new State(neighbor, this.taskPositions, this.tasksCarried,
                                this.tasksLeft, this.remainingCapacity, cost);
                        succ.tasksCarried.add(task);
                        succ.tasksLeft.remove(task);
                        succ.remainingCapacity -= task.weight;
                        successors.add(succ);
                    }
                }
            }
        }

        for (State succ : successors) {
            succ.updateTaskPositions();
        }

        this.successors = successors;
    }

    private void updateTaskPositions() {
        List<Task> tasksCarried = new ArrayList<>(this.tasksCarried);
        for (Task carried : tasksCarried) {
            // Update task position
            this.taskPositions.put(carried, this.vehiclePosition);

            // Check if task can be delivered
            if (carried.deliveryCity == this.vehiclePosition) {
                this.tasksCarried.remove(carried);
                this.remainingCapacity += carried.weight;
            }
        }
    }

    public boolean isAlreadyDiscoveredAs(State other) {
        List<Boolean> checks = new ArrayList<>();
        // Is in the same city
        checks.add(this.vehiclePosition.equals(other.vehiclePosition));
        // Carrying the same tasks
        checks.add(this.tasksCarried.equals(other.tasksCarried));
        // Having completed the same deliveries
        checks.add(this.tasksLeft.equals(other.tasksLeft));
        // Having equal or greater cost
        checks.add(this.cost >= other.cost);

        for (Boolean c : checks) {
            if (!c) return false;
        }
        return true;
    }
}

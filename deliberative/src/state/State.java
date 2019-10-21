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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class State {

    // ID of the state in the search space tree
    // private int levelId;
    // private int childId;

    // Position of vehicle and tasks
    private City vehiclePos;
    private Map<Task, City> taskPositions;

    // Actions carried out at the city
    private List<Action> actions;

    // Tasks being carried at the moment
    private List<Task> tasksCarried;

    // Tasks left
    private List<Task> tasksLeft;

    // How much weight the vehicle can still carry
    private int remainingCapacity;

    // Cost until now
    private double cost;

    // Path until now
    private List<State> previousStates;

    // Next states
    private List<State> successors;

    // Constructor for initial states
    public State(Vehicle vehicle, TaskSet tasks) {
        this.vehiclePos = vehicle.homeCity();
        this.actions = new ArrayList<>();

        Map<Task, City> taskPositions = new HashMap<>();
        for (Task t : tasks)
            taskPositions.put(t, t.pickupCity);
        this.taskPositions = taskPositions;

        this.tasksCarried = new ArrayList<>();
        this.tasksLeft = new ArrayList<>(tasks);
        this.remainingCapacity = vehicle.capacity();
        this.cost = 0;
        this.previousStates = new ArrayList<>();
        this.successors = new ArrayList<>();
    }

    // Copying constructor
    public State(City vehiclePos, List<Action> actions, Map<Task, City> taskPositions, List<Task> tasksCarried,
                 List<Task> tasksLeft, int remainingCapacity, double cost, List<State> previousStates) {
        this.vehiclePos = vehiclePos;
        this.actions = new ArrayList<>(actions);
        this.taskPositions = new HashMap<>(taskPositions);
        this.tasksCarried = new ArrayList<>(tasksCarried);
        this.tasksLeft = new ArrayList<>(tasksLeft);
        this.remainingCapacity = remainingCapacity;
        this.cost = cost;
        this.previousStates = new ArrayList<>(previousStates);
        this.successors = new ArrayList<>();
    }

    // Getters/setters
    public City getVehiclePos() {
        return vehiclePos;
    }

    public void setVehiclePos(City vehiclePos) {
        this.vehiclePos = vehiclePos;
    }

    public Map<Task, City> getTaskPositions() {
        return taskPositions;
    }

    public void setTaskPositions(Map<Task, City> taskPositions) {
        this.taskPositions = taskPositions;
    }

    public int getRemainingCapacity() {
        return remainingCapacity;
    }

    public void setRemainingCapacity(int remainingCapacity) {
        this.remainingCapacity = remainingCapacity;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public List<State> getPreviousStates() {
        return previousStates;
    }

    public void setPreviousStates(List<State> previousStates) {
        this.previousStates = previousStates;
    }

//    public int getLevelId() {
//        return levelId;
//    }
//
//    public void setLevelId(int levelId) {
//        this.levelId = levelId;
//    }
//
//    public int getChildId() {
//        return childId;
//    }
//
//    public void setChildId(int childId) {
//        this.childId = childId;
//    }

    public List<State> getSuccessors() {
        return this.successors;
    }

    public void setSuccessors(List<State> successors) {
        this.successors = successors;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public List<Task> getTasksCarried() {
        return tasksCarried;
    }

    public void setTasksCarried(List<Task> tasksCarried) {
        this.tasksCarried = tasksCarried;
    }

    public List<Task> getTasksLeft() {
        return tasksLeft;
    }

    public void setTasksLeft(List<Task> tasksLeft) {
        this.tasksLeft = tasksLeft;
    }

    public void generateSuccessors() {

        // New state attributes
        // int levelId;
        // int childId;

        List<State> successors = new ArrayList<>();
        List<Action> actions = new ArrayList<>();

        // Add current state as a previous state for successors
        List<State> previousStates = new ArrayList<>(this.previousStates);
        previousStates.add(this);

        // Generate candidate states for each neighboring city
        for (City neighbor : this.vehiclePos.neighbors()) {
            System.out.println("Generating states at " + neighbor.name + " with cost " + cost);
            double cost = this.cost + this.vehiclePos.distanceTo(neighbor); //TODO Check if we need cost per km
            actions.add(new Move(neighbor));

            // Deliver or update position of tasks carried at the moment
            int remainingCapacity = this.remainingCapacity;
            Map<Task, City> taskPositions = new HashMap<>(this.taskPositions);
            List<Task> tasksCarried = new ArrayList<>(this.tasksCarried);

            for (Task carried : this.tasksCarried) {
                // Update task position
                taskPositions.put(carried, neighbor);

                // Check if task can be delivered
                if (carried.deliveryCity == neighbor) {
                    tasksCarried.remove(carried);
                    remainingCapacity += carried.weight;
                    actions.add(new Delivery(carried));
                }
            }

            // If vehicle hasn't delivered all of its tasks, generate successors
            // Otherwise this is a goal state
            if (!tasksCarried.isEmpty() || !this.tasksLeft.isEmpty()) {
                // Generate successor state where the agent does not pick up any task
                successors.add(
                        new State(neighbor, actions, taskPositions, tasksCarried, this.tasksLeft, remainingCapacity,
                                cost, previousStates)
                );

                // Generate candidate states for tasks we can possibly pickup
                for (Task task : this.tasksLeft) {
                    if (task.pickupCity == neighbor && task.weight <= this.remainingCapacity) {
                        State newState = new State(neighbor, actions, taskPositions, tasksCarried, this.tasksLeft,
                                remainingCapacity, cost, previousStates);
                        newState.actions.add(new Pickup(task));
                        newState.taskPositions.put(task, neighbor);
                        newState.tasksCarried.add(task);
                        newState.remainingCapacity -= task.weight;
                        newState.tasksLeft.remove(task);
                        successors.add(newState);
                    }
                }
            }

            this.successors = successors;
        }
    }

    public boolean isSameState(State other) {
        List<Boolean> checks = new ArrayList<>();
        // Is in the same city
        checks.add(this.vehiclePos == other.vehiclePos);
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

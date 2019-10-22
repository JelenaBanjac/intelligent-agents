package behavior;

/* import table */
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import state.State;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class Deliberative implements DeliberativeBehavior {

    enum Algorithm { BFS, ASTAR }

    /* Environment */
    Topology topology;
    TaskDistribution td;

    /* the properties of the agent */
    Agent agent;
    int capacity;

    /* the planning class */
    Algorithm algorithm;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;

        // initialize the planner
        int capacity = agent.vehicles().get(0).capacity();
        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

        // ...
    }

    @Override
    public Plan plan(Vehicle vehicle, TaskSet tasks) {
        Plan plan;
        State initialState = new State(vehicle, tasks, vehicle.getCurrentTasks());

        // Compute the plan with the selected algorithm.
        switch (algorithm) {
            case ASTAR:
                // ...
                plan = naivePlan(vehicle, tasks);
                break;
            case BFS:
                State finalState = BFS(initialState);
                plan = buildPlan(finalState, vehicle.homeCity());
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        System.out.println("Optimal plan distance: " + plan.totalDistance());
        return plan;
    }

    private boolean stateIsRedundant(State s, List<State> C) {
        for (State c : C) {
            if (s.isAlreadyDiscoveredAs(c)) {
                return true;
            }
        }
        return false;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity))
                plan.appendMove(city);

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path())
                plan.appendMove(city);

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }

    @Override
    public void planCancelled(TaskSet carriedTasks) {

        if (!carriedTasks.isEmpty()) {
            // This cannot happen for this simple agent, but typically
            // you will need to consider the carriedTasks when the next
            // plan is computed.
        }
    }

    private State BFS(State initial) {
        List<State> finalStates = new ArrayList<>();

        // BFS Search
        Queue<State> Q = new LinkedList<>();
        List<State> C = new ArrayList<>();
        Q.add(initial);

        while (!Q.isEmpty()) {
            State n = Q.poll();

            // Check if we have already reached n with lesser cost
            if (!stateIsRedundant(n, C)) {
                //n.printState();
                C.add(n);
                n.generateSuccessors();

                if (n.getSuccessors().isEmpty())
                    finalStates.add(n);
                else
                    Q.addAll(n.getSuccessors());
            }
        }

        // Select optimal final state
        State optimalState = finalStates.get(0);
        double optimalCost = finalStates.get(0).getCost();

        for (State s : finalStates) {
            if (s.getCost() < optimalCost) {
                optimalCost = s.getCost();
                optimalState = s;
            }
        }

        return optimalState;
    }

    private Plan buildPlan(State finalState, City startingPoint) {
        Plan plan = new Plan(startingPoint);

        for (Action a : finalState.getActions()) {
            plan.append(a);
        }

        return plan;
    }
}

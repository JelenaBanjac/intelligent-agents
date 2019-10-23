package behavior;

/* import table */

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
import action.Action;
import java.util.Collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * An optimal planner for one vehicle.
 */
@SuppressWarnings("unused")
public class Deliberative implements DeliberativeBehavior {

    enum Algorithm { BFS, ASTAR, NAIVE }

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
        State finalState;
        State initialState = new State(vehicle, tasks);

        long startTime = System.currentTimeMillis();
        // Compute the plan with the selected algorithm.
        switch (algorithm) {
            case ASTAR:
                // ...
            	finalState = ASTAR(initialState);
                plan = buildPlan(finalState, vehicle);
                break;
            case BFS:
                finalState = BFS(initialState);
                plan = naivePlan(vehicle, tasks);
                // plan = buildPlan(finalState);
                break;
            case NAIVE:
            	plan = naivePlan(vehicle, tasks);
                break;
            default:
                throw new AssertionError("Should not happen.");
        }
        System.out.println("Time taken: " + (System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
		
        return plan;
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
    
    public Plan buildPlan(State state, Vehicle vehicle) {
    	City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Action action : state.actions) {
        	City destinationCity;
        	if (action.type == Action.Type.DELIVER) {
        		destinationCity = action.task.deliveryCity;
        	} else {
        		destinationCity = action.task.pickupCity;
        	}
        	
        	for (City city : current.pathTo(destinationCity)) {
        		plan.appendMove(city);
        	}
        	current = destinationCity;
        	
        	if (action.type == Action.Type.DELIVER) {
        		plan.appendDelivery(action.task);
        	} else {
        		plan.appendPickup(action.task);
        	}
        	
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
    
    public State itsCopyInC(State n, List<State> C) {
    	for (State c : C) {
            if (n.isAlreadyDiscoveredAs(c)) {
                return c;
            }
        }
        return null;
    }
    
    public boolean existsInC(State n, List<State> C) {
    	for (State c : C) {
            if (n.isAlreadyDiscoveredAs(c)) {
                return true;
            }
        }
        return false;
    }
    

    
	public State ASTAR(State initial) {
    	Plan plan = new Plan(initial.getVehiclePosition());
    	
    	List<State> Q = new LinkedList<>();
        List<State> C = new ArrayList<>();
        State n = null;
        
        Q.add(initial);
        
        while (!Q.isEmpty()) {
        	n = Q.remove(0);
        	
        	if (n.isFinal()) return n;
        	
        	if (!existsInC(n, C) || n.getHeuristic() < itsCopyInC(n, C).getHeuristic()) {
        		// add n to C
        		C.add(n);
        		// add successors of n
        		Q.addAll(n.getSuccessors());
        		// sort and merge
        		Collections.sort(Q);
        	}

        }
        
        return n;
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
            if (!existsInC(n, C)) {
                //n.printState();
                C.add(n);
//                n.generateSuccessors();

                if (n.getSuccessors().isEmpty())
                    finalStates.add(n);
                else
                    Q.addAll(n.getSuccessors());
            }
        }

        // Select optimal final state
        State optimalState = finalStates.get(0);
        double optimalCost = finalStates.get(0).cost;

        for (State s : finalStates) {
            if (s.cost < optimalCost) {
                optimalCost = s.cost;
                optimalState = s;
            }
        }

        return optimalState;
    }
/*
    private boolean alreadyReachedState(OldState n, List<OldState> C) {
        for (OldState c : C) {
            if (n.isSameState(c)) {
                System.out.println("Match!");
                return true;
            }
        }
        return false;
    }

    private Plan buildPlan(OldState finalState) {
        City startingPoint = finalState.getPreviousStates().get(0).getVehiclePos();
        Plan plan = new Plan(startingPoint);

        for (OldState state : finalState.getPreviousStates()) {
            for (Action a : state.getActions())
                plan.append(a);
        }

        return plan;
    }*/
}

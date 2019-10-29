package behavior;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cop.SLS;
import cop.Solution;
import cop.TaskAction;
import logist.LogistSettings;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.config.Parsers;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class Centralized implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;

    @Override
    public void setup(Topology topology, TaskDistribution distribution,
                      Agent agent) {

        // this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_default.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }

        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);

        this.topology = topology;
        this.distribution = distribution;
        this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();

//		System.out.println("Agent " + agent.id() + " has tasks " + tasks);
//        Plan planVehicle1 = naivePlan(vehicles.get(0), tasks);
//        Plan planVehicle1 = buildPlan(vehicles.get(0));

        SLS sls = new SLS(tasks, vehicles);
        Solution initial = sls.selectInitialSolution();
        List<Plan> plans = buildPlans(vehicles, initial);

        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");

        return plans;
    }

    private List<Plan> buildPlans(List<Vehicle> vehicles, Solution solution) {
        List<Plan> plans = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            // Get first task (and check if vehicle has any tasks)
            TaskAction nextTask = solution.nextTask(vehicle);

            // If vehicle has tasks assigned to it
            if (nextTask != null) {
                City current = vehicle.getCurrentCity();
                Plan plan = new Plan(current);

                while (nextTask != null) {
                    City destination = nextTask.getCity();

                    // Add a move action to the city that the action takes place in
                    for (City city : current.pathTo(destination))
                        plan.appendMove(city);

                    // Append the action to the plan
                    plan.append(nextTask.getAction());

                    // Fetch next task
                    current = destination;
                    nextTask = solution.nextTask(nextTask);
                }

                plans.add(plan);
            } else {
                plans.add(Plan.EMPTY);
            }
        }

        return plans;
    }

    private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (Task task : tasks) {
            // move: current city => pickup location
            for (City city : current.pathTo(task.pickupCity)) {
                plan.appendMove(city);
            }

            plan.appendPickup(task);

            // move: pickup location => delivery location
            for (City city : task.path()) {
                plan.appendMove(city);
            }

            plan.appendDelivery(task);

            // set current city
            current = task.deliveryCity;
        }
        return plan;
    }
}

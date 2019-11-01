package template;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
import variables.PDTask;
import variables.PDTask.Type;
import variables.Solution;

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 *
 */
@SuppressWarnings("unused")
public class CentralizedTemplate implements CentralizedBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private long timeout_setup;
    private long timeout_plan;
    private static Random random = new Random(123);
    
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
        
        Solution solution = SLS(vehicles, tasks);
        
        List<Plan> plans = new ArrayList<Plan>();
        // for every vehicle, add the solution of SLS
        for (Vehicle vehicle : vehicles) {
        	plans.add(slsPlan(vehicle, solution.variables.get(vehicle)));
        }
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        // plans in the same order as its corresponding vehicles
        return plans;
    }
    
    private List<Solution> chooseNeighbors(Solution Aold) {
		/**
		 * In each iteration, we choose one vehicle at random and perform 
		 * local operators on this vehicle to compute the neighbor solutions.
		 */
		List<Solution> N = new ArrayList<Solution>();
		
		// vehicle vi has task
		Vehicle vi;
		do {
			int randNum = random.nextInt(Aold.variables.keySet().size()-1);
			vi = Aold.getVehicles().get(randNum);
		} while (Aold.variables.get(vi).size() == 0);
		
		// apply the changing vehicle operator
		for (Vehicle vj : Aold.getVehicles()) {
			if (vj == vi) continue;
			PDTask t = Aold.variables.get(vi).get(0);
			
			if (Aold.vehicleTasksWeight(Aold.variables.get(vj)) + t.getTask().weight <= vj.capacity()) {
				Solution A = changeVehicle(Aold, vi, vj);
				
				if (A.constraints()) N.add(A);
			}
		}
		
		// apply the changing task order operator
		int numberOfTasksInVehicle = Aold.variables.get(vi).size();
		if (numberOfTasksInVehicle >= 4) {
			for (int tIdx1 = 0; tIdx1 < numberOfTasksInVehicle-1; tIdx1++) {
				for (int tIdx2 = tIdx1+1; tIdx2 < numberOfTasksInVehicle; tIdx2++) {
					PDTask t1 = Aold.variables.get(vi).get(tIdx1);
					PDTask t2 = Aold.variables.get(vi).get(tIdx2);
					
					if (t1.getTask() == t2.getTask()) continue;
					
					if (t1.getType() == Type.PICKUP) {
						PDTask t1d = new PDTask(t1.getTask(), Type.DELIVER);
						// if pickup will be placed after delivery
						if (tIdx2 > Aold.variables.get(vi).indexOf(t1d)) continue;
					} else {
						PDTask t1p = new PDTask(t1.getTask(), Type.PICKUP);
						// if deliver will be placed before pickup
						if (tIdx2 < Aold.variables.get(vi).indexOf(t1p)) continue;
					}
					
					if (t2.getType() == Type.PICKUP) {
						PDTask t2d = new PDTask(t2.getTask(), Type.DELIVER);
						// if pickup will be placed after delivery
						if (tIdx2 > Aold.variables.get(vi).indexOf(t2d)) continue;
					} else {
						PDTask t2p = new PDTask(t2.getTask(), Type.PICKUP);
						// if deliver will be placed before pickup
						if (tIdx2 < Aold.variables.get(vi).indexOf(t2p)) continue;
					}
					
					Solution A = changingTaskOrder(Aold, vi, tIdx1, tIdx2);
					
					if (A.constraints()) N.add(A);
				}
			}
		}
			
		
		return N;
	}

    private Solution changeVehicle(Solution A, Vehicle v1, Vehicle v2) {
    	/**
    	 * Operator 1: Take the first task from the tasks of one vehicle 
    	 * and give it to another vehicle.
    	 */
    	Solution A1 = new Solution(A);
    	
    	List<PDTask> v1Tasks = new ArrayList<PDTask>(A1.variables.get(v1));
    	List<PDTask> v2Tasks = new ArrayList<PDTask>(A1.variables.get(v2));
    	
    	
    	PDTask tP = A1.variables.get(v1).get(0);
    	PDTask tD = new PDTask(tP.getTask(), Type.DELIVER);
    	List<PDTask> toRemove = new ArrayList<PDTask>();
    	toRemove.add(tP);
    	toRemove.add(tD);
    	// remove pickup & delivery
    	v1Tasks.removeAll(toRemove);
    	
    	// add both pickup and delivery to beginning of another vehicle tasks
    	v2Tasks.add(0, tD);
    	v2Tasks.add(0, tP);
    	
    	A1.variables.put(v1, v1Tasks);
    	A1.variables.put(v2, v2Tasks);
    	
    	return A1;
    }
    
    private Solution changingTaskOrder(Solution A, Vehicle vi, int tIdx1, int tIdx2) {
    	/**
    	 * Operator 2: Change the order of two tasks in the task list of 
    	 * a vehicle.
    	 */
    	Solution A1 = new Solution(A);
    	
    	List<PDTask> vTasks = new ArrayList<PDTask>(A.variables.get(vi));
    	
    	Collections.swap(vTasks, tIdx1, tIdx2);
    	
    	A1.variables.put(vi, vTasks);
    	
    	return A1;
    }
    
    private Solution localChoice(List<Solution> N, Solution A, double p) {
    	Solution Anew = null;
    	
    	for (Solution solution : N) {
    		if (Anew == null || cost(solution) < cost(Anew) ) {
    			Anew = new Solution(solution);
    		}
    	}
    	
    	if (Anew != null && random.nextFloat() < p) {
    		return Anew;
    	} else {
    		return A;
    	}
    }
    
    public double cost(Solution s) {
    	double cost = 0.0;
    	
    	for (Vehicle vehicle : s.variables.keySet()) {
    		cost += slsPlan(vehicle, s.variables.get(vehicle)).totalDistance() * vehicle.costPerKm();
    	}
    	
    	return cost;
    }
    
    public Solution SLS(List<Vehicle> vehicles, TaskSet tasks) {
    	//TODO: implement SLS algorithm
    	
    	// select initial solution
    	Solution A = new Solution(vehicles, tasks);
    	
    	int iteration = 0;
    	int maxNumberOfIterations = 10000;
    	double p = 0.3;
    	
    	do {
			Solution Aold = new Solution(A);
			List<Solution> N = chooseNeighbors(Aold);
			A = localChoice(N, A, p);
			
    		iteration++;
		} while (iteration < maxNumberOfIterations);
    	
    	return A;
    } 
    

    private Plan slsPlan(Vehicle vehicle, List<PDTask> vehicleTasks) {
    	City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);

        for (PDTask task : vehicleTasks) {
        	if (task.getType() == Type.PICKUP) {
        		// move: current city => pickup location
        		for (City city : current.pathTo(task.getTask().pickupCity)) {
        			plan.appendMove(city);
        		}
        		// pickup
        		plan.appendPickup(task.getTask());
        		
        		 // set current city
        		current = task.getTask().pickupCity;
        	} else {
        		// move: pickup location => delivery location
        		for (City city : current.pathTo(task.getTask().deliveryCity)) {
        			plan.appendMove(city);
        		}
        		// deliver
        		plan.appendDelivery(task.getTask());
        		
        		// set current city
        		current = task.getTask().deliveryCity;
        	}
            
        }
        return plan;
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

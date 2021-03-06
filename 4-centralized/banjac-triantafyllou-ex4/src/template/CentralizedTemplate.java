package template;

import java.io.File;
//the list of imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
import java.util.HashMap;
import java.util.HashSet;

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
    private boolean debug = false;
    
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
    
    private List<Solution> chooseNeighborsRandomVehicle(Solution Aold) {
		/**
		 * In each iteration, we choose one vehicle at random and perform 
		 * local operators on this vehicle to compute the neighbor solutions.
		 */
		List<Solution> N = new ArrayList<Solution>();

		// choose random vehicle vi
		int randNum = random.nextInt(Aold.variables.keySet().size()-1);
		Vehicle vi = Aold.getVehicles().get(randNum);
		
		
		// apply the changing vehicle operator
		for (Vehicle vj : Aold.getVehicles()) {
			if (vi == vj || Aold.variables.get(vj).size() == 0) continue;
			
			Solution Anew = changeVehicle(Aold, vj, vi);
			if (Anew.constraints()) N.add(Anew);
		}

		// apply the changing task order operator
		int numberOfTasksInVehicle = Aold.variables.get(vi).size();
		if (numberOfTasksInVehicle >= 4) {
			for (int tIdx1 = 0; tIdx1 < numberOfTasksInVehicle-1; tIdx1++) {
				for (int tIdx2 = tIdx1+1; tIdx2 < numberOfTasksInVehicle; tIdx2++) {
					PDTask t1 = Aold.variables.get(vi).get(tIdx1);
					PDTask t2 = Aold.variables.get(vi).get(tIdx2);
					
					Solution Anew = changingTaskOrder(Aold, vi, tIdx1, tIdx2);
					if (Anew.constraints()) N.add(Anew);						
				}
			}
		}	
			
		return N;
	}
    
    
    private List<Solution> chooseNeighbors(Solution Aold) {
		/**
		 * In each iteration, we choose one vehicle at random and perform 
		 * local operators on this vehicle to compute the neighbor solutions.
		 */
		List<Solution> N = new ArrayList<Solution>();
		
		//TODO: change the more tasks from one to two
		
		for (Vehicle vi : Aold.getVehicles()) {
		
			// apply the changing vehicle operator
			for (Vehicle vj : Aold.getVehicles()) {
				if (vi == vj || Aold.variables.get(vj).size() == 0) continue;
				
				Solution Anew = changeVehicle(Aold, vj, vi);
				if (Anew.constraints()) N.add(Anew);
				
				// apply the changing task order operator
				int numTasks = Aold.variables.get(vi).size();
				if (numTasks >= 4) {
					for (int tIdx1 = 0; tIdx1 < numTasks-1; tIdx1++) {
						for (int tIdx2 = tIdx1+1; tIdx2 < numTasks; tIdx2++) {
							PDTask t1 = Aold.variables.get(vi).get(tIdx1);
							PDTask t2 = Aold.variables.get(vi).get(tIdx2);
							
							Solution A = changingTaskOrder(Aold, vi, tIdx1, tIdx2);
							
							if (A.constraints()) N.add(A);
						}
					}
				}
				// end
				
			}
			
			//TODO: more tasks to change the order, not only 2
			
			// apply the changing task order operator
			int numberOfTasksInVehicle = Aold.variables.get(vi).size();
			if (numberOfTasksInVehicle >= 4) {
				for (int tIdx1 = 0; tIdx1 < numberOfTasksInVehicle-1; tIdx1++) {
					for (int tIdx2 = tIdx1+1; tIdx2 < numberOfTasksInVehicle; tIdx2++) {
						PDTask t1 = Aold.variables.get(vi).get(tIdx1);
						PDTask t2 = Aold.variables.get(vi).get(tIdx2);
						
						Solution Anew = changingTaskOrder(Aold, vi, tIdx1, tIdx2);
						if (Anew.constraints()) N.add(Anew);
						
						// apply the changing vehicle operator
						for (Vehicle vj : Anew.getVehicles()) {
							if (vi == vj || Anew.variables.get(vj).size() == 0) continue;
							
							Solution A = changeVehicle(Anew, vj, vi);
						
							if (A.constraints()) N.add(A);
						}
						// end
							
					}
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
    	
    	int idx = 0; //random.nextInt(A.variables.keySet().size()-1);
    	
    	PDTask tP = A1.variables.get(v1).get(idx); 
    	PDTask tD = A1.findPairTask(v1, tP); 
    	v1Tasks.remove(tP);
    	v1Tasks.remove(tD);
    	
    	// add both pickup and delivery to beginning of another vehicle tasks
    	v2Tasks.add(0, tD);  // v2Tasks.add(tP);
    	v2Tasks.add(0, tP);  //v2Tasks.add(tD);
    	
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
    
    private void printCostSolutions(HashMap<Double, List<Solution>> costSolutions) {
    	for (double k : costSolutions.keySet()) {
    		System.out.println(k + " " + costSolutions.get(k).size());
    	}
    	System.out.println(costSolutions.keySet().size());
    }
    
    private Solution localChoice(List<Solution> N, Solution A, double p) {
    	Solution Anew = A;
    	double bestCost = cost(A);
    	
    	if (Anew != null && random.nextFloat() < p) {
	    	for (Solution s : N) {
	    		double cost = cost(s);
	    		if (cost <= bestCost) {
	    			Anew = s;
	    			bestCost = cost;
	    		}
	    	}
	    	return Anew;
    	} else {
    		int randInd = random.nextInt(N.size());
    		return N.get(randInd);
    	}
    	
    }
    
    public double cost(Solution s) {
    	double cost = 0.0;
    	
    	for (Vehicle vehicle : s.variables.keySet()) {
    		cost += slsPlan(vehicle, s.variables.get(vehicle)).totalDistance() * vehicle.costPerKm();
    	}
    	
    	return cost;
    }
    
    public Solution getBestSolution(HashSet<Solution> solutions) {
    	double minCost = 10000000.0;
    	Solution bestSolution = null;
    	for (Solution s : solutions) {
    		if (cost(s) < minCost) {
    			minCost = cost(s);
    			bestSolution = s;
    		}
    	}
    	return bestSolution;
    }
    
    private Solution getRandomObject(HashSet<Solution> from) {
	   Random rnd = new Random();
	   int i = rnd.nextInt(from.size());
	   return (Solution) from.toArray()[i];
	}
    
    public Solution SLS(List<Vehicle> vehicles, TaskSet tasks) {
    	// select initial solution
    	Solution A = new Solution(vehicles, tasks);
    	
    	int iteration = 0;
    	int maxNumberOfIterations = 1000000;
    	double p = 0.1;  // best [0.3, 0.5]
    	
    	long start_time = System.currentTimeMillis();
    	long current_time = System.currentTimeMillis();
    	
    	System.out.println("Iteration " + iteration + " (" + (current_time-start_time) + "ms) cost " + cost(A));
		if (debug) {
			System.out.println(A);
		}
		
		HashSet<Solution> rollbackSolutions = new HashSet<Solution>();
		int sameSolution = 0;
		int sameSolutionLimit = 10;
    	
    	do {
			Solution Aold = new Solution(A);
			List<Solution> N = chooseNeighbors(Aold); //chooseNeighborsRandomVehicle(Aold);
			A = localChoice(N, A, p);
			
			current_time = System.currentTimeMillis();
			
    		iteration++;
    		
    		if (cost(Aold) > cost(A)) {
				System.out.println("Iteration " + iteration + " (" + (current_time-start_time) + "ms) cost " + cost(A));
	    		if (debug) {
					System.out.println(A);
	    		}
	    		rollbackSolutions.add(A);
	    		sameSolution = 0;
			} else {
				sameSolution++;
				if (sameSolution > sameSolutionLimit) {
					int randInt = random.nextInt(10);
					A = getRandomObject(rollbackSolutions);
					p = random.nextFloat();
					System.out.println("--- rollback --- (solution cost )" + cost(A));
				}
			}
		} while (iteration < maxNumberOfIterations && (current_time-start_time + 1000) < timeout_plan);
    	
    	
    	A = getBestSolution(rollbackSolutions);
    	
    	System.out.println("Number of iterations " + iteration);
    	System.out.println("Solution cost " + cost(A));
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

}

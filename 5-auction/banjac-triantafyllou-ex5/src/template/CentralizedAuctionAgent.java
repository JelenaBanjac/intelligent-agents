package template;

//the list of imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.io.File;
import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;
import logist.LogistSettings;
import logist.config.Parsers;
import variables.PDTask;
import variables.PDTask.Type;
import variables.Solution;
import variables.SolutionCost;
import logist.task.DefaultTaskDistribution;;


@SuppressWarnings("unused")
public class CentralizedAuctionAgent implements AuctionBehavior {

	private Topology topology;
	private DefaultTaskDistribution distribution;
	private Agent agent;
	private Random random;
	private Vehicle vehicle;
	private City currentCity;
    private long timeout_setup;
    private long timeout_plan;
    private long timeout_bid;
    private Solution solution;
    private Solution newSolution;
    private Long totalReward = 0l;
    private int winCount = 0;
    private int bidCount = 0;
    
    private Task currentTask;
	public HashMap<Integer, List<Long>> bidHistory = new HashMap<>();
	public List<Integer> winners = new ArrayList<>();

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		// this code is used to get the timeouts
        LogistSettings ls = null;
        try {
            ls = Parsers.parseSettings("config" + File.separator + "settings_auction.xml");
        }
        catch (Exception exc) {
            System.out.println("There was a problem loading the configuration file.");
        }
        
        // the setup method cannot last more than timeout_setup milliseconds
        timeout_setup = ls.get(LogistSettings.TimeoutKey.SETUP);
        // the plan method cannot execute more than timeout_plan milliseconds
        timeout_plan = ls.get(LogistSettings.TimeoutKey.PLAN);
        // the bid timeout
        timeout_bid = ls.get(LogistSettings.TimeoutKey.BID);
        
		this.topology = topology;
		this.distribution = (DefaultTaskDistribution) distribution;
		this.agent = agent;
		this.vehicle = agent.vehicles().get(0);
		this.currentCity = vehicle.homeCity();

		long seed = -9019554669489983951L * currentCity.hashCode() * agent.id();
		this.random = new Random(seed);
		
		this.solution = new Solution(agent.vehicles());
	}

	public void addBids(Long[] bids, int winnerID) {
		for (int i = 0; i < bids.length; ++i) {
			List<Long> currenbids = bidHistory.get(i);
			if (currenbids == null) {
				currenbids = new ArrayList<Long>();
			}
			currenbids.add(bids[i]);
			bidHistory.put(i, currenbids);
		}

		winners.add(winnerID);
	}

	@Override
	public void auctionResult(Task previous, int winner, Long[] bids) {
		final boolean won = winner == agent.id();
		if (won) {
			solution = newSolution != null ? newSolution : Solution.extendSolution(solution, currentTask);
			newSolution = null;

			totalReward += bids[winner];
			++winCount;
		}

		++bidCount;

		addBids(bids, winner);

		System.out.print(" bid " + bids[agent.id()] + " for num bids " + bidCount + " and ");
		System.out.println((won ? "won" : "lost") + " [total = " + winCount + "]");
	}


	
	public SolutionCost computeMarginalCost() {
		
		solution = new Solution(getSolution(solution.getVehicles(), solution.getTasks(), timeout_bid/2));
		
		// naive estimator and no future
		double currentCost = cost(solution);
		
		// TODO: recompute the plan on the extended solution as well
		Solution extendedSolution = Solution.extendSolution(solution, currentTask);
		extendedSolution = new Solution(getSolution(extendedSolution.getVehicles(), extendedSolution.getTasks(), timeout_bid/2));
		
		double extendedCost = cost(extendedSolution);
		
		double marginalCost = Math.max(0, extendedCost - currentCost);		
		
		SolutionCost solutionCost = new SolutionCost(marginalCost, extendedSolution);
		
		double ratio = 1.0 + (random.nextDouble() * 0.05 * currentTask.id);
		double bid = ratio * solutionCost.marginalCost;

		return new SolutionCost(bid, solutionCost.solution);  //(long) Math.round(bid);
	}
	
	
	public SolutionCost computeMarginalCost_advanced() {
		
		// ********
		solution = new Solution(getSolution(solution.getVehicles(), solution.getTasks(), timeout_bid/2));

		// estimate without future
		double currentCost = cost(solution);
		
		// TODO: recompute the plan on the extended solution as well
		Solution extendedSolution = Solution.extendSolution(solution, currentTask);
		double extendedCost = cost(extendedSolution);
		
		double marginalCost = Math.max(0, extendedCost - currentCost);		
		// *********
		
		// using extendedSolution, marginalCost
		double minTasks = 5;
		int numPredictions = 10;
		double risk = 0.7;
		
		long start_time = System.currentTimeMillis();
		SolutionCost solutionCost = new SolutionCost(marginalCost, extendedSolution);
		
		if (extendedSolution.getTasks().size() >= minTasks || solutionCost.marginalCost == 0) {
			return solutionCost;
		}
		
		long middle_time = System.currentTimeMillis();
		long timeout = timeout_bid;
		timeout -= (middle_time - start_time);
		long time_share = (long) ((timeout * 0.95)/numPredictions);
		
		// compute estimations
		double worsePredictionCost = Double.NEGATIVE_INFINITY;
		double bestPredictionCost = Double.POSITIVE_INFINITY;
		double sum = 0;
		
		for (int i = 0; i < numPredictions; i++) {
			start_time = System.currentTimeMillis();
			Solution futureSolution = new Solution(extendedSolution);
			//System.out.println(futureSolution);
			
			// extend plan with random tasks
			while (futureSolution.getTasks().size() < minTasks) {
				futureSolution = Solution.extendSolution(futureSolution, this.distribution.createTask());
			}
			
			middle_time = System.currentTimeMillis();
			
			// ******** futureSolution, task, timeshare-(midtime-starttime).marginalCost
			long future_timeout = time_share - (middle_time - start_time);
			
			futureSolution = new Solution(getSolution(futureSolution.getVehicles(), futureSolution.getTasks(), future_timeout/2));

			// estimate without future10860
			double futureCost = cost(futureSolution);
			
			// TODO: recompute the plan on the extended solution as well
			Solution extendedFutureSolution = Solution.extendSolution(futureSolution, currentTask);
			double extendedFutureCost = cost(extendedFutureSolution);
			
			double prediction = Math.max(0, extendedFutureCost - futureCost);
			// ********
			
			worsePredictionCost = Math.max(0, worsePredictionCost - prediction);
			bestPredictionCost = Math.max(0, bestPredictionCost - prediction);
			
			sum += prediction;
		}
		
		double bid = Math.min(worsePredictionCost, solutionCost.marginalCost);
		bid = solutionCost.marginalCost - (solutionCost.marginalCost - bid) * risk;

		return new SolutionCost(bid, null);  //(long) Math.round(bid);
	}

	
    public double cost(Solution s) {
    	double cost = 0.0;
    	
    	for (Vehicle vehicle : s.variables.keySet()) {
    		cost += onePlan(vehicle, s.variables.get(vehicle)).totalDistance() * vehicle.costPerKm();
    	}
    	
    	return cost;
    }
    
	@Override
	public Long askPrice(Task task) {
		currentTask = task;
		
		// TODO: change to advanced or default here
		SolutionCost result = computeMarginalCost(); //computeMarginalCost_advanced();
		newSolution = result.solution;
		Long bid = (long) Math.ceil(result.marginalCost);
		
		System.out.println("Bid value " + bid);

		return bid;
	}
	
	public Solution getSolution(List<Vehicle> vehicles, List<Task> tasks, long timeout) {
		// TODO: change this
		// select initial solution
    	Solution A = new Solution(vehicles, tasks);
    	if (A.getTasks().size() <= 1) {
    		return A;
    	}
    	
    	int iteration = 0;
    	int maxNumberOfIterations = 1000000;
    	double p = 0.1;  // best [0.3, 0.5]
    	
    	long start_time = System.currentTimeMillis();
    	long current_time = System.currentTimeMillis();
    	
    	System.out.println("Iteration " + iteration + " (" + (current_time-start_time) + "ms) cost " + cost(A));
		
		HashSet<Solution> rollbackSolutions = new HashSet<Solution>();
		// add this solution just so it is not empty 
		rollbackSolutions.add(A);
		
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
		} while (iteration < maxNumberOfIterations && (current_time-start_time + 1000) < timeout);
    	
    	
    	A = getBestSolution(rollbackSolutions);
    	
    	System.out.println("Number of iterations " + iteration);
    	System.out.println("Solution cost " + cost(A));
    	return A;
	}
	
    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
        ArrayList<Task> tsks = new ArrayList<Task>();
        tsks.addAll(tasks);
        
        Solution solution = getSolution(vehicles, tsks, timeout_plan);
        
        double totalCost = cost(solution);
        
        if (totalCost > totalReward) {
        	System.out.println("Money lost..." + (totalReward-totalCost));
        } else {
        	System.out.println("Money earned..." + (totalReward-totalCost));
        }
        
        List<Plan> plans = new ArrayList<Plan>();
        // for every vehicle, add the solution of SLS
        for (Vehicle vehicle : vehicles) {
        	plans.add(onePlan(vehicle, solution.variables.get(vehicle)));
        }
        
        long time_end = System.currentTimeMillis();
        long duration = time_end - time_start;
        System.out.println("The plan was generated in " + duration + " milliseconds.");
        
        // plans in the same order as its corresponding vehicles
        return plans;
    }
	
	private Plan onePlan(Vehicle vehicle, List<PDTask> vehicleTasks) {
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
	
	
	//************************************************** For SLS **************************************************//
	
	
	private List<Solution> chooseNeighbors(Solution Aold) {
		/**
		 * In each iteration, we choose one vehicle at random and perform 
		 * local operators on this vehicle to compute the neighbor solutions.
		 */
		List<Solution> N = new ArrayList<Solution>();

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
}

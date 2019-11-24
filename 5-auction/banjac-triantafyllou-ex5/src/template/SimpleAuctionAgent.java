package template;

//the list of imports
import java.util.ArrayList;
import java.util.HashMap;
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


@SuppressWarnings("unused")
public class SimpleAuctionAgent implements AuctionBehavior {

	private Topology topology;
	private TaskDistribution distribution;
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
		this.distribution = distribution;
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
		// naive estimator and no future
		double currentCost = cost(solution);
		
		Solution extendedSolution = Solution.extendSolution(solution, currentTask);
		double extendedCost = cost(extendedSolution);
		
		double marginalCost = Math.max(0, extendedCost - currentCost);
		
		SolutionCost solutionCost = new SolutionCost(marginalCost, extendedSolution);
		
		
		double ratio = 1.0 + (random.nextDouble() * 0.05 * currentTask.id);
		double bid = ratio * solutionCost.marginalCost;

		return new SolutionCost(bid, solutionCost.solution);  //(long) Math.round(bid);
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
		
		SolutionCost result = computeMarginalCost();
		newSolution = result.solution;
		Long bid = (long) Math.ceil(result.marginalCost);

		return bid;
	}
	
	public Solution getSolution(List<Vehicle> vehicles, TaskSet tasks) {
		// TODO: change this
		Solution solution = new Solution(vehicles, tasks);
		
		return solution;
	}
	
    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        long time_start = System.currentTimeMillis();
        
        Solution solution = getSolution(vehicles, tasks);
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
}

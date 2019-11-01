package variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import variables.PDTask.Type;

public class Solution {
	/**
	 * Vehicle as a key, and list of tasks as a value.
	 * The order of the list is important since it represents 
	 * the time point when the task will be executed.
	 */
	public HashMap<Vehicle, List<PDTask>> variables;

	private Vehicle biggestVehicle(List<Vehicle> vehicles) {
		// initially, get the first vehicle from the list
		Vehicle biggestVehicle = vehicles.get(0);
		
		// finding the vehicle in the list with the biggest capacity
		for (Vehicle vehicle : vehicles) {
			if (vehicle.capacity() > biggestVehicle.capacity()) {
				biggestVehicle = vehicle;
			}
		}
		
		return biggestVehicle;
	}
	
	public Solution() {
		this.variables = new HashMap<Vehicle, List<PDTask>>();
	}
	
	
	public Solution(List<Vehicle> vehicles, TaskSet tasks) {
		this.variables = new HashMap<Vehicle, List<PDTask>>();
		
		// initialize all vehicle tasks
		for (Vehicle vehicle : vehicles) {
			this.variables.put(vehicle, new ArrayList<PDTask>());
		}
		
		// give all the tasks to the biggest vehicle (after pickup immediately deliver)
		Vehicle biggestVehicle = biggestVehicle(vehicles);
		for (Task task : tasks) {
			if (task.weight > biggestVehicle.capacity()) {
				new Exception("Problem is unsolvable!");
			}
			this.variables.get(biggestVehicle).add(new PDTask(task, Type.PICKUP));
			this.variables.get(biggestVehicle).add(new PDTask(task, Type.DELIVER));
		}
	}
	
	public Solution(Solution s) {
		this.variables = new HashMap<Vehicle, List<PDTask>>();
		
		for (Vehicle vehicle : s.variables.keySet()) {
			List<PDTask> tasks = s.variables.get(vehicle);
			this.variables.put(vehicle, new ArrayList<PDTask>(tasks));
		}
	}
	
	private List<Solution> chooseNeighbors() {
		List<Solution> neighbors = new ArrayList<Solution>();
		
		return neighbors;
	}

    private Solution changeVehicle(Solution A, Vehicle v1, Vehicle v2) {
    	Solution A1 = new Solution(A);
    	
    	return A1;
    }
    
    private Solution changingTaskOrder(Solution A, Vehicle vi, int tIdx1, int iIdx2) {
    	Solution A1 = new Solution(A);
    	
    	return A1;
    }
    
    private Solution localChoice(List<Solution> neighbors, Solution A, double p) {
    	Solution Anew = null;
    	
    	return Anew;
    }
    
    public static Solution SLS(List<Vehicle> vehicles, TaskSet tasks) {
    	//TODO: implement SLS algorithm
    	Solution initialSolution = new Solution(vehicles, tasks);
    	
    	int iteration = 0;
    	int maxNumberOfIterations = 10000;
    	
    	do {
			
    		iteration++;
		} while (iteration < maxNumberOfIterations);
    	
    	return initialSolution;
    } 
	
	private List<Task> getTasksOnly(List<PDTask> pdtasks) {
		List<Task> tasks = new ArrayList<Task>();
		
		for (PDTask pdtask : pdtasks) {
			tasks.add(pdtask.getTask());
		}
		return tasks;
	}
	
	public boolean constraints(TaskSet tasks) {
		//TODO: check constraints
		
		int totalTasks = 0;
		Set<Task> tasksSet = new HashSet<Task>();
		for (List<PDTask> vehicleTasks : this.variables.values()) {
			totalTasks += vehicleTasks.size();
			tasksSet.addAll(getTasksOnly(vehicleTasks));
		}

		if (totalTasks != 2*tasks.size()) {
			return false;
		}
		tasksSet.retainAll(tasks);
		if (tasksSet.size() != tasks.size()) {
			return false;
		}
		
		
		for (Vehicle vehicle : this.variables.keySet()) {
			List<PDTask> vehiclePDTasks = this.variables.get(vehicle);
			List<Task> vehicleTasks = getTasksOnly(vehiclePDTasks);
			
			double totalTaskVeight = 0.0;
			for (Task task : vehicleTasks) {
				int tp = vehiclePDTasks.indexOf(new PDTask(task, Type.PICKUP));
				int td = vehiclePDTasks.indexOf(new PDTask(task, Type.DELIVER));
				
				// if there is no corresponding pickup/delivery of the task
				if (tp == -1 || td == -1) {
					return false;
				}
				// if delivery happened before pickup
				if (tp > td) {
					return false;
				}
				
				totalTaskVeight += task.weight;
			}
			
			// if vehicle cannot handle all the tasks it carries
			if (totalTaskVeight > vehicle.capacity()) {
				return false;
			}
		}
		
		
		
		return true;
	}
	
	public double cost(Plan plan, Vehicle vehicle) {
		double cost = 0.0;
		
		//TODO: calculate the cost
		cost = plan.totalDistance() * vehicle.costPerKm();
		
		return cost;
	}
}

package variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	

	private static Vehicle biggestVehicle(List<Vehicle> vehicles) {
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
	
	public Solution(List<Vehicle> vehicles) {
		this.variables = new HashMap<Vehicle, List<PDTask>>();
		
		// initialize all vehicle tasks
		for (Vehicle vehicle : vehicles) {
			this.variables.put(vehicle, new ArrayList<PDTask>());
		}
	}
	
	public Solution(List<Vehicle> vehicles, List<Task> tasks) {
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
	
	public static Solution extendSolution(Solution oldSolution, Task newTask) {
		Solution newSolution = new Solution(oldSolution);
		
		Vehicle biggestVehicle = biggestVehicle(oldSolution.getVehicles());
		
		if (newTask.weight > biggestVehicle.capacity()) {
			new Exception("Problem is unsolvable!");
		}
		newSolution.variables.get(biggestVehicle).add(new PDTask(newTask, Type.PICKUP));
		newSolution.variables.get(biggestVehicle).add(new PDTask(newTask, Type.DELIVER));
		
		return newSolution;
	}
	
	
	@Override
	public String toString() {
		String retVal = "Solution: \n";
		for (Vehicle v : variables.keySet()) {
			retVal += "\t vehicleId=" + v.id() + "\n";
			for (PDTask task : variables.get(v)) {
				retVal += "\t\t" + "task" + task.getTask().id + " " + task.getType() + "\n";
			}
		}
		retVal += "\n";
		return retVal;
	}

	public List<Vehicle> getVehicles() {
		List<Vehicle> vehicles = new ArrayList<Vehicle>();
		
		for (Vehicle vehicle : this.variables.keySet()) {
			vehicles.add(vehicle);
		}
		return vehicles;
	}
	
	public List<Task> getTasks() {
		List<Task> tasks = new ArrayList<Task>();
		HashSet<Task> tasksSet = new HashSet<Task>();
		
		for (List<PDTask> vTasks : this.variables.values()) {
			for (PDTask task : vTasks) {
				tasksSet.add(task.getTask());
			} 			
		}
		tasks.addAll(tasksSet);
		return tasks;
	}
	
	public boolean exceedesVehicleCapacity(Vehicle vehicle, List<PDTask> tasks) {
		double currentWeight = 0.0;
		
		for (PDTask task : tasks) {
			double currentTaskWeight = task.getTask().weight;
			
			if (task.getType() == Type.PICKUP) {
				
				currentWeight += currentTaskWeight;
				
				if (currentWeight > vehicle.capacity())
					return true;
			} else {
				currentWeight -= currentTaskWeight;
			}
		}
		
		return false;
	}
	
	public PDTask findPairTask(Vehicle vehicle, PDTask task1) {
		for (PDTask task : this.variables.get(vehicle)) {
			if (task1.getTask() == task.getTask()) {
				if (task1.getType() == task.getType()) continue;
				else {
					return task;
				}
			}
		}
		return null;
	}
	
	public boolean constraints() {
		//TODO: check constraints
		for (Vehicle vehicle : this.variables.keySet()) {
			List<PDTask> vehiclePDTasks = this.variables.get(vehicle);
			
			// if vehicle cannot handle all the tasks it carries
			if (exceedesVehicleCapacity(vehicle, vehiclePDTasks)) {
				
				return false;
			}
						
			for (PDTask task : vehiclePDTasks) {
				PDTask pairTask = findPairTask(vehicle, task);
				
				int tp;
				int td;
				if (task.getType() == Type.PICKUP) {
					tp = vehiclePDTasks.indexOf(task);
					td = vehiclePDTasks.indexOf(pairTask);
				} else {
					tp = vehiclePDTasks.indexOf(pairTask);
					td = vehiclePDTasks.indexOf(task);
				}
				
				// if there is no corresponding pickup/delivery of the task
				if (tp == -1 || td == -1) {
					return false;
				}
				// if delivery happened before pickup
				if (tp > td) {
					return false;
				}
			}
			
		}
		
		return true;
	}
	
}

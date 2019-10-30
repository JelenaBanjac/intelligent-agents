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

public class Variables {
	/**
	 * Vehicle as a key, and list of tasks as a value.
	 * The order of the list is important since it represents 
	 * the time point when the task will be executed.
	 */
	private HashMap<Vehicle, List<PDTask>> variables;

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
	
	
	public void initialize(List<Vehicle> vehicles, TaskSet tasks) {
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
			
			for (Task task : vehicleTasks) {
				int tp = vehiclePDTasks.indexOf(new PDTask(task, Type.PICKUP));
				int td = vehiclePDTasks.indexOf(new PDTask(task, Type.DELIVER));
				
				if (tp == -1 || td == -1) {
					return false;
				}
				if (tp > td) {
					return false;
				}
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

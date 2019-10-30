package variables;

import java.util.HashMap;
import java.util.List;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import variables.PDTask.Type;

public class Variables {

	private HashMap<PDTask, PDTask> nextTask = new HashMap<PDTask, PDTask>();
	private HashMap<PDTask, Integer> time = new HashMap<PDTask, Integer>();
	private HashMap<Task, Vehicle> vehicle = new HashMap<Task, Vehicle>();
	
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
	
	public void generateNextTask(List<Vehicle> vehicles, TaskSet tasks) {
		this.nextTask = new HashMap<PDTask, PDTask>();
		
		// initialize the vehicles with null values
		for (Vehicle vehicle : vehicles) {
			PDTask key = new PDTask(vehicle);
			PDTask value = null;
			this.nextTask.put(key, value);
		}
		// put first task to pickup to the biggest vehicle
		Vehicle biggestVehicle = biggestVehicle(vehicles);
		PDTask key = new PDTask(biggestVehicle);
		PDTask value = new PDTask(tasks.iterator().next(), Type.PICKUP);
		this.nextTask.put(key, value);
		
		
		
		
		for (Task task : tasks) {
			PDTask keyP = new PDTask(task, Type.PICKUP);
			PDTask valueP = null;
			this.nextTask.put(keyP, valueP);
			
			PDTask keyD = new PDTask(task, Type.DELIVER);
			PDTask valueD = null;
			this.nextTask.put(keyD, valueD);			
		}
		
		
		
		
		if (this.nextTask.size() != 2*tasks.size() + vehicles.size()) {
			try {
				throw new Exception("The size of `nextTask` should be " + (2*tasks.size() + vehicles.size()) + ", instead it is " + this.nextTask.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void generateTime(TaskSet tasks) {
		// TODO: values should be different, and range from 1 to 2*N_T
		this.time = new HashMap<PDTask, Integer>();
		
		for (Task task : tasks) {
			PDTask keyP = new PDTask(task, Type.PICKUP);
			int valueP = 0;
			this.time.put(keyP, valueP);
			
			PDTask keyD = new PDTask(task, Type.DELIVER);
			int valueD = 0;
			this.time.put(keyD, valueD);			
		}
		
		if (this.time.size() != 2*tasks.size()) {
			try {
				throw new Exception("The size of `time` should be " + (2*tasks.size()) + ", instead it is " + this.time.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void generateVehicle(Vehicle biggestVehicle, TaskSet tasks) {
		
		this.vehicle = new HashMap<Task, Vehicle>();
		
		for (Task task : tasks) {
			this.vehicle.put(task, biggestVehicle);		
		}
		
		if (this.vehicle.size() != tasks.size()) {
			try {
				throw new Exception("The size of `vehicle` should be " + (tasks.size()) + ", instead it is " + this.vehicle.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean constraints() {
		boolean satisfied = false;
		
		//TODO: check constraints
		
		
		for (PDTask key : this.nextTask.keySet()) {
			PDTask value = this.nextTask.get(key);
			
			// task
			if (key.getTask() != null) {
				// nextTask(t, TYPE) != (t, TYPE)
				if (key.getTask().id == value.getTask().id && key.getType() == value.getType()) {
					return false;
				}
				
			// vehicle
			} else {
				// nextTask(v_k) != (t_j, DELIVER)
				if (value.getType() == Type.DELIVER) {
					return false;
				}
			}
			
			
			
		}
		
		
		return satisfied;
	}
	
	public double cost(Plan plan, Vehicle vehicle) {
		double cost = 0.0;
		
		//TODO: calculate the cost
		cost = plan.totalDistance() * vehicle.costPerKm();
		
		return cost;
	}
}

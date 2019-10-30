package variables;

import logist.simulation.Vehicle;
import logist.task.Task;

public class PDTask {
	enum Type {PICKUP, DELIVER};
	
	private Task task;
	private Type type;
	private Vehicle vehicle;
	
	public PDTask(Task task, Type type) {
		this.task = task;
		this.type = type;
		this.vehicle = null;
	}
	
	public PDTask(Vehicle vehicle) {
		this.task = null;
		this.type = null;
		this.vehicle = vehicle;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
	}
	
	
}

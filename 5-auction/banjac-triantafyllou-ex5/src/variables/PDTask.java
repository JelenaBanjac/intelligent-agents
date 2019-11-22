package variables;

import logist.task.Task;

public class PDTask {
	public enum Type {PICKUP, DELIVER};
	
	private Task task;
	private Type type;
	
	public PDTask(Task task, Type type) {
		this.task = task;
		this.type = type;
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

	
	
}

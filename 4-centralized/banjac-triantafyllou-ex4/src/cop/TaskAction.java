package cop;

import logist.plan.Action;
import logist.plan.Action.Pickup;
import logist.plan.Action.Delivery;
import logist.task.Task;
import logist.topology.Topology.City;

public class TaskAction {
    private Task task;
    private Action action;

    public TaskAction(Task task, Action action) {
        this.task = task;
        this.action = action;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public City getCity() {
        if (this.action instanceof Pickup)
            return this.task.pickupCity;
        else if (this.action instanceof Delivery)
            return this.task.deliveryCity;

        return null; // TODO handle
    }
}

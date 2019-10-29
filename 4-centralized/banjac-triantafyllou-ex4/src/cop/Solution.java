package cop;

import logist.plan.Action.Delivery;
import logist.plan.Action.Pickup;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

import java.util.HashMap;
import java.util.Map;

public class Solution {
    Map<TaskAction, TaskAction> taskNextTask;
    Map<Vehicle, TaskAction> vehicleNextTask;
    Map<TaskAction, Double> taskActionTime;
    Map<TaskAction, Vehicle> taskVehicle;

    public Solution() {
        this.taskNextTask = new HashMap<>();
        this.vehicleNextTask = new HashMap<>();
        this.taskActionTime = new HashMap<>();
        this.taskVehicle = new HashMap<>();
    }

    public TaskAction nextTask(TaskAction taskAction) {
        return taskNextTask.getOrDefault(taskAction, null);
    }

    public TaskAction nextTask(Vehicle vehicle) {
        return vehicleNextTask.getOrDefault(vehicle, null);
    }

    public void appendTask(Task task, Vehicle vehicle) {
        TaskAction latest = this.getLatestTaskAction(vehicle);

        // Create pickup and delivery actions for new task
        TaskAction pickup = new TaskAction(task, new Pickup(task));
        TaskAction delivery = new TaskAction(task, new Delivery(task));

        // Check if the task is the first one for the vehicle
        double currentTime;
        City currentCity;
        if (latest == null) {
            vehicleNextTask.put(vehicle, pickup);
            currentTime = 0;
            currentCity = vehicle.homeCity();

        } else {
            taskNextTask.put(latest, pickup);
            currentTime = taskActionTime.get(latest);
            currentCity = latest.getCity();
        }

        // Add them to the variable maps
        taskNextTask.put(pickup, delivery);

        // Compute time for the pickup action
        double pTime = currentTime + currentCity.distanceTo(task.pickupCity) / vehicle.speed();
        taskActionTime.put(pickup, pTime);

        // Compute time for the delivery action
        double dTime = pTime + task.pickupCity.distanceTo(task.deliveryCity) / vehicle.speed();
        taskActionTime.put(delivery, dTime);

        taskVehicle.put(pickup, vehicle);
        taskVehicle.put(delivery, vehicle);
    }

    public TaskAction getLatestTaskAction(Vehicle vehicle) {
        double max = -1;
        TaskAction latest = null;
        for (Map.Entry<TaskAction, Double> e : this.taskActionTime.entrySet()) {
            if (e.getValue() > max && this.taskVehicle.get(e.getKey()) == vehicle) {
                latest = e.getKey();
                max = e.getValue();
            }
        }

        return latest;
    }
}

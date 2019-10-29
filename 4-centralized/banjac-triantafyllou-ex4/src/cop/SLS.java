package cop;

import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

import java.util.List;

public class SLS {
    private TaskSet tasks;
    private List<Vehicle> vehicles;

    public SLS(TaskSet tasks, List<Vehicle> vehicles) {
        this.tasks = tasks;
        this.vehicles = vehicles;
    }

    public Solution execute() {
        return selectInitialSolution();
    }

    public Solution selectInitialSolution() {
        Solution initial = new Solution();

        // Give all the tasks to the biggest vehicle
        Vehicle biggest = this.getBiggestVehicle(this.vehicles);
        for (Task task : this.tasks) {
            initial.appendTask(task, biggest);
        }

        return initial;
    }

    public Vehicle getBiggestVehicle(List<Vehicle> vehicles) {
        int max = -1;
        Vehicle biggest = null;
        for (Vehicle v : vehicles) {
            if (v.capacity() > max) {
                biggest = v;
                max = v.capacity();
            }
        }
        return biggest;
    }
}

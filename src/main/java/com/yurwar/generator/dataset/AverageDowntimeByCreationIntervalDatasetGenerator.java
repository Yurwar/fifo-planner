package com.yurwar.generator.dataset;

import com.yurwar.entity.Task;
import com.yurwar.generator.task.TaskGenerator;
import lombok.Builder;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.yurwar.planner.FifoPlanner;

import java.util.*;

@Builder
public class AverageDowntimeByCreationIntervalDatasetGenerator {
    private Map<Integer, Double> result;
    private int minTaskDuration;
    private int maxTaskDuration;
    private int minTaskPriority;
    private int maxTaskPriority;
    private int minCreationInterval;
    private int maxCreationInterval;

//    public AverageDowntimeByCreationIntervalDatasetGenerator() {
//        result = new HashMap<>();
//        init();
//    }

    public void init() {
        result = new HashMap<>();
        privateInit();
    }

    private double getDowntimePercentByInterval(int interval) {
        List<Task> tasksToDo = new ArrayList<>();
        TaskGenerator taskGenerator = TaskGenerator.builder()
                .minTaskDuration(minTaskDuration)
                .maxTaskDuration(maxTaskDuration)
                .minTaskPriority(minTaskPriority)
                .maxTaskPriority(maxTaskPriority)
                .minCreationInterval(interval)
                .maxCreationInterval(interval)
                .build();
//        TaskGenerator taskGenerator = TaskGenerator.builder()
//                .minTaskDuration(5)
//                .maxTaskDuration(95)
//                .minTaskPriority(0)
//                .maxTaskPriority(32)
//                .minCreationInterval(interval)
//                .maxCreationInterval(interval)
//                .build();

        taskGenerator.reset();

        for (int i = 0; i < 1000; i++) {
            tasksToDo.add(taskGenerator.generateNextTask());
        }

        FifoPlanner fifoPlanner = new FifoPlanner(tasksToDo);

        List<Task> result = fifoPlanner.execute();

        int totalTime = fifoPlanner.getCurrentTime();
        int downtime = fifoPlanner.getDowntimeCount();

        return (((double) downtime) / ((double) totalTime)) * 100;
    }

    private void privateInit() {
        for (int i = 1; i <= 100; i++) {
            result.put(i, getDowntimePercentByInterval(i));
        }
    }

    public XYDataset getDataset() {
        XYSeries series = new XYSeries("Залежність");

        result.forEach(series::add);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }
}

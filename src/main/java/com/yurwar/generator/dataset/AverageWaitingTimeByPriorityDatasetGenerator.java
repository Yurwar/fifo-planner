package com.yurwar.generator.dataset;

import com.yurwar.entity.Task;
import com.yurwar.generator.task.TaskGenerator;
import lombok.Builder;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import com.yurwar.planner.FifoPlanner;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Builder
public class AverageWaitingTimeByPriorityDatasetGenerator {
    private List<Task> result;
    private Map<Integer, Double> results = new HashMap<>();
    private int minTaskDuration;
    private int maxTaskDuration;
    private int minTaskPriority;
    private int maxTaskPriority;
    private int minCreationInterval;
    private int maxCreationInterval;
//
//    public AverageWaitingTimeByPriorityDatasetGenerator() {
//        init();
//    }

    public void init() {
        results = new HashMap<>();
        privateInit();
    }

    private void privateInit() {
        List<Task> tasksToDo = new ArrayList<>();
        TaskGenerator taskGenerator = TaskGenerator.builder()
                .minTaskDuration(minTaskDuration)
                .maxTaskDuration(maxTaskDuration)
                .minTaskPriority(minTaskPriority)
                .maxTaskPriority(maxTaskPriority)
                .minCreationInterval(minCreationInterval)
                .maxCreationInterval(maxCreationInterval)
                .build();
//        TaskGenerator taskGenerator = TaskGenerator.builder()
//                .minTaskDuration(5)
//                .maxTaskDuration(100)
//                .minTaskPriority(0)
//                .maxTaskPriority(32)
//                .minCreationInterval(5)
//                .maxCreationInterval(100)
//                .build();

        taskGenerator.reset();

        for (int i = 0; i < 1000; i++) {
            tasksToDo.add(taskGenerator.generateNextTask());
        }

        FifoPlanner fifoPlanner = new FifoPlanner(tasksToDo);

        result = fifoPlanner.execute();

        OptionalDouble avg = result.stream()
                .mapToDouble(Task::getWaitingTime)
                .average();

        avg.ifPresent(wt -> System.out.println("Average waiting time: " + wt));
    }

    public XYDataset getDataset() {
        XYSeries series = new XYSeries("Залежність");

//        for (int i = 0; i < 32; i++) {
//            series.add(i, getY(i)*getMult());
//        }

        for (int i = 0; i < 32; i++) {
            series.add(i, getAvgWaitingTimeByPriority(i));
        }

        results.forEach(series::add);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }

    private double getAvgWaitingTimeByPriority(int priority) {
        OptionalDouble avg = result.stream()
                .filter(task -> task.getPriority() == priority)
                .mapToDouble(Task::getWaitingTime)
                .average();

        return avg.orElse(0);
    }

    protected double stdDeviation = 2, variance = 4, mean = 16;

    public double getY(double x) {

        return Math.pow(Math.exp(-(((x - mean) * (x - mean)) / ((2 * variance)))), 1 / (stdDeviation * Math.sqrt(2 * Math.PI)));

    }

    public double getMult() {
        return 300 + ThreadLocalRandom.current().nextInt(0, 201);
    }
}

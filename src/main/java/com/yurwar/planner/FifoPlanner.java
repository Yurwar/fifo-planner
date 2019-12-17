package com.yurwar.planner;

import com.yurwar.entity.Task;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class FifoPlanner {
    private int currentTime;
    private List<Task> futureTasks;
    private Map<Integer, LinkedList<Task>> queues;
    private List<Task> finishedTasks;
    private int downtimeCount;

    public FifoPlanner(List<Task> tasks) {
        this.currentTime = 0;
        this.futureTasks = tasks;
        this.queues = new HashMap<>();
        this.finishedTasks = new ArrayList<>();
        this.downtimeCount = 0;

        this.refreshQueues();
    }

    public List<Task> execute() {
        while (futureTasks.size() > 0 || getPendingTasksCount() > 0) {
            Task currentTask = findNextTask();

            if (Objects.isNull(currentTask)) {
                currentTime++;
                incrementWaitingTimeForPendingTasks();
                refreshQueues();

                downtimeCount++;
                continue;
            } else {
                while (!currentTask.isFinished()) {
                    currentTask.giveTic();

                    currentTime++;
                    incrementWaitingTimeForPendingTasks();
                    refreshQueues();

                    if (isMorePriorityTaskExist(currentTask.getPriority())) {
                        break;
                    }
                }
            }

            if (!currentTask.isFinished()) {
                putTask(currentTask);
            } else {
                finishedTasks.add(currentTask);
            }
        }

        return finishedTasks;
    }

    private void refreshQueues() {
        List<Task> tasksToEvict = new ArrayList<>();
        futureTasks.stream()
                .filter(task -> task.getCreationTime() <= currentTime)
                .forEach(task -> {
                    LinkedList<Task> list = Optional
                            .ofNullable(queues
                                    .get(task.getPriority()))
                            .orElseGet(LinkedList::new);
                    list.add(task);

                    tasksToEvict.add(task);
                    queues.putIfAbsent(task.getPriority(), list);
                });

        futureTasks.removeAll(tasksToEvict);
    }

    private Task findNextTask() {
        return queues.entrySet().stream()
                .filter(integerListEntry -> !integerListEntry.getValue().isEmpty())
                .min(Comparator.comparingInt(Map.Entry::getKey))
                .map(integerListEntry -> integerListEntry.getValue().remove(0))
                .orElse(null);
    }

    private void putTask(Task task) {
        LinkedList<Task> list = Optional
                .ofNullable(queues
                        .get(task.getPriority()))
                .orElseGet(LinkedList::new);
        list.offerFirst(task);
        queues.putIfAbsent(task.getPriority(), list);
    }

    private long getPendingTasksCount() {
        return queues.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    private void incrementWaitingTimeForPendingTasks() {
        queues.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(Task::waitTic);
    }

    private boolean isMorePriorityTaskExist(int oldPriority) {
        return queues.entrySet().stream()
                .anyMatch(integerListEntry ->
                        integerListEntry.getKey() < oldPriority
                        &&
                        integerListEntry.getValue().size() > 0);
    }
}

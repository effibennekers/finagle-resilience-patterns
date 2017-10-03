package org.effiandeggie.jfall.task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Task extends Thread {

    private final int numberOfRuns;
    private final int taskId;
    private final Supplier<TaskResponse> executor;
    private final Consumer<TaskReport> statusReporter;


    public Task(int taskId, int numberOfRuns, Supplier<TaskResponse> executor, Consumer<TaskReport> statusReporter) {
        this.numberOfRuns = numberOfRuns;
        this.taskId = taskId;
        this.executor = executor;
        this.statusReporter = statusReporter;

    }

    @Override
    public void run() {
        System.err.println("started task # " + taskId);
        for (int i = 0; i < numberOfRuns; i++) {
            final long start = System.nanoTime();
            final TaskResponse response = executor.get();
            statusReporter.accept(new TaskReport(response.getInstance(),  (System.nanoTime() - start)/1000000, response.getHttpStatus()));
        }
        System.err.println("stopped task # " + taskId);
    }
}

package org.effiandeggie.jfall.task;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Task extends Thread {

    private final Supplier<TaskResponse> executor;
    private final Consumer<TaskReport> statusReporter;

    private boolean doRun;


    public Task(Supplier<TaskResponse> executor, Consumer<TaskReport> statusReporter) {
        this.executor = executor;
        this.statusReporter = statusReporter;
        doRun = true;

    }

    @Override
    public void run() {
        while (doRun) {
            final long start = System.nanoTime();
            final TaskResponse response = executor.get();
            statusReporter.accept(new TaskReport(response.getInstance(), (System.nanoTime() - start) / 1000000, response.getHttpStatus()));
        }
    }

    public void stopRunning() {
        doRun = false;
    }
}

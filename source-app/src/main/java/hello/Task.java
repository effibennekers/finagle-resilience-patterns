package hello;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Task extends Thread {

    private final int numberOfRuns;
    private final int taskId;
    private final Supplier<TaskResponse> executor;
    private final Consumer<Integer> statusReporter;


    public Task(int taskId, int numberOfRuns, Supplier<TaskResponse> executor, Consumer<Integer> statusReporter) {
        this.numberOfRuns = numberOfRuns;
        this.taskId = taskId;
        this.executor = executor;
        this.statusReporter = statusReporter;

    }

    @Override
    public void run() {
        System.err.println("started task # " + taskId);
        for (int i = 0; i < numberOfRuns; i++) {
            final TaskResponse response = executor.get();
            System.err.println("task #" + taskId + ": \"" + response.getResponse() + "\"");
            statusReporter.accept(response.getHttpStatus());
        }
        System.err.println("stopped task # " + taskId);
    }
}

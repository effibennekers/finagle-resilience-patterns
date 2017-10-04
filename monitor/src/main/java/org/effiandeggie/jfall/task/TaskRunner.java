package org.effiandeggie.jfall.task;


import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class TaskRunner {

    final List<Task> tasks = new LinkedList<>();

    protected abstract Supplier<TaskResponse> getExecutor();

    public TaskRunner(Consumer<TaskReport> reporter, TaskConfiguration taskConfiguration) {
        final Supplier<TaskResponse> executor = getExecutor();
        for (int i = 0; i < taskConfiguration.getNumberOfThreads(); i++) {
            tasks.add(new Task(executor, reporter));
        }
    }


    public void run() {
        tasks.forEach(Thread::start);
    }

    public void stop() {
        tasks.forEach(Task::stopRunning);
    }
}

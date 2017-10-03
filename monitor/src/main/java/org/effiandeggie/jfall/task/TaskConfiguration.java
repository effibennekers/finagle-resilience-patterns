package org.effiandeggie.jfall.task;

public class TaskConfiguration {
    private int numberOfRuns;
    private int numberOfThreads;

    public TaskConfiguration() {
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfRuns(final int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }

    public void setNumberOfThreads(final int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
}

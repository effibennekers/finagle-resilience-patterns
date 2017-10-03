package org.effiandeggie.jfall.task;

public class TaskReport {
    private final String instance;
    private long duration;
    private int httpStatus;

    public TaskReport(final String instance, final long duration, final int httpStatus) {
        this.instance = instance;
        this.duration = duration;
        this.httpStatus = httpStatus;
    }

    public String getInstance() {
        return instance;
    }

    public long getDuration() {
        return duration;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}

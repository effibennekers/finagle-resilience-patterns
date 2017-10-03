package org.effiandeggie.jfall.task;


public class TaskResponse {

    private final int httpStatus;
    private final String response;
    private final String instance;

    public TaskResponse(final int httpStatus, final String response, final String instance) {
        this.httpStatus = httpStatus;
        this.response = response;
        this.instance = instance;
    }


    public String getResponse() {
        return response;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
    public String getInstance() {
        return instance;
    }

}

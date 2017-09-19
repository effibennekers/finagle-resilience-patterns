package hello;


public class TaskResponse {

    private final String description;
    private final int httpStatus;
    private final String response;

    public TaskResponse(final String description, final int httpStatus, final String response) {
        this.description = description;
        this.httpStatus = httpStatus;
        this.response = response;
    }

    public String getDescription() {
        return description;
    }

    public String getResponse() {
        return response;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}

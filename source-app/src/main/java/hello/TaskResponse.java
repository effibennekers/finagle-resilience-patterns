package hello;


public class TaskResponse {

    private final int httpStatus;
    private final String response;

    public TaskResponse(final int httpStatus, final String response) {
        this.httpStatus = httpStatus;
        this.response = response;
    }

    public String getResponse() {
        return response;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}

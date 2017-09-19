package hello;

import org.apache.http.client.methods.HttpGet;

public class HttpClientScenario {

    private final String description;
    private final HttpGet get;

    public HttpClientScenario(final String description, final HttpGet get) {
        this.description = description;
        this.get = get;
    }

    public String getDescription() {
        return description;
    }

    public HttpGet getGet() {
        return get;
    }
}

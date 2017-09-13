package hello;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;

public class Task extends Thread {

    private final CloseableHttpClient httpClient;
    private final int numberOfRuns;
    private final HttpGet httpGet = new HttpGet("http://localhost:8080/greeting");

    public Task(final CloseableHttpClient httpClient, int numberOfRuns) {
        this.httpClient = httpClient;
        this.numberOfRuns = numberOfRuns;
    }

    @Override
    public void run() {
        for (int i = 0; i < numberOfRuns; i++) {
            try {
                CloseableHttpResponse response1 = httpClient.execute(httpGet);
                final ObjectMapper mapper = new ObjectMapper();
                final Greeting greeting = mapper.readValue(response1.getEntity().getContent(), Greeting.class);
                System.err.println(greeting);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

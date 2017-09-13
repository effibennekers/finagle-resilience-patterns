package hello;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.util.Random;

public class Task extends Thread {

    private final CloseableHttpClient httpClient;
    private final int numberOfRuns;
    private final HttpGet httpGet = new HttpGet("http://localhost:8080/greeting");

    private final Random random = new Random();

    public Task(final CloseableHttpClient httpClient, int numberOfRuns) {
        this.httpClient = httpClient;
        this.numberOfRuns = numberOfRuns;
    }

    @Override
    public void run() {
        for (int i = 0; i < numberOfRuns; i++) {
            try {
                CloseableHttpResponse response1 = httpClient.execute(httpGet);
                System.err.println(IOUtils.toString(response1.getEntity().getContent(), "UTF-8"));
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

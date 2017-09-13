package hello;


import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SourceApplication {

    private static final int NUMBER_OF_THREADS = 200;
    private static final int NUMBER_OF_STEPS = 1000;

    public static void main(String[] args) {

        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(2);
        cm.setDefaultMaxPerRoute(2);
        final HttpHost localhost = new HttpHost("locahost", 80);
        cm.setMaxPerRoute(new HttpRoute(localhost), 5);


        try (
                CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        ) {
            final List<Task> tasks = new LinkedList<>();
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                tasks.add(new Task(httpClient, NUMBER_OF_STEPS));
            }

            tasks.forEach(t -> t.start());
            tasks.forEach(t -> {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

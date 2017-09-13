package hello;


import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SourceApplication {

    private static final int NUMBER_OF_THREADS = 1;
    private static final int NUMBER_OF_STEPS = 100;

    public static void main(String[] args) {

        MultiThreadedHttpConnectionManager connectionManager =
                new MultiThreadedHttpConnectionManager();
        HttpClient client = new HttpClient(connectionManager);
        try
                (CloseableHttpClient httpclient = HttpClients.createDefault()
                ) {
            final List<Task> tasks = new LinkedList<>();
            for (int i = 0; i < NUMBER_OF_THREADS; i++) {
                tasks.add(new Task(httpclient, NUMBER_OF_STEPS));
            }

            tasks.forEach(t -> t.start());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

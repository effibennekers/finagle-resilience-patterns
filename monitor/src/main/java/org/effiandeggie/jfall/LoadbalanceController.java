package org.effiandeggie.jfall;


import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.effiandeggie.jfall.task.TaskConfiguration;
import org.effiandeggie.jfall.task.TaskResponse;
import org.effiandeggie.jfall.task.TaskRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.IOException;
import java.security.SecureRandom;

import static java.lang.String.format;

@Controller
public class LoadbalanceController {
    private static final int EFFIE_PORT = 8081;
    private static final int EGGIE_PORT = 8082;
    final SecureRandom random = new SecureRandom();
    private MessageSendingOperations<String> messagingTemplate;


    @Autowired
    public LoadbalanceController(final MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    @PostMapping(value = "/loadbalancing", consumes = "application/json")
    public ResponseEntity<?> testLoadbalancingWithApacheHttpClient(@RequestBody final TaskConfiguration taskConfiguration) {
        System.err.println ("task conf. threads" + taskConfiguration.getNumberOfThreads());
        System.err.println ("task conf. runs" + taskConfiguration.getNumberOfRuns());
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(2);

        final HttpHost effieHost = new HttpHost("locahost", EFFIE_PORT);
        cm.setMaxPerRoute(new HttpRoute(effieHost), 200);
        final HttpHost eggieHost = new HttpHost("locahost", EGGIE_PORT);
        cm.setMaxPerRoute(new HttpRoute(eggieHost), 200);
        final HttpGet getFromEffi = new HttpGet(format("http://localhost:%d/loadbalancing", EFFIE_PORT));
        final HttpGet getFromEggie = new HttpGet(format("http://localhost:%d/loadbalancing", EGGIE_PORT));

        final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        final TaskRunner taskRunner = new TaskRunner(() -> {
            final HttpGet get;
            final String instance;
            if (random.nextBoolean()) {
                get = getFromEffi;
                instance = "effi";
            } else {
                get = getFromEggie;
                instance = "eggie";
            }
            try (CloseableHttpResponse response1 = httpClient.execute(get)) {
                return new TaskResponse(response1.getStatusLine().getStatusCode(), IOUtils.toString(response1.getEntity().getContent(), "UTF-8"),
                        instance);
            } catch (IOException e) {
                return new TaskResponse(500, e.getMessage(), instance);
            }
        }, report -> {
            messagingTemplate.convertAndSend("/topic/loadbalancing", report);
        }, taskConfiguration);
        taskRunner.run();
        return ResponseEntity.accepted().build();
    }
}

package org.effiandeggie.jfall.task;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;

public class AppacheLoadbalanceTaskRunner extends TaskRunner {
    private final HttpGet getFromEffi;
    private final HttpGet getFromEggie;
    private final CloseableHttpClient httpClient;

    private static final int EFFIE_PORT = 8081;
    private static final int EGGIE_PORT = 8082;
    final SecureRandom random = new SecureRandom();

    @Override
    protected Supplier<TaskResponse> getExecutor() {
        return () -> {
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
        };
    }

    public AppacheLoadbalanceTaskRunner(final Consumer<TaskReport> reporter, final TaskConfiguration taskConfiguration) {
        super(reporter, taskConfiguration);
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(taskConfiguration.getNumberOfThreads());
        cm.setDefaultMaxPerRoute(2);

        final HttpHost effieHost = new HttpHost("locahost", EFFIE_PORT);
        cm.setMaxPerRoute(new HttpRoute(effieHost), 200);
        final HttpHost eggieHost = new HttpHost("locahost", EGGIE_PORT);
        cm.setMaxPerRoute(new HttpRoute(eggieHost), 200);
        getFromEffi = new HttpGet(format("http://localhost:%d/loadbalancing", EFFIE_PORT));
        getFromEggie = new HttpGet(format("http://localhost:%d/loadbalancing", EGGIE_PORT));

        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }
}

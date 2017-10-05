package org.effiandeggie.jfall.task;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.effiandeggie.jfall.Instance;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.String.format;

public class AppacheLoadbalanceTaskRunner extends TaskRunner {

    private final Instance[] instances;


    private final CloseableHttpClient httpClient;

    final SecureRandom random = new SecureRandom();
    private final HttpGet[] getRequests;

    @Override
    protected Supplier<TaskResponse> getExecutor() {
        return () -> {
            final int selectedInstance = random.nextInt(instances.length);
            final HttpGet get = getRequests[selectedInstance];
            final String instanceName = instances[selectedInstance].getName();
            try (CloseableHttpResponse response1 = httpClient.execute(get)) {
                return new TaskResponse(response1.getStatusLine().getStatusCode(), IOUtils.toString(response1.getEntity().getContent(), "UTF-8"),
                        instanceName);
            } catch (IOException e) {
                return new TaskResponse(500, e.getMessage(), instanceName);
            }
        };
    }

    public AppacheLoadbalanceTaskRunner(final Consumer<TaskReport> reporter, final TaskConfiguration taskConfiguration, final Instance[] instances) {
        super(reporter, taskConfiguration);
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(taskConfiguration.getNumberOfThreads() * instances.length);
        cm.setDefaultMaxPerRoute(taskConfiguration.getNumberOfThreads());

        getRequests = new HttpGet[instances.length];
        for (int i = 0; i < instances.length; i++) {
            final Instance instance = instances[i];
            final HttpHost host = new HttpHost("locahost", instance.getPort());
            cm.setMaxPerRoute(new HttpRoute(host), taskConfiguration.getNumberOfThreads());
            getRequests[i] = new HttpGet(format("http://localhost:%d/loadbalancing", instance.getPort()));
        }

        httpClient = HttpClients.custom().setConnectionManager(cm).build();
        this.instances = instances;
    }
}

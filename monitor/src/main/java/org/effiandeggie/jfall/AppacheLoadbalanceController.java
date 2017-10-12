package org.effiandeggie.jfall;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

@RestController
public class AppacheLoadbalanceController {

    private static final int MAX_LOAD = 10 ;
    private HttpGet[] getRequests;
    private CloseableHttpClient httpClient;

    private Instance[] instances;

    private SecureRandom random = new SecureRandom();

    @Autowired
    public AppacheLoadbalanceController(@Qualifier("instances") final Instance[] instances) {
        this.instances = instances;
    }

    @PostConstruct
    public void init() {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_LOAD * instances.length);
        cm.setDefaultMaxPerRoute(MAX_LOAD);

        getRequests = new HttpGet[instances.length];
        for (int i = 0; i < instances.length; i++) {
            final Instance instance = instances[i];
            final HttpHost host = new HttpHost(instance.getHost(), instance.getPort());
            cm.setMaxPerRoute(new HttpRoute(host), MAX_LOAD);
            getRequests[i] = new HttpGet(format("http://%s:%d/weather", instance.getHost(), instance.getPort()));
        }

        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    @GetMapping(value = "/api/apache/loadbalancing")
    public CompletableFuture<String> loadbalance(final HttpServletResponse httpServletResponse) {
        final int selectedInstance = random.nextInt(instances.length);
        final HttpGet get = getRequests[selectedInstance];
        final String instanceName = instances[selectedInstance].getName();
        httpServletResponse.setHeader("instance", instanceName);
        final CompletableFuture<String> response = new CompletableFuture<>();

        try (CloseableHttpResponse response1 = httpClient.execute(get)) {
            response.complete(IOUtils.toString(response1.getEntity().getContent(), "UTF-8"));
        } catch (IOException e) {
            response.completeExceptionally(e);
        }
        return response;
    }
}

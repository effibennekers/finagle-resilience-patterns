package org.effiandeggie.jfall;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import static java.lang.String.format;

public class BaseApacheController {

    private static final int MAX_LOAD = 10;
    protected final CloseableHttpClient httpClient;

    public BaseApacheController(final Instance... instances) {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_LOAD * instances.length);
        cm.setDefaultMaxPerRoute(MAX_LOAD);

        for (int i = 0; i < instances.length; i++) {
            final Instance instance = instances[i];
            final HttpHost host = new HttpHost(instance.getHost(), instance.getPort());
            cm.setMaxPerRoute(new HttpRoute(host), MAX_LOAD);
        }

        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    public HttpGet createGetRequest(final Instance instance) {
        return new HttpGet(format("http://%s:%d/weather", instance.getHost(), instance.getPort()));
    }
}

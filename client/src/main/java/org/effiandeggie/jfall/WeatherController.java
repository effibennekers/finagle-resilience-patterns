package org.effiandeggie.jfall;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.effiandeggie.jfall.instances.Instance;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.format;

@RestController
public class WeatherController {

    private static final int MAX_LOAD = 10;
    private final CloseableHttpClient httpClient;
    private final String hostName;
    private final int port;


    @Autowired
    public WeatherController(final InstanceManager instanceManager) {

        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(MAX_LOAD);
        cm.setDefaultMaxPerRoute(MAX_LOAD);
        hostName = "weather1";
        port = 8080;

        final HttpHost host = new HttpHost(hostName, port);
        cm.setMaxPerRoute(new HttpRoute(host), MAX_LOAD);

        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    @GetMapping(value = "/api/weather")
    public ResponseEntity<String> loadbalance(final HttpServletResponse httpServletResponse) {
        final HttpGet get = new HttpGet(format("http://%s:%d/weather", hostName, port));
        httpServletResponse.setHeader("instance", hostName);
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                final String data = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                return ResponseEntity.ok(data);
            } else {
                return ResponseEntity.status(statusCode).build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
}

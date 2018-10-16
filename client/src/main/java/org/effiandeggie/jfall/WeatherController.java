package org.effiandeggie.jfall;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.effiandeggie.jfall.instances.Instance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.lang.String.format;

@RestController
public class WeatherController {

    private final Instance instance;
    private CloseableHttpClient httpClient;

    private static final int TIMEOUT_IN_MILISECIONDS = 1500;

    public WeatherController(Instance[] instances) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(10);
        cm.setDefaultMaxPerRoute(10);
        instance = instances[0];

        HttpHost host = new HttpHost(instance.getHost(), instance.getPort());
        cm.setMaxPerRoute(new HttpRoute(host), 10);
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_IN_MILISECIONDS)
                .setConnectionRequestTimeout(TIMEOUT_IN_MILISECIONDS)
                .setConnectionRequestTimeout(TIMEOUT_IN_MILISECIONDS).build();


        httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(cm).build();
    }

    @GetMapping(value = "/api/weather")
    public ResponseEntity<String> loadbalance(HttpServletResponse httpServletResponse) {
        HttpGet get = new HttpGet(format("http://%s:%d/weather", instance.getHost(), instance.getPort()));
        httpServletResponse.setHeader("instance", instance.getName());
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return ResponseEntity.ok(getContentString(response));
            } else {
                return ResponseEntity.status(statusCode).build();
            }
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }

    private String getContentString(final CloseableHttpResponse response) throws IOException {
        return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
    }
}

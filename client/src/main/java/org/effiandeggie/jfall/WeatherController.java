package org.effiandeggie.jfall;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class WeatherController {

    private CloseableHttpClient httpClient;

    public WeatherController() {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(10);
        cm.setDefaultMaxPerRoute(10);

        HttpHost host = new HttpHost("weather1", 8080);
        cm.setMaxPerRoute(new HttpRoute(host), 10);

        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    @GetMapping(value = "/api/weather")
    public ResponseEntity<String> loadbalance(HttpServletResponse httpServletResponse) {
        HttpGet get = new HttpGet("http://weather1:8080/weather");
        httpServletResponse.setHeader("instance", "weather1");
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

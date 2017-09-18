package hello;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class LoadBalanceTest extends BaseTest {

    @Test
    public void testLoadbalancingWithHttpClient() {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(2);
        final HttpHost localhost = new HttpHost("locahost", 80);
        cm.setMaxPerRoute(new HttpRoute(localhost), 200);
        final HttpGet httpGet1 = new HttpGet("http://localhost:8080/loadbalancing");
        final HttpGet httpGet2 = new HttpGet("http://localhost:8081/loadbalancing");
        final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();
        final Random random = new Random();
        runTask(() ->
        {
            try (CloseableHttpResponse response1 = httpClient.execute(random.nextBoolean()? httpGet1: httpGet2)) {
                return new TaskResponse(response1.getStatusLine().getStatusCode() , IOUtils.toString(response1.getEntity().getContent(), "UTF-8"));
            } catch (IOException e) {
                return new TaskResponse(500, e.getMessage());
            }

        });

    }
}

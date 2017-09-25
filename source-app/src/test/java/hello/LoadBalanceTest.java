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
import java.security.SecureRandom;

public class LoadBalanceTest extends BaseTest {

    @Test
    public void testLoadbalancingWithApacheHttpClient() {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(2);
        final HttpHost effieHost = new HttpHost("locahost", 8080);
        cm.setMaxPerRoute(new HttpRoute(effieHost), 200);
        final HttpHost eggieHost = new HttpHost("locahost", 8081);
        cm.setMaxPerRoute(new HttpRoute(eggieHost), 200);
        final HttpGet getFromEffi = new HttpGet("http://localhost:8080/loadbalancing");
        final HttpGet getFromEggie = new HttpGet("http://localhost:8081/loadbalancing");

        final CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(cm).build();

        final SecureRandom random = new SecureRandom();
        runTask(() -> {
            final HttpGet get = random.nextBoolean() ? getFromEffi : getFromEggie;
            try (CloseableHttpResponse response1 = httpClient.execute(get)) {
                return new TaskResponse(response1.getStatusLine().getStatusCode(), IOUtils.toString(response1.getEntity().getContent(), "UTF-8"));
            } catch (IOException e) {
                return new TaskResponse(500, e.getMessage());
            }

        });
    }

}

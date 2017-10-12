package org.effiandeggie.jfall;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.CompletableFuture;

@RestController
public class ApacheLoadbalanceController extends BaseAppacheController {

    private Instance[] instances;

    private SecureRandom random = new SecureRandom();

    @Autowired
    public ApacheLoadbalanceController(@Qualifier("instances") final Instance[] instances) {
        super(instances);
        this.instances = instances;
    }

    @GetMapping(value = "/api/apache/loadbalancing")
    public CompletableFuture<String> loadbalance(final HttpServletResponse httpServletResponse) {
        final Instance selectedInstance = instances[random.nextInt(instances.length)];
        final HttpGet get = createGetRequest(selectedInstance);
        httpServletResponse.setHeader("instance", selectedInstance.getName());
        final CompletableFuture<String> futureResponse = new CompletableFuture<>();
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            futureResponse.complete(IOUtils.toString(response.getEntity().getContent(), "UTF-8"));
        } catch (IOException e) {
            futureResponse.completeExceptionally(e);
        }
        return futureResponse;
    }
}

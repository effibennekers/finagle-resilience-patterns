package org.effiandeggie.jfall;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> loadbalance(final HttpServletResponse httpServletResponse) {
        final Instance selectedInstance = instances[random.nextInt(instances.length)];
        final HttpGet get = createGetRequest(selectedInstance);
        httpServletResponse.setHeader("instance", selectedInstance.getName());
        final CompletableFuture<ResponseEntity<String>> futureResponse = new CompletableFuture<>();
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

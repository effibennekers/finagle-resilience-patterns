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
import java.util.concurrent.CompletableFuture;

@RestController
public class ApacheFailoverController extends BaseAppacheController {


    private final Instance primaryInstance;
    private final Instance secondaryInstance;

    @Autowired
    public ApacheFailoverController(@Qualifier("primaryInstance") final Instance primaryInstance, @Qualifier("secondaryInstance") final Instance secondaryInstance) {
        super(primaryInstance, secondaryInstance);
        this.primaryInstance = primaryInstance;
        this.secondaryInstance = secondaryInstance;
    }

    @GetMapping(value = "/api/apache/failover")
    public CompletableFuture<ResponseEntity<String>> failover(final HttpServletResponse httpServletResponse) {
        final HttpGet get = createGetRequest(primaryInstance);
        final CompletableFuture<ResponseEntity<String>> futureResponse = new CompletableFuture<>();

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                httpServletResponse.setHeader("instance", primaryInstance.getName());
                final String data = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                futureResponse.complete(ResponseEntity.ok(data));
            } else {
                failover(futureResponse, httpServletResponse);
            }
        } catch (IOException e) {
            failover(futureResponse, httpServletResponse);
        }
        return futureResponse;
    }

    private void failover(final CompletableFuture<ResponseEntity<String>> futureResponse, final HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("instance", secondaryInstance.getName());
        final HttpGet get = createGetRequest(secondaryInstance);
        try (CloseableHttpResponse response = httpClient.execute(get)) {
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                final String data = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                futureResponse.complete(ResponseEntity.ok(data));
            } else {
                futureResponse.complete(ResponseEntity.status(statusCode).build());
            }
        } catch (IOException e) {
            futureResponse.complete(ResponseEntity.status(500).build());
        }
    }
}

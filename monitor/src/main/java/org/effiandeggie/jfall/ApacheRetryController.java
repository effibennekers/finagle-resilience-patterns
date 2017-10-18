package org.effiandeggie.jfall;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
public class ApacheRetryController extends BaseAppacheController {


    private static final long MAX_DURATION = 1000_000_000;
    private static final String ENCODING = "UTF-8";
    private final Instance primaryInstance;

    @Autowired
    public ApacheRetryController(@Qualifier("primaryInstance") final Instance primaryInstance) {
        super(primaryInstance);
        this.primaryInstance = primaryInstance;
    }

    @GetMapping(value = "/api/apache/retry")
    public ResponseEntity<String> retry(final HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("instance", primaryInstance.getName());
        final CompletableFuture<ResponseEntity<String>> futureResponse = tryOverall();
        try {
            return futureResponse.get(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(500).build();

        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
    }

    private CompletableFuture<ResponseEntity<String>> tryOverall() {
        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                final CompletableFuture<ResponseEntity<String>> futureResponse = tryRequest();
                try {
                    ResponseEntity<String> response = futureResponse.get(60, TimeUnit.MILLISECONDS);
                    if (response.getStatusCode().value() == 200) {
                        return response;
                    } else {
                        futureResponse.cancel(true);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    return ResponseEntity.status(500).build();

                } catch (TimeoutException e) {
                    futureResponse.cancel(true);
                }
            }
        });
    }

    private CompletableFuture<ResponseEntity<String>> tryRequest() {
        final HttpGet get = createGetRequest(primaryInstance);

        return CompletableFuture.supplyAsync(() -> {
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
        });
    }
}

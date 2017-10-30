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

@RestController
public class ApacheFailoverController extends BaseApacheController {


    private final Instance primaryInstance;
    private final Instance secondaryInstance;

    @Autowired
    public ApacheFailoverController(@Qualifier("primaryInstance") final Instance primaryInstance, @Qualifier("secondaryInstance") final Instance secondaryInstance) {
        super(primaryInstance, secondaryInstance);
        this.primaryInstance = primaryInstance;
        this.secondaryInstance = secondaryInstance;
    }

    @GetMapping(value = "/api/apache/failover")
    public ResponseEntity<String> getFailover(final HttpServletResponse httpServletResponse) {
        final HttpGet get = createGetRequest(primaryInstance);

        try (CloseableHttpResponse response = httpClient.execute(get)) {
            if (response.getStatusLine().getStatusCode() == 200) {
                httpServletResponse.setHeader("instance", primaryInstance.getName());
                final String data = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
                return ResponseEntity.ok(data);
            } else {
                return failover(httpServletResponse);
            }
        } catch (IOException e) {
            return failover(httpServletResponse);
        }
    }

    private ResponseEntity<String> failover(final HttpServletResponse httpServletResponse) {
        httpServletResponse.setHeader("instance", secondaryInstance.getName());
        final HttpGet get = createGetRequest(secondaryInstance);
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

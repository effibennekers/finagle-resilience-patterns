package org.effiandeggie.jfall;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Duration;
import com.twitter.util.Future;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
public class FinagleRetryController {

    private Instance primaryInstance;


    private Service<Request, Response> primaryClient;

    @Autowired
    public FinagleRetryController(@Qualifier("primaryInstance") final Instance primaryInstance) {
        this.primaryInstance = primaryInstance;
    }

    @PostConstruct
    public void init() {
        primaryClient = HostFilter$.MODULE$.client()
                .withSession().acquisitionTimeout(Duration.fromMilliseconds(1500))
                .withRequestTimeout(Duration.fromMilliseconds(60))
                .newService(primaryInstance.getHost() + ":" + primaryInstance.getPort(), "retry");
    }

    @GetMapping("/api/finagle/retry")
    public CompletableFuture<ResponseEntity<String>> getRetry(final HttpServletResponse httpServletResponse) {
        final Request primaryRequest = Request.apply(Method.Get(), "/weather");
        httpServletResponse.setHeader("instance", primaryInstance.getName());
        primaryRequest.host("localhost");
        final Future<Response> futureResponse = primaryClient.apply(primaryRequest);

        return FutureUtil.toJavaFuture(futureResponse.map(
                response -> {
                    if (response.getStatusCode() == 200) {
                        return ResponseEntity.ok(response.getContentString());
                    } else {
                        return ResponseEntity.status(response.getStatusCode()).build();
                    }
                }));
    }
}

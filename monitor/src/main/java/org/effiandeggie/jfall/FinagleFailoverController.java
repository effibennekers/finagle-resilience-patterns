package org.effiandeggie.jfall;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import com.twitter.util.Try;
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
public class FinagleFailoverController {

    private Instance primaryInstance;

    private Instance secondaryInstance;

    private Service<Request, Response> primaryClient;
    private Service<Request, Response> secondaryClient;

    @Autowired
    public FinagleFailoverController(@Qualifier("primaryInstance") final Instance primaryInstance,
                                     @Qualifier("secondaryInstance") final Instance secondaryInstance) {
        this.primaryInstance = primaryInstance;
        this.secondaryInstance = secondaryInstance;

    }

    @PostConstruct
    public void init() {
        primaryClient = HostFilter$.MODULE$.client()
                .withSessionQualifier().noFailFast()
                .newService(primaryInstance.getHost() + ":" + primaryInstance.getPort(), "primary");
        secondaryClient = HostFilter$.MODULE$.client().
                newService(secondaryInstance.getHost() + ":" + secondaryInstance.getPort(), "secondary");
    }

    @GetMapping("/api/finagle/failover")
    public CompletableFuture<ResponseEntity<String>> getFailover(final HttpServletResponse httpServletResponse) {
        final Request primaryRequest = Request.apply(Method.Get(), "/weather");
        primaryRequest.host("localhost");
        final Future<Try<Response>> tryableFutureResponse = primaryClient.apply(primaryRequest).liftToTry();
        final Future<Response> futureResponse = tryableFutureResponse.flatMap(tryResponse -> {
            if (isValidRequest(tryResponse)) {
                httpServletResponse.setHeader("instance", primaryInstance.getName());
                return Future.value(tryResponse.get());
            } else {
                httpServletResponse.setHeader("instance", secondaryInstance.getName());
                final Request secondaryRequest = Request.apply(Method.Get(), "/weather");
                secondaryRequest.host("localhost");
                return secondaryClient.apply(secondaryRequest);
            }
        });

        return FutureUtil.toJavaFuture(futureResponse.map(
                response -> {
                    if (response.getStatusCode() == 200) {
                        return ResponseEntity.ok(response.getContentString());
                    } else {
                        return ResponseEntity.status(response.getStatusCode()).build();
                    }
                }));
    }

    private boolean isValidRequest(final Try<Response> tryResponse) {
        return tryResponse.isReturn() && tryResponse.get().getStatusCode() == 200;
    }

}

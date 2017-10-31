package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.effiandeggie.jfall.utils.FutureUtil;
import org.effiandeggie.jfall.instances.Instance;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
public class FinagleFailoverController extends BaseFinagleController {

    private Instance[] primaryInstances;

    private Instance secondaryInstance;

    private Service<Request, Response> primaryClient;
    private Service<Request, Response> secondaryClient;

    @Autowired
    public FinagleFailoverController(final InstanceManager instanceManager) {
        super(instanceManager);
        this.primaryInstances = instanceManager.getPrimaryInstances();
        this.secondaryInstance = instanceManager.getSecondaryInstances()[0];
    }

    @PostConstruct
    public void init() {
        primaryClient = HostFilter$.MODULE$.client()
                .withSessionQualifier().noFailFast()
                .newService(instancesToConnectionString(primaryInstances), "primary");
        secondaryClient = HostFilter$.MODULE$.client().
                newService(instancesToConnectionString(secondaryInstance), "secondary");
    }

    @GetMapping("/api/finagle/failover")
    public CompletableFuture<ResponseEntity<String>> getFailover(final HttpServletResponse httpServletResponse) {
        final Request primaryRequest = Request.apply(Method.Get(), "/weather");
        primaryRequest.host("localhost");
        final Future<Try<Response>> tryableFutureResponse = primaryClient.apply(primaryRequest).liftToTry();
        final Future<Response> futureResponse = tryableFutureResponse.flatMap(tryResponse -> {
            if (isValidResponse(tryResponse)) {
                setHeadersForDemo(httpServletResponse, tryResponse.get());
                return Future.value(tryResponse.get());
            } else {
                setHeadersForDemo(httpServletResponse, secondaryInstance);
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

    private boolean isValidResponse(final Try<Response> tryResponse) {
        return tryResponse.isReturn() && tryResponse.get().getStatusCode() == 200;
    }

}

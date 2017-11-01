package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
public class FinagleFailoverController extends BaseFinagleController {

    private Service<Request, Response> primaryClient;
    private Service<Request, Response> secondaryClient;

    @Autowired
    public FinagleFailoverController() {
        String primaryConnectionString = "weather1:8080,weather2:8080";
        primaryClient = HostFilter$.MODULE$.client()
                .withSessionQualifier().noFailFast()
                .newService(primaryConnectionString, "primary");

        String secondaryConnectionString = "oldweather:8080";
        secondaryClient = HostFilter$.MODULE$.client().newService(secondaryConnectionString, "secondary");
    }

    @GetMapping("/api/finagle/failover")
    public CompletableFuture<ResponseEntity<String>> getFailover(HttpServletResponse httpServletResponse) {
        Request primaryRequest = Request.apply(Method.Get(), "/weather");
        primaryRequest.host("localhost");

        Future<Try<Response>> tryableFutureResponse = primaryClient.apply(primaryRequest).liftToTry();
        Future<Response> futureResponse = tryableFutureResponse.flatMap(tryResponse -> {
            if (isValidResponse(tryResponse)) {
                return Future.value(tryResponse.get());
            } else {
                Request secondaryRequest = Request.apply(Method.Get(), "/weather");
                secondaryRequest.host("localhost");
                return secondaryClient.apply(secondaryRequest);
            }
        });

        return toSpringResponse(futureResponse, httpServletResponse);
    }

    private boolean isValidResponse(Try<Response> tryResponse) {
        return tryResponse.isReturn() && tryResponse.get().getStatusCode() == 200;
    }
}

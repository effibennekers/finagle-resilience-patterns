package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
public class FinagleFailoverController extends BaseFinagleController {

    private Service<Request, Response> primaryClient;
    private Service<Request, Response> secondaryClient;

    public FinagleFailoverController() {
        primaryClient = HostFilter$.MODULE$.client()
                .withSessionQualifier().noFailFast()
                .newService("weather1:8080,weather2:8080", "primary");

        secondaryClient = HostFilter$.MODULE$.client().newService("oldweather:8080", "secondary");
    }

    @GetMapping("/api/finagle/failover")
    public CompletableFuture<ResponseEntity<String>> getFailover(HttpServletResponse httpServletResponse) {
        Request primaryRequest = createRequest();

        Future<Try<Response>> tryableFutureResponse = primaryClient.apply(primaryRequest).liftToTry();
        Future<Response> futureResponse = tryableFutureResponse.flatMap(tryResponse -> {
            if (isValidResponse(tryResponse)) {
                return Future.value(tryResponse.get());
            } else {
                Request secondaryRequest = createRequest();
                return secondaryClient.apply(secondaryRequest);
            }
        });

        return toSpringResponse(futureResponse, httpServletResponse);
    }

    private Request createRequest() {
        Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");
        return request;
    }

    private boolean isValidResponse(Try<Response> tryResponse) {
        return tryResponse.isReturn() && tryResponse.get().getStatusCode() == 200;
    }
}

package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Function;
import com.twitter.util.Future;
import org.effiandeggie.finagle.filters.Http$;
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
        primaryClient = Http$.MODULE$.client()
                .withSessionQualifier().noFailFast()
                .newService("weather1:8080,weather2:8080", "primary");

        secondaryClient = Http$.MODULE$.client().newService("oldweather:8080", "secondary");
    }

    @GetMapping("/api/finagle/failover")
    public CompletableFuture<ResponseEntity<String>> getFailover(HttpServletResponse httpServletResponse) {
        Request request = createRequest();

        Future<Response> primaryFutureResponse = primaryClient.apply(request).rescue(
                new Function<Throwable, Future<Response>>() {
                    @Override
                    public Future<Response> apply(Throwable v1) {
                        return secondaryClient.apply(request);
                    }
                }
        );
        Future<Response> futureResponse = primaryFutureResponse.flatMap(primaryResponse -> {
            if (primaryResponse.getStatusCode() == 200) {
                return Future.value(primaryResponse);
            } else {
                return secondaryClient.apply(request);
            }
        });

        return toSpringResponse(futureResponse, httpServletResponse);
    }

    private Request createRequest() {
        Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");
        return request;
    }
}

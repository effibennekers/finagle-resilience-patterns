package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Function;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.clients.Http;
import org.effiandeggie.jfall.instances.Instance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

import static org.effiandeggie.jfall.instances.InstanceManager.connectionString;

@RestController
public class FinagleFailoverController extends BaseFinagleController {

    private Service<Request, Response> primaryClient;
    private Service<Request, Response> secondaryClient;


    public FinagleFailoverController(Instance[] instances) {
        primaryClient = Http.client()
                .newService(connectionString(instances[0], instances[1]), "primary");

        secondaryClient = Http.client().newService(connectionString(instances[2]), "secondary");
    }

    private static <A, B> Function<A, B> partialFunction(java.util.function.Function<A, B> lambda) {
        return new Function<A, B>() {
            @Override
            public B apply(A a) {
                return lambda.apply(a);
            }
        };

    }

    @GetMapping("/api/finagle/failover")
    public CompletableFuture<ResponseEntity<String>> getFailover(HttpServletResponse httpServletResponse) {
        Request request = createRequest();

        Future<Response> futureResponse =
                primaryClient.apply(request).rescue(partialFunction(x -> secondaryClient.apply(request)))
                        .flatMap(primaryResponse -> {
                                    if (primaryResponse.getStatusCode() == 200) {
                                        return Future.value(primaryResponse);
                                    } else {
                                        return secondaryClient.apply(request);
                                    }
                                }
                        );
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

package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.service.RetryBudget;
import com.twitter.finagle.service.RetryFilter;
import com.twitter.finagle.service.SimpleRetryPolicy;
import com.twitter.finagle.stats.NullStatsReceiver;
import com.twitter.finagle.util.DefaultTimer;
import com.twitter.util.Duration;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.clients.Http;
import org.effiandeggie.jfall.instances.Instance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.Tuple2;

import java.util.concurrent.CompletableFuture;

import static org.effiandeggie.jfall.instances.InstanceManager.connectionString;

@RestController
public class FinagleRetryController extends BaseFinagleController {


    private Service<Request, Response> client;

    public FinagleRetryController(Instance[] instances) {
        RetryFilter<Request, Response> retryFilter =
                new RetryFilter<Request, Response>(createRetryPolicy(),
                        DefaultTimer.getInstance(),
                        NullStatsReceiver.get(),
                        RetryBudget.apply());

        client = retryFilter.andThen(Http.client().newService(connectionString(instances[0]), "retry"));
    }

    private SimpleRetryPolicy<Tuple2<Request, Try<Response>>> createRetryPolicy() {
        return new SimpleRetryPolicy<Tuple2<Request, Try<Response>>>() {
            public Duration backoffAt(int retry) {
                return Duration.fromMilliseconds(retry * 10);
            }

            public boolean shouldRetry(Tuple2<Request, Try<Response>> requestTryResponse) {
                Try<Response> tryResponse = requestTryResponse._2;
                return tryResponse.isReturn() && tryResponse.get().getStatusCode() == 503;
            }
        };
    }

    @GetMapping("/api/finagle/retry")
    public CompletableFuture<ResponseEntity<String>> getRetry() {
        Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");

        Future<Response> futureResponse = client.apply(request);
        return toSpringResponse(futureResponse);
    }
}

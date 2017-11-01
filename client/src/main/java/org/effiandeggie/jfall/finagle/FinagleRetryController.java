package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.service.RetryBudget$;
import com.twitter.finagle.service.RetryFilter;
import com.twitter.finagle.service.SimpleRetryPolicy;
import com.twitter.finagle.stats.NullStatsReceiver$;
import com.twitter.finagle.util.DefaultTimer$;
import com.twitter.util.Duration;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.Tuple2;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
public class FinagleRetryController extends BaseFinagleController {


    private Service<Request, Response> client;

    public FinagleRetryController() {
        RetryFilter<Request, Response> retryFilter =
                new RetryFilter<>(createPolicy(),
                        DefaultTimer$.MODULE$.twitter(), NullStatsReceiver$.MODULE$,
                        RetryBudget$.MODULE$.apply());

        client = retryFilter.andThen(HostFilter$.MODULE$.client().newService("weather1:8080", "retry"));
    }

    private SimpleRetryPolicy<Tuple2<Request, Try<Response>>> createPolicy() {
        return new SimpleRetryPolicy<Tuple2<Request, Try<Response>>>() {
            public Duration backoffAt(int retry) {
                return Duration.fromMilliseconds(retry * 10);
            }

            public boolean shouldRetry(Tuple2<Request, Try<Response>> requestTryResponse) {
                Try<Response> tryResponse = requestTryResponse._2;
                return tryResponse.isReturn() && tryResponse.get().getStatusCode() == 404;
            }
        };
    }

    @GetMapping("/api/finagle/retry")
    public CompletableFuture<ResponseEntity<String>> getRetry(HttpServletResponse httpServletResponse) {
        Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");

        Future<Response> futureResponse = client.apply(request);
        return toSpringResponse(futureResponse, httpServletResponse);
    }
}

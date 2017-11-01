package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.service.RetryBudget;
import com.twitter.finagle.service.RetryBudget$;
import com.twitter.finagle.service.RetryFilter;
import com.twitter.finagle.service.RetryPolicy;
import com.twitter.finagle.service.SimpleRetryPolicy;
import com.twitter.finagle.stats.NullStatsReceiver$;
import com.twitter.finagle.util.DefaultTimer$;
import com.twitter.util.Duration;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.Tuple2;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
public class FinagleRetryController extends BaseFinagleController {


    private Service<Request, Response> client;

    @Autowired
    public FinagleRetryController(InstanceManager instanceManager) {
        super(instanceManager);

        String connectionString = "weather1:8080";

        RetryBudget budget = RetryBudget$.MODULE$.apply();

        RetryPolicy<Tuple2<Request, Try<Response>>> policy = new SimpleRetryPolicy<Tuple2<Request, Try<Response>>>() {
            public Duration backoffAt(int retry) {
                return Duration.fromMilliseconds(retry * 10);
            }

            public boolean shouldRetry(Tuple2<Request, Try<Response>> requestTryResponse) {
                Try<Response> tryResponse = requestTryResponse._2;
                return tryResponse.isReturn() && tryResponse.get().getStatusCode() == 404;
            }
        };

        RetryFilter<Request, Response> retryFilter =
                new RetryFilter<>(policy, DefaultTimer$.MODULE$.twitter(), NullStatsReceiver$.MODULE$, budget);

        client = retryFilter.andThen(HostFilter$.MODULE$.client().newService(connectionString, "retry"));
    }

    @GetMapping("/api/finagle/retry")
    public CompletableFuture<ResponseEntity<String>> getRetry(HttpServletResponse httpServletResponse) {
        Request primaryRequest = Request.apply(Method.Get(), "/weather");
        primaryRequest.host("localhost");

        Future<Response> futureResponse = client.apply(primaryRequest);
        return toSpringResponse(futureResponse, httpServletResponse);
    }
}

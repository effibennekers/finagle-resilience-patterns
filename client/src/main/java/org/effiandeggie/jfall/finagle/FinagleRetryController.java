package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.finagle.param.HighResTimer$;
import com.twitter.finagle.service.Backoff$;
import com.twitter.finagle.service.RetryBudget;
import com.twitter.finagle.service.RetryFilter;
import com.twitter.finagle.service.RetryPolicy;
import com.twitter.finagle.stats.StatsReceiver;
import com.twitter.util.Duration;
import com.twitter.util.Future;
import com.twitter.util.Timer;
import com.twitter.util.Try;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.effiandeggie.jfall.instances.Instance;
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

    private Instance instance;

    private Service<Request, Response> client;

    @Autowired
    public FinagleRetryController(InstanceManager instanceManager) {
        super(instanceManager);
        this.instance = instanceManager.getPrimaryInstances()[0];

        String connectionString = instancesToConnectionString(instance);
        //"http://effi:8080"

        client = HostFilter$.MODULE$.client()
                .withSession().acquisitionTimeout(Duration.fromMilliseconds(1500))
                .withRequestTimeout(Duration.fromMilliseconds(60))
                .newService(connectionString, "retry");
    }

    @GetMapping("/api/finagle/retry")
    public CompletableFuture<ResponseEntity<String>> getRetry(HttpServletResponse httpServletResponse) {
        Request primaryRequest = Request.apply(Method.Get(), "/weather");
        primaryRequest.host("localhost");

        setHeadersForDemo(httpServletResponse, instance);

        Future<Response> futureResponse = client.apply(primaryRequest);
        return toSpringResponse(futureResponse);
    }
}

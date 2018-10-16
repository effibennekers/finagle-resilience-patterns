package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import org.effiandeggie.finagle.filters.Http$;
import org.effiandeggie.jfall.instances.Instance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

import static org.effiandeggie.jfall.instances.InstanceManager.connectionString;

@RestController
public class FinagleLoadbalanceController extends BaseFinagleController {

    private Service<Request, Response> client;

    public FinagleLoadbalanceController(Instance[] instances) {
        client = Http$.MODULE$.client().newService(connectionString(instances[0], instances[1]), "loadbalancer");
    }

    @GetMapping(value = "/api/finagle/loadbalancing")
    public CompletableFuture<ResponseEntity<String>> loadbalance(HttpServletResponse httpServletResponse) {
        Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");

        Future<Response> futureResponse = client.apply(request);
        return toSpringResponse(futureResponse, httpServletResponse);
    }
}

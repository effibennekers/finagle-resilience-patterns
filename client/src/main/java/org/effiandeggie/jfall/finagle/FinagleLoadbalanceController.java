package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

@RestController
public class FinagleLoadbalanceController extends BaseFinagleController {

    private Service<Request, Response> client;

    @Autowired
    public FinagleLoadbalanceController(InstanceManager instanceManager) {
        super(instanceManager);
        String connectionString = "weather1:8080,weather2:8080";

        client = HostFilter$.MODULE$.client().newService(connectionString, "loadbalancer");
    }

    @GetMapping(value = "/api/finagle/loadbalancing")
    public CompletableFuture<ResponseEntity<String>> loadbalance(HttpServletResponse httpServletResponse) {
        Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");

        Future<Response> futureResponse = client.apply(request);
        return toSpringResponse(futureResponse, httpServletResponse);
    }
}

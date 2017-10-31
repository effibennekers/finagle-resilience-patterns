package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.effiandeggie.jfall.utils.FutureUtil;
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
    public FinagleLoadbalanceController(final InstanceManager instanceManager) {
        super(instanceManager);
        client = HostFilter$.MODULE$.client().newService(instancesToConnectionString(instanceManager.getInstances()), "loadbalancer");
    }

    @GetMapping(value = "/api/finagle/loadbalancing")
    public CompletableFuture<ResponseEntity<String>> loadbalance(final HttpServletResponse httpServletResponse) {
        final Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");
        final Future<ResponseEntity<String>> futureResponse = client.apply(request).map(response -> {
            setHeadersForDemo(httpServletResponse, response);
            if (response.getStatusCode() == 200) {
                return ResponseEntity.ok(response.contentString());
            } else {
                return ResponseEntity.status(response.getStatusCode()).build();
            }
        });
        return FutureUtil.toJavaFuture(futureResponse);
    }

}

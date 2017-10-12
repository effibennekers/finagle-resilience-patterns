package org.effiandeggie.jfall;

import com.twitter.finagle.Name;
import com.twitter.finagle.Resolver$;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RestController
public class FinagleLoadbalanceController {

    private Service<Request, Response> client;

    private final InstanceManager instanceManager;


    @Autowired
    public FinagleLoadbalanceController(final InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
        final String hosts = Arrays.stream(instanceManager.getInstances()).map(instance ->
                instance.getHost() + ":" + instance.getPort()).collect(Collectors.joining(","));
        final Name dest = Resolver$.MODULE$.eval(hosts);
        client = HostFilter$.MODULE$.client().newService(dest, "loadbalancer");
    }

    @GetMapping(value = "/api/finagle/loadbalancing")
    public CompletableFuture<String> loadbalance(final HttpServletResponse httpServletResponse) {
        final Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");
        final Future<String> futureResponse = client.apply(request).map(response -> {
            final String instanceName = instanceManager.lookup(response).map(Instance::getName).orElse("unknown");
            httpServletResponse.setHeader("instance", instanceName);
            return response.contentString();
        });
        return FutureUtil.toJavaFuture(futureResponse);
    }
}

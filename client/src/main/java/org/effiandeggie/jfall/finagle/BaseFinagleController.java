package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import org.effiandeggie.jfall.instances.Instance;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.effiandeggie.jfall.utils.FutureUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class BaseFinagleController {

    private final InstanceManager instanceManager;

    public BaseFinagleController(final InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    private void setHeadersForDemo(final HttpServletResponse httpServletResponse, final Response response) {
        final String instanceName = instanceManager.lookup(response).map(Instance::getName).orElse("unknown");
        System.err.println ("Instance name: " + instanceName);
        System.err.println ("response headers name: " + response.headerMap());
        httpServletResponse.setHeader("instance", instanceName);
    }

    protected String instancesToConnectionString(final Instance... instances) {
        return Arrays.stream(instances).map(instance ->
                instance.getHost() + ":" + instance.getPort()).collect(Collectors.joining(","));

    }

    protected CompletableFuture<ResponseEntity<String>> toSpringResponse(final Future<Response> futureResponse, HttpServletResponse httpServletResponse) {
        final CompletableFuture<ResponseEntity<String>> javaFutureResponse = FutureUtil.toJavaFuture(futureResponse.map(

                response -> {
                    setHeadersForDemo(httpServletResponse, response);
                    if (response.getStatusCode() == 200) {
                        return ResponseEntity.ok(response.getContentString());
                    } else {
                        return ResponseEntity.status(response.getStatusCode()).build();
                    }
                }));
        javaFutureResponse.exceptionally(y -> {
            final ResponseEntity<String> errorResult = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            return errorResult;
        });
        return javaFutureResponse;
    }
}

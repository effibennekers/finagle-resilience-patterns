package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import org.effiandeggie.jfall.instances.Instance;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.effiandeggie.jfall.utils.FutureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.CompletableFuture;

public class BaseFinagleController {

    @Autowired
    private InstanceManager instanceManager;

    private void setHeadersForDemo(final HttpServletResponse httpServletResponse, final Response response) {
        final String instanceName = instanceManager.lookup(response).map(Instance::getName).orElse("unknown");
        httpServletResponse.setHeader("instance", instanceName);
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
        javaFutureResponse.exceptionally(x -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        return javaFutureResponse;
    }
}

package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import org.effiandeggie.jfall.instances.Instance;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.effiandeggie.jfall.utils.FutureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.CompletableFuture;

public class BaseFinagleController {

    private static final String HEADERNAME_INSTANCE = "instance";
    @Autowired
    private InstanceManager instanceManager;

    protected CompletableFuture<ResponseEntity<String>> toSpringResponse(final Future<Response> futureResponse) {
        final CompletableFuture<ResponseEntity<String>> javaFutureResponse = FutureUtil.toJavaFuture(futureResponse.map(

                response -> {
                    final String instanceName = instanceManager.lookup(response).map(Instance::getName).orElse("unknown");
                    if (response.getStatusCode() == 200) {
                        return ResponseEntity.ok().header(HEADERNAME_INSTANCE, instanceName).body(response.getContentString());
                    } else {
                        return ResponseEntity.status(response.getStatusCode()).header("instance", instanceName).build();
                    }
                }));
        javaFutureResponse.exceptionally(x -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        return javaFutureResponse;
    }
}

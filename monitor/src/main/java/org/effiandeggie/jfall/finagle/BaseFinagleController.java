package org.effiandeggie.jfall.finagle;

import com.twitter.finagle.http.Response;
import org.effiandeggie.jfall.instances.Instance;
import org.effiandeggie.jfall.instances.InstanceManager;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.stream.Collectors;

public class BaseFinagleController {

    private final InstanceManager instanceManager;

    public BaseFinagleController(final InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    protected void setHeadersForDemo(final HttpServletResponse httpServletResponse, final Response response) {
        final String instanceName = instanceManager.lookup(response).map(Instance::getName).orElse("unknown");
        httpServletResponse.setHeader("instance", instanceName);
    }

    protected void setHeadersForDemo(final HttpServletResponse httpServletResponse, final Instance instance) {
        httpServletResponse.setHeader("instance", instance.getName());
    }

    protected String instancesToConnectionString(final Instance... instances) {
        return Arrays.stream(instances).map(instance ->
                instance.getHost() + ":" + instance.getPort()).collect(Collectors.joining(","));

    }
}

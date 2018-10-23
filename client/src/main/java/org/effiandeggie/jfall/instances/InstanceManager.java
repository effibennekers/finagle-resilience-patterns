package org.effiandeggie.jfall.instances;

import com.twitter.finagle.http.HeaderMap;
import com.twitter.finagle.http.Response;
import scala.Option;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstanceManager {

    private final Map<String, Instance> instanceTable = new HashMap<>();

    private final Instance[] instances;

    public Instance[] getInstances() {
        return instances;
    }

    public InstanceManager(final Instance[] instances) {
        this.instances = instances;
        for (Instance instance : instances) {
            instanceTable.put(instance.getKey(), instance);
        }
    }

    public Optional<Instance> lookup(final Option<String> host, Option<Integer> port) {
        if (host.isDefined() && port.isDefined()) {
            return Optional.ofNullable(instanceTable.get(host.get() + ":" + port.get()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Instance> lookup(final Response response) {
        final HeaderMap headers = response.headerMap();
        final Option<String> hostname = headers.get("Host");
        final Option<Integer> port = headers.get("Port").map(Integer::parseInt);
        return lookup(hostname, port);
    }

    public static String connectionString(Instance... instances) {
        return Arrays.stream(instances).
                map(instance -> instance.getKey()).collect(Collectors.joining(","));

    }

}

package org.effiandeggie.jfall;

import com.twitter.finagle.http.HeaderMap;
import com.twitter.finagle.http.Response;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import scala.Option;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InstanceManager {

    private final Map<String, Instance> instanceTable = new HashMap<>();

    @Getter
    private final Instance[] instances;

    public InstanceManager(@Qualifier("instances") final Instance[] instances) {
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
}

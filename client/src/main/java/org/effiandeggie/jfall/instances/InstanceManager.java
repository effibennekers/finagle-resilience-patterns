package org.effiandeggie.jfall.instances;

import com.twitter.finagle.http.HeaderMap;
import com.twitter.finagle.http.Response;
import lombok.Getter;
import org.effiandeggie.jfall.instances.Instance;
import scala.Option;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;

public class InstanceManager {

    private final Map<String, Instance> instanceTable = new HashMap<>();

    @Getter
    private final Instance[] instances;

    @Getter
    private final Instance[] primaryInstances;

    @Getter
    private final Instance[] secondaryInstances;

    public InstanceManager(final Instance[] primaryInstances, final Instance[] secondaryInstances) {
        final List<Instance> instanceList = new LinkedList<>();
        this.primaryInstances = primaryInstances;
        this.secondaryInstances = secondaryInstances;
        instanceList.addAll(asList(primaryInstances));
        instanceList.addAll(asList(secondaryInstances));

        this.instances = instanceList.toArray(new Instance[instanceList.size()]);

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

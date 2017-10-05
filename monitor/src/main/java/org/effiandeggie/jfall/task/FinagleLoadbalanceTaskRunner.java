package org.effiandeggie.jfall.task;

import com.twitter.finagle.Name;
import com.twitter.finagle.Resolver$;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.Future;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.effiandeggie.jfall.Instance;
import scala.Option;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FinagleLoadbalanceTaskRunner extends TaskRunner {
    private final Service<Request, Response> client;
    private final Request request;
    private final Map<Integer, String> portHostTable = new HashMap<>();

    public FinagleLoadbalanceTaskRunner(final Consumer<TaskReport> reporter, final TaskConfiguration taskConfiguration,
                                        final Instance[] instances) {
        super(reporter, taskConfiguration);
        final String hosts = Arrays.stream(instances).map(instance -> "localhost:" + instance.getPort()).collect(Collectors.joining(","));
        for (final Instance instance : instances) {
            portHostTable.put(instance.getPort(), instance.getName());
        }
        final Name dest = Resolver$.MODULE$.eval(hosts);
        client = HostFilter$.MODULE$.client().newService(dest, "localhost");
        request = Request.apply(Method.Get(), "/loadbalancing");
        request.host("localhost");
    }

    @Override
    protected Supplier<TaskResponse> getExecutor() {
        return () -> {
            final Future<Response> futureResponse = client.apply(request);
            try {
                final Response result = Await.result(futureResponse);
                final Option<String> scalaOptionalHost = result.headerMap().get("Port");
                final Optional<String> javaOptHost = scalaOptionalHost.isDefined()?
                        Optional.of(scalaOptionalHost.get()):Optional.empty();

                final int port = javaOptHost.map(Integer::parseInt).orElse(-1);
                final String host = portHostTable.getOrDefault(port, "unknown");
                return new TaskResponse(result.statusCode(), result.contentString(), host);
            } catch (Exception e) {
                e.printStackTrace();
                return new TaskResponse(500, e.getMessage(), "unexepected error");
            }
        };
    }
}

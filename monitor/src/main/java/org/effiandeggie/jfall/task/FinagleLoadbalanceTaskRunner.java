package org.effiandeggie.jfall.task;

import com.twitter.finagle.Http;
import com.twitter.finagle.Name;
import com.twitter.finagle.Resolver$;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.Future;
import org.effiandeggie.finagle.filters.HostFilter$;
import scala.Option;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FinagleLoadbalanceTaskRunner extends TaskRunner {
    private final Service<Request, Response> client;
    private final Request request;

    public FinagleLoadbalanceTaskRunner(final Consumer<TaskReport> reporter, final TaskConfiguration taskConfiguration) {
        super(reporter, taskConfiguration);
        final Name dest = Resolver$.MODULE$.eval("localhost:8081,localhost:8082");
        //client = Http.newService(dest, "loadbalance");
        client = HostFilter$.MODULE$.client().newService(dest, "localhost");
        //final Service<Request, Response> x = Http.newService(dest, "l");


        request = Request.apply(Method.Get(), "/loadbalancing");
        request.host("localhost");
    }

    @Override
    protected Supplier<TaskResponse> getExecutor() {
        return () -> {
            final Future<Response> futureResponse = client.apply(request);
            try {
                final Response result = Await.result(futureResponse);
                final Option<String> scalaOptionalHost = result.headerMap().get("host");

                final String host = scalaOptionalHost.isDefined()?scalaOptionalHost.get():"not set";
                return new TaskResponse(result.statusCode(), result.contentString() , host);
            } catch (Exception e) {
                return new TaskResponse(500, e.getMessage(), "eggie");
            }

        };
    }
}

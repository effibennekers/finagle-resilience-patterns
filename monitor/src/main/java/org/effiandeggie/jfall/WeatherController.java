package org.effiandeggie.jfall;

import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.util.concurrent.CompletableFuture;

@RestController
public class WeatherController {


    @GetMapping("/weather")
    public CompletableFuture<String> getReport() {

        final Service<Request, Response> secondaryClient = HostFilter$.MODULE$.client().
                withSessionQualifier().noFailFast().
                newService("localhost:8083", "secondary");
        final Service<Request, Response> primaryClient = HostFilter$.MODULE$.client().
                withTransport().verbose()
                .withSessionQualifier().noFailFast()
                .newService("localhost:8081", "primary");
        final Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");

        final Future<Try<Response>> tryableFutureResponse = primaryClient.apply(request).liftToTry();

        final Future<Response> futureResponse = tryableFutureResponse.flatMap(tryResponse -> {
            if (tryResponse.isThrow()) {
                return secondaryClient.apply(request);
            } else {
                if (tryResponse.get().getStatusCode() != 200) {
                    return secondaryClient.apply(request);
                } else {
                    return Future.value(tryResponse.get());
                }
            }
        });

        return FutureUtil.toJavaFuture(futureResponse.map(x -> x.contentString()));
    }
}




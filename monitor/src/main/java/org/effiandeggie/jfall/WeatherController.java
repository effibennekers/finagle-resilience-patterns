package org.effiandeggie.jfall;

import com.twitter.finagle.Name;
import com.twitter.finagle.Resolver$;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.Future;
import com.twitter.util.Try;
import org.effiandeggie.finagle.filters.HostFilter$;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WeatherController {


    @GetMapping("/weather")
    public ResponseEntity<String> getReport() {

        final Service<Request, Response> secondaryClient = HostFilter$.MODULE$.client().
                withSessionQualifier().noFailFast().
                newService("localhost:8083", "secondary");
        final Service<Request, Response> primaryClient = HostFilter$.MODULE$.client().
                withTransport().verbose()
                .withSessionQualifier().noFailFast()
                .newService("localhost:8081", "primary");
        final Request request = Request.apply(Method.Get(), "/weather");
        request.host("localhost");

        final Future<Try<Response>> futureResponse = primaryClient.apply(request).liftToTry();

        final Future<Response> x = futureResponse.flatMap(tryResponse -> {
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

        try {
            final Response result = Await.result(x);
            return ResponseEntity.ok(result.getContentString());
        } catch (Exception e) {
            System.err.println("Exception in controller" + e.getMessage());
            return ResponseEntity.status(500).build();
        } finally {
        }

    }
}




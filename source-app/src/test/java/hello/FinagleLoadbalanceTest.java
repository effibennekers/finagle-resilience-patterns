package hello;

import com.twitter.finagle.Http;
import com.twitter.finagle.Service;
import com.twitter.finagle.http.Method;
import com.twitter.finagle.http.Request;
import com.twitter.finagle.http.Response;
import com.twitter.util.Await;
import com.twitter.util.Future;
import org.junit.Test;


public class FinagleLoadbalanceTest extends BaseTest {
    @Test
    public void testLoadbalancingWithFinagle() {
        final Service<Request, Response> client = (Service<Request, Response>) Http.newService("localhost:8080,localhost:8081");

        Request request = Request.apply(Method.Get(), "/loadbalancing");
        request.host("localhost");

        runTask(() -> {
            final Future<Response> futureResponse = client.apply(request);
            try {
                final Response result = Await.result(futureResponse);
                return new TaskResponse(result.statusCode(), result.contentString());
            } catch (Exception e) {
                return new TaskResponse(500, e.getMessage());
            }

        });
    }
}

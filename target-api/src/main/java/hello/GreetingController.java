package hello;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private static final String TEMPLATE = "[%d] Hello %s from %s!";
    private final AtomicLong counter = new AtomicLong();

    private Random random = new Random();

    @Value("${server.display-name}")
    private String serviceName;

    @RequestMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        simulateHeavyProcessing();
        return String.format(TEMPLATE, counter.incrementAndGet(), name, serviceName);
    }

    private void simulateHeavyProcessing() {
        try {
            Thread.sleep(50 + random.nextInt(50));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

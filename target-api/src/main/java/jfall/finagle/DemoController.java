package jfall.finagle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class DemoController {

    private final AtomicLong counter = new AtomicLong();

    private Random random = new Random();

    @Value("${server.display-name}")
    private String serviceName;

    @Value("${simulate.processing.base:50}")
    private long simulateProcessingBase;

    @Value("${simulate.processing.random:50}")
    private int simulateProcessingRandom;

    @RequestMapping("/loadbalancing")
    public String getLoadbalancing() {
        final long number = counter.incrementAndGet();
        simulateHeavyProcessing();
        return String.format("%d loadbalancing example from %s", number, serviceName);
    }

    private void simulateHeavyProcessing() {
        try {
            Thread.sleep(simulateProcessingBase + random.nextInt(simulateProcessingRandom));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

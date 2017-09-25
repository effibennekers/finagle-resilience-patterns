package jfall.finagle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class DemoController {

    private final AtomicLong counter = new AtomicLong();

    private Random random = new Random();

    @Value("${server.display-name}")
    private String serviceName;

    @Value("${simulate.processing.base:50}")
    private long defaultSimulateProcessingBase;

    @Value("${simulate.processing.random:50}")
    private int defaultSimulateProcessingRandom;
    private long simulateProcessingBase;
    private int simulateProcessingRandom;

    @PostConstruct
    public void setDefaults() {
        this.simulateProcessingBase = defaultSimulateProcessingBase;
        this.simulateProcessingRandom = defaultSimulateProcessingRandom;
    }

    @RequestMapping(value = "/simulate", method = RequestMethod.POST)
    public String postSimulate(@RequestParam(value = "base", required = false) Optional<Long> base,
                               @RequestParam(value = "random", required = false) Optional<Integer> random) {
        if (base.isPresent()) {
            simulateProcessingBase = base.get();
        }
        if (random.isPresent()) {
            simulateProcessingRandom = random.get();
        }
        if (!base.isPresent() && !random.isPresent()) {
            setDefaults();
        }
        final String message = "Simulation params for " + serviceName + ": base: " + simulateProcessingBase + ", random: " + simulateProcessingRandom;
        System.err.println(message);
        return message;
    }

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

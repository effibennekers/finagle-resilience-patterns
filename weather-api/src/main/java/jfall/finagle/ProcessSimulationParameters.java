package jfall.finagle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;

@Component
public class ProcessSimulationParameters {

    private transient final SecureRandom secureRandom = new SecureRandom();
    @Value("${process.simulation.base:50}")
    private long defaultBaseTime;
    @Value("${process.simulation.random:5}")
    private int defaultRandom;
    @Value("${process.simulation.random.multiplier:10}")
    private int defaultRandomMultiplier;

    /**
     * I've tried to use lombok for this fields, but then all of a sudden {@link jfall.finagle.SimulationController#postSimulationParameters(ProcessSimulationParameters)}
     * yields a 415 error instead of accepting a JSON value
     */
    private long baseTime;
    private int random;
    private int randomMultiplier;
    private int failureRate;


    public void update(final ProcessSimulationParameters parameters) {
        this.baseTime = parameters.getBaseTime();
        this.random = parameters.getRandom();
        this.randomMultiplier = parameters.getRandomMultiplier();
        this.failureRate = parameters.getFailureRate();
    }

    @PostConstruct
    public void setToDefault() {
        this.baseTime = defaultBaseTime;
        this.random = defaultRandom;
        this.randomMultiplier = defaultRandomMultiplier;
        this.failureRate = 0;
    }

    public long getBaseTime() {
        return baseTime;
    }


    public int getRandom() {
        return random;
    }


    public int getRandomMultiplier() {
        return randomMultiplier;
    }

    public int getFailureRate() {
        return failureRate;
    }

    public long simulationTime() {
        return baseTime + secureRandom.nextInt(random) * randomMultiplier;
    }

    public boolean simulateFaiure() {
        int dice = secureRandom.nextInt(100) + 1;
        return dice < failureRate;
    }


    @Override
    public String toString() {
        return "base time: " + baseTime +
                ", random: " + random +
                ", random multiplier: " + randomMultiplier +
                ", failure rate: " + failureRate;
    }
}

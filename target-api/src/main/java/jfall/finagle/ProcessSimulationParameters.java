package jfall.finagle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.SecureRandom;

@Component
public class ProcessSimulationParameters {

    private final SecureRandom secureRandom = new SecureRandom();
    @Value("${process.simulation.base:50}")
    private long defaultBaseTime;
    @Value("${process.simulation.random:5}")
    private int defaultRandom;
    @Value("${process.simulation.random.multiplier:10}")
    private int defaultRandomMultiplier;

    private long baseTime;
    private int random;
    private int randomMultiplier;


    public void update(final ProcessSimulationParameters parameters) {
        this.baseTime = parameters.getBaseTime();
        this.random = parameters.getRandom();
        this.randomMultiplier = parameters.getRandomMultiplier();
    }

    @PostConstruct
    public void setToDefault() {
        this.baseTime = defaultBaseTime;
        this.random = defaultRandom;
        this.randomMultiplier = defaultRandomMultiplier;
    }

    public long getBaseTime() {
        return baseTime;
    }

    public void setBaseTime(final long baseTime) {
        this.baseTime = baseTime;
    }

    public int getRandom() {
        return random;
    }

    public void setRandom(final int random) {
        this.random = random;
    }

    public int getRandomMultiplier() {
        return randomMultiplier;
    }

    public void setRandomMultiplier(final int randomMultiplier) {
        this.randomMultiplier = randomMultiplier;
    }

    public long simulationTime() {
        return baseTime + secureRandom.nextInt(random) * randomMultiplier;
    }

    @Override
    public String toString() {
        return "base time: " + baseTime + ", random: " + random + ", random multiplier: " + randomMultiplier;
    }
}

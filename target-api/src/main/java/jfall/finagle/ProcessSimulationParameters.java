package jfall.finagle;

import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;

public class ProcessSimulationParameters {

    private final SecureRandom secureRandom = new SecureRandom();
    @Value("${process.simulation.base:50}")
    private long baseTime;
    @Value("${process.simulation.random:5}")
    private int random;
    @Value("${process.simulation.random.multiplier:10}")
    private int randomMultiplier;

    public ProcessSimulationParameters() {
    }

    public ProcessSimulationParameters(final ProcessSimulationParameters source) {
        this.baseTime = source.getBaseTime();
        this.random = source.getRandom();
        this.randomMultiplier = source.getRandomMultiplier();
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

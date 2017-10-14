package jfall.finagle;

import com.ing.example.jfall.pojo.WeatherReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;

@RestController
public class WeatherController {

    private final ProcessSimulationParameters processSimulationParameters;

    private final SecureRandom random = new SecureRandom();

    @Value("${weather.fallback.mode:false}")
    private boolean fallbackMode;

    public WeatherController(final ProcessSimulationParameters processSimulationParameters) {
        this.processSimulationParameters = processSimulationParameters;
        System.err.println ("Using parameters: " + processSimulationParameters.toString());
    }

    @GetMapping("/weather")
    public ResponseEntity<WeatherReport> getReport() {
        simulateHeavyProcessing();

        if (processSimulationParameters.simulateFaiure()) {
            return ResponseEntity.notFound().build();

        }
        else {
            final WeatherReport report = new WeatherReport();
            report.setCondition(randomEnum(WeatherReport.Condition.class));
            report.setTemperature(-20 + random.nextInt(51));
            if (!fallbackMode) {
                report.setWindForce(random.nextInt(11));
                report.setWindDirection(randomEnum(WeatherReport.WindDirection.class));
            }
            return ResponseEntity.ok(report);
        }
    }


    public <T extends Enum<?>> T randomEnum(Class<T> clazz) {
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    private void simulateHeavyProcessing() {
        try {
            final long delay = processSimulationParameters.simulationTime();
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

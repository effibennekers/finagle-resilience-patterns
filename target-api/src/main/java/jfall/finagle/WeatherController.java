package jfall.finagle;

import com.ing.example.jfall.pojo.WeatherReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;

@RestController
public class WeatherController {

    private SecureRandom random = new SecureRandom();
    @Value("${weather.fallback.mode:false}")
    private boolean fallbackMode;

    @GetMapping("/weather")
    public ResponseEntity<WeatherReport> getReport() {
        final WeatherReport report = new WeatherReport();
        report.setCondition(randomEnum(WeatherReport.Condition.class));
        report.setWindDirection(randomEnum(WeatherReport.WindDirection.class));
        report.setTemperature(-20 + random.nextInt(51));
        report.setWindForce(random.nextInt(11));
        if (!fallbackMode) {
            report.setAccuracy(random.nextInt(100) + 1);
        }
        return ResponseEntity.ok(report);

    }


    public <T extends Enum<?>> T randomEnum(Class<T> clazz) {
        int x = random.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}

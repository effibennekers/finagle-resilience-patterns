package jfall.finagle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class DemoController {

    private final AtomicLong counter = new AtomicLong();

    private final ProcessSimulationParameters defaultParameters;

    private ProcessSimulationParameters currentparameters;

    @Value("${server.display-name}")
    private String serviceName;


    public DemoController(final ProcessSimulationParameters processSimulationParameters) {
        defaultParameters = processSimulationParameters;

    }

    @PostConstruct
    public void setDefaults() {
        currentparameters = new ProcessSimulationParameters(defaultParameters);
    }

    @RequestMapping(value = "/simulation", method = RequestMethod.GET)
    public ResponseEntity<ProcessSimulationParameters> getSimulation() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        return new ResponseEntity<ProcessSimulationParameters>(currentparameters, httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/simulation/defaults", method = RequestMethod.POST)
    public ResponseEntity<ProcessSimulationParameters> postReset() {
        return postSimulate(defaultParameters);
    }

    @RequestMapping(value = "/simulation", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsSimulate() {
        return ResponseEntity.accepted().build();

    }

    @RequestMapping(value = "/simulation", method = RequestMethod.POST)
    public ResponseEntity<ProcessSimulationParameters> postSimulate(@RequestBody final ProcessSimulationParameters parameters) {
        currentparameters = new ProcessSimulationParameters(parameters);
        final String message = "Simulation params for " + serviceName + " " + currentparameters.toString();
        System.err.println(message);
        return getSimulation();
    }

    @RequestMapping("/loadbalancing")
    public String getLoadbalancing() {
        final long number = counter.incrementAndGet();
        simulateHeavyProcessing();
        return String.format("%d loadbalancing example from %s", number, serviceName);
    }

    private void simulateHeavyProcessing() {
        try {
            final long delay = currentparameters.simulationTime();
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

package jfall.finagle;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimulationController {

    private final ProcessSimulationParameters processSimulationParameters;

    public SimulationController(final ProcessSimulationParameters processSimulationParameters) {
        this.processSimulationParameters = processSimulationParameters;
    }

    @RequestMapping(value = "/simulation", method = RequestMethod.GET)
    public ResponseEntity<ProcessSimulationParameters> getSimulation() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        return new ResponseEntity<>(processSimulationParameters, httpHeaders, HttpStatus.OK);
    }

    @RequestMapping(value = "/simulation/defaults", method = RequestMethod.POST)
    public ResponseEntity<ProcessSimulationParameters> postReset() {
        processSimulationParameters.setToDefault();
        return getSimulation();
    }

    @RequestMapping(value = "/simulation", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> optionsSimulate() {
        return ResponseEntity.accepted().build();

    }

    @RequestMapping(value = "/simulation", method = RequestMethod.POST)
    public ResponseEntity<ProcessSimulationParameters> postSimulate(@RequestBody final ProcessSimulationParameters parameters) {
        processSimulationParameters.update(parameters);
        return getSimulation();
    }

}

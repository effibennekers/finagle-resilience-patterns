package org.effiandeggie.jfall;


import org.effiandeggie.jfall.task.AppacheLoadbalanceTaskRunner;
import org.effiandeggie.jfall.task.FinagleLoadbalanceTaskRunner;
import org.effiandeggie.jfall.task.TaskConfiguration;
import org.effiandeggie.jfall.task.TaskRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.core.MessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoadbalanceController {
    private MessageSendingOperations<String> messagingTemplate;

    private Map<Integer, TaskRunner> runnerTable = new HashMap<>();
    private int lastRunnerId = 0;

    @Autowired
    public LoadbalanceController(final MessageSendingOperations<String> messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @DeleteMapping(value = "/runner/{id}")
    public ResponseEntity<?> testLoadbalancingWithApacheHttpClient(@PathVariable("id") final int id) {
        final TaskRunner taskRunner = runnerTable.get(id);
        if (taskRunner != null) {
            taskRunner.stop();
            runnerTable.remove(id);
            return ResponseEntity.accepted().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/runner/apache/loadbalancing", consumes = "application/json")
    public ResponseEntity<Integer> testLoadbalancingWithApacheHttpClient(@RequestBody final TaskConfiguration taskConfiguration) {
        final TaskRunner taskRunner = new AppacheLoadbalanceTaskRunner(
                report -> {
                    messagingTemplate.convertAndSend("/topic/loadbalancing", report);
                }, taskConfiguration);
        final int id = lastRunnerId++;
        runnerTable.put(id, taskRunner);
        taskRunner.run();
        return ResponseEntity.ok(id);
    }

    @PutMapping(value = "/runner/finagle/loadbalancing", consumes = "application/json")
    public ResponseEntity<Integer> testLoadbalancingWithFinagle(@RequestBody final TaskConfiguration taskConfiguration) {
        final TaskRunner taskRunner = new FinagleLoadbalanceTaskRunner(
                report -> {
                    messagingTemplate.convertAndSend("/topic/loadbalancing", report);
                }, taskConfiguration);
        final int id = lastRunnerId++;
        runnerTable.put(id, taskRunner);
        taskRunner.run();
        return ResponseEntity.ok(id);
    }
}

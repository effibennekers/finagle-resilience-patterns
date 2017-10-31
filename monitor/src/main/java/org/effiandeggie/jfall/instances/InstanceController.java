package org.effiandeggie.jfall.instances;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class InstanceController {


    @Resource
    private InstanceManager instanceManager;

    @GetMapping("/instances")
    public ResponseEntity<Instance[]> getInstances() {
        return ResponseEntity.ok(instanceManager.getInstances());
    }
}

package org.effiandeggie.jfall;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class InstanceController {


    @Resource
    private Instance[] instances;

    @GetMapping("/instances")
    public ResponseEntity<Instance[]> getInstances() {
        return ResponseEntity.ok(instances);
    }
}

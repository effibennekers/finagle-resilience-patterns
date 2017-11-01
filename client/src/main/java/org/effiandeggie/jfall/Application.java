package org.effiandeggie.jfall;

import org.effiandeggie.jfall.instances.Instance;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


//    @Bean
//    public InstanceManager instanceManager() {
//        final Instance[] primaryInstances = new Instance[]{
//                new Instance("effi", "effi", 8080),
//                new Instance("eggie", "eggie", 8080)
//
//        };
//        final Instance[] secondaryInstances = new Instance[]{
//                new Instance("oldweather", "oldweather", 8080)
//        };
//        return new InstanceManager(primaryInstances, secondaryInstances);
//    }
    @Bean
    public InstanceManager instanceManager() {
        final Instance[] primaryInstances = new Instance[]{
                new Instance("effi", "localhost", 8081),
                new Instance("eggie", "localhost", 8082)

        };
        final Instance[] secondaryInstances = new Instance[]{
                new Instance("oldweather", "localhost", 8083)
        };
        return new InstanceManager(primaryInstances, secondaryInstances);
    }
}

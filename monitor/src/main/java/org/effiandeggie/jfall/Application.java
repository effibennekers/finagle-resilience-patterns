package org.effiandeggie.jfall;

import org.springframework.beans.factory.annotation.Qualifier;
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


    @Bean(name = "primaryInstance")
    public Instance primaryInstance() {
        return new Instance("effi", "localhost", 8081);
    }

    @Bean(name = "secondaryInstance")
    public Instance secondaryInstance() {
        return new Instance("henk", "localhost", 8083);
    }

    @Bean
    public Instance[] instances(final Instance primaryInstance, final Instance secondaryInstance) {
        return new Instance[]{
                primaryInstance,
                new Instance("eggie", "localhost", 8082),
                secondaryInstance
        };
    }

    @Bean
    public InstanceManager instanceManager(@Qualifier("instances") final Instance[] instances) {
        return new InstanceManager(instances);
    }
}

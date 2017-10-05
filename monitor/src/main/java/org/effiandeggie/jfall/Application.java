package org.effiandeggie.jfall;

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


    @Bean
    public Instance[]instances() {
        return new Instance[] {
                new Instance("effi", 8081),
                new Instance("eggie", 8082),
                new Instance("joost", 8083)
        };

    }
}

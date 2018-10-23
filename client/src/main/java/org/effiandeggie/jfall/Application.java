package org.effiandeggie.jfall;

import org.effiandeggie.jfall.instances.Instance;
import org.effiandeggie.jfall.instances.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@SpringBootApplication
@EnableScheduling
public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Value("${instances:weather1:weather1:8080,weather2:weather2:8080,oldweather:oldweather:8080}")
    private String instanceString;

    private Pattern HOST_PATTERN = Pattern.compile("(\\p{Alnum}+):(\\p{Alnum}+):(\\d+)");

    @Bean
    public Instance[] instances() {
        final List<Instance> instanceList = new LinkedList<>();
        LOG.info("Instance: " + instanceString);
        for (String instance : instanceString.split(",")) {
            Matcher matcher = HOST_PATTERN.matcher(instance);
            if (matcher.find()) {
                instanceList.add(new Instance(matcher.group(1), matcher.group(2), Integer.parseInt(matcher.group(3))));
            } else {
                throw new IllegalArgumentException(format("instance string [%s] contains invalid instance [%s]", instanceString, instance));
            }

        }
        return instanceList.toArray(new Instance[instanceList.size()]);

    }


    @Bean
    public InstanceManager instanceManager(Instance[] instances) {
        for (int i = 0; i < instances.length; i++) {
            LOG.info(format("Instance %d with name %s, host %s and port %d",
                    i, instances[i].getName(), instances[i].getHost(), instances[i].getPort()));
        }
        return new InstanceManager(instances);
    }
}

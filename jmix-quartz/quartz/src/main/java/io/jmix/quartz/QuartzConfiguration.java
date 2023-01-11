package io.jmix.quartz;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {CoreConfiguration.class, DataConfiguration.class})
@PropertySource(name = "io.jmix.quartz", value = "classpath:/io/jmix/quartz/module.properties")
public class QuartzConfiguration {
}

package io.jmix.reportsrest;

import io.jmix.core.annotation.JmixModule;
import io.jmix.reports.ReportsConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@EnableWebSecurity
@JmixModule(dependsOn = ReportsConfiguration.class)
@PropertySource(name = "io.jmix.reportsrest", value = "classpath:/io/jmix/reportsrest/module.properties")
public class ReportsRestConfiguration {
}

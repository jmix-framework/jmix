package io.jmix.appsettings;

import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {DataConfiguration.class})
public class AppSettingsConfiguration {
}

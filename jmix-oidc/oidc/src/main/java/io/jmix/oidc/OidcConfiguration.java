package io.jmix.oidc;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {CoreConfiguration.class})
@PropertySource(name = "io.jmix.oidc", value = "classpath:/io/jmix/oidc/module.properties")
public class OidcConfiguration {
}

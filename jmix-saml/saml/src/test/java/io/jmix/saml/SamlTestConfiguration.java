package io.jmix.saml;

import io.jmix.core.annotation.JmixModule;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(SamlConfiguration.class)
@JmixModule(id = "io.jmix.saml.test", dependsOn = SamlConfiguration.class)
public class SamlTestConfiguration {
}

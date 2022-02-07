package io.jmix.oidc;

import io.jmix.core.annotation.JmixModule;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@EnableAutoConfiguration
@Import(OidcConfiguration.class)
@JmixModule(id = "io.jmix.oidc.test", dependsOn = OidcConfiguration.class)
public class OidcTestConfiguration {
}

package sample.base;

import io.jmix.core.annotation.JmixModule;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@JmixModule(id = "sample-base", dependsOn = EclipselinkConfiguration.class)
public class BaseConfiguration {
}

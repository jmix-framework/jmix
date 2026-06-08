package sample.app;

import io.jmix.core.annotation.JmixModule;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@JmixModule(id = "sample-app", dependsOn = EclipselinkConfiguration.class)
public class AppConfiguration {
}

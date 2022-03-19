package test_support_modules.addon;

import io.jmix.core.annotation.JmixModule;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@JmixModule(dependsOn = {EclipselinkConfiguration.class})
public class TestAddonConfiguration {
}

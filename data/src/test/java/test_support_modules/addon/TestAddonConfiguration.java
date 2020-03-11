package test_support_modules.addon;

import io.jmix.core.annotation.JmixModule;
import io.jmix.data.JmixDataConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@JmixModule(dependsOn = {JmixDataConfiguration.class})
public class TestAddonConfiguration {
}

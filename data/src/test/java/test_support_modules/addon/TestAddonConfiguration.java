package test_support_modules.addon;

import io.jmix.core.annotation.JmixModule;
import io.jmix.data.DataConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@JmixModule(dependsOn = {DataConfiguration.class})
public class TestAddonConfiguration {
}

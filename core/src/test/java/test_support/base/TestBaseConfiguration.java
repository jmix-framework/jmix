package test_support.base;


import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@JmixModule(dependsOn = CoreConfiguration.class)
@PropertySource(name = "test_support.base", value = "classpath:/test_support/base/base-module.properties")
public class TestBaseConfiguration {
}

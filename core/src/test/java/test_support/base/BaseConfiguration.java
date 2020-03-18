package test_support.base;


import io.jmix.core.JmixCoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import org.springframework.context.annotation.Configuration;

@Configuration
@JmixModule(dependsOn = JmixCoreConfiguration.class)
public class BaseConfiguration {
}

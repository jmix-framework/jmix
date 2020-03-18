package test_support.addon1;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.annotation.JmixProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import test_support.base.BaseConfiguration;

@Configuration
@ComponentScan
@JmixModule(dependsOn = BaseConfiguration.class, properties = {
        @JmixProperty(name = "jmix.viewsConfig", value = "test_support/addon1/views.xml", append = true),
        @JmixProperty(name = "prop1", value = "addon1_prop1", append = true),
        @JmixProperty(name = "prop2", value = "addon1_prop2", append = true),
        @JmixProperty(name = "prop_to_override", value = "addon1_prop3")
})
public class TestAddon1Configuration {
}

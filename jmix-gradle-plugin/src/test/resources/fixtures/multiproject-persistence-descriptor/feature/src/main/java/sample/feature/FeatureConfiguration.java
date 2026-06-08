package sample.feature;

import io.jmix.core.annotation.JmixModule;
import org.springframework.context.annotation.Configuration;
import sample.base.BaseConfiguration;

@Configuration
@JmixModule(id = "sample-feature", dependsOn = BaseConfiguration.class)
public class FeatureConfiguration {
}

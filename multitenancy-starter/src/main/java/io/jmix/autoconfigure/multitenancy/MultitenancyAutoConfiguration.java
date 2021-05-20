package io.jmix.autoconfigure.multitenancy;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.multitenancy.MultitenancyConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({CoreConfiguration.class, DataConfiguration.class, MultitenancyConfiguration.class})
public class MultitenancyAutoConfiguration {
}

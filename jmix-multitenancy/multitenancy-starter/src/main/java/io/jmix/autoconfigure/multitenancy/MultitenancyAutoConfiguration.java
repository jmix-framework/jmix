package io.jmix.autoconfigure.multitenancy;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.multitenancy.MultitenancyConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, MultitenancyConfiguration.class})
public class MultitenancyAutoConfiguration {
}

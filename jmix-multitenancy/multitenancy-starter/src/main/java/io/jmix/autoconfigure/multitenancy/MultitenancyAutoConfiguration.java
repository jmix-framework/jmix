package io.jmix.autoconfigure.multitenancy;

import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.multitenancy.MultitenancyConfiguration;
import io.jmix.multitenancy.observation.JmixTenantContextObservationFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({CoreConfiguration.class, DataConfiguration.class, MultitenancyConfiguration.class})
public class MultitenancyAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "jmix.core", name = "use-user-info-for-observation",
            havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    public JmixTenantContextObservationFilter tenantContextObservationFilter() {
        return new JmixTenantContextObservationFilter();
    }
}

/*
 * Copyright 2026 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test_support;

import io.jmix.core.annotation.JmixModule;
import io.jmix.multitenancy.MultitenancyConfiguration;
import io.jmix.multitenancy.core.TenantProvider;
import io.jmix.multitenancy.Multitenancy;
import io.jmix.search.index.impl.AddonMultitenancyAdapter;
import io.jmix.search.index.impl.MultitenancyAdapter;
import io.jmix.testsupport.config.LiquibaseTestConfiguration;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;

/**
 * Tracks changes of both a tenant-aware and a plain entity, to see what the listener puts into the queue.
 */
@Configuration
@JmixModule
// The add-on is imported, not merely depended on: @JmixModule(dependsOn) only orders modules, it does
// not bring their beans into the context, and the tenant listener is one of those beans.
@Import({BaseSearchTestConfiguration.class, LiquibaseTestConfiguration.class, MultitenancyConfiguration.class})
@PropertySource("classpath:/test_support/test-app.properties")
public class TenantChangeTrackingTestConfiguration {

    /**
     * The base test configuration wires the tenantless registry; here the add-on is genuinely active, which is what
     * the tenant scenarios are about.
     */
    @Bean("search_MultitenancyAdapter")
    @Primary
    public MultitenancyAdapter multitenancyAdapter(Multitenancy multitenancy) {
        return new AddonMultitenancyAdapter(multitenancy);
    }

    @Bean
    public TestTenantEventTracker testTenantEventTracker(ClusterApplicationEventChannelSupplier channelSupplier) {
        return new TestTenantEventTracker(channelSupplier);
    }

    @Bean
    public TestAutoDetectableIndexDefinitionScope testAutoDetectableIndexDefinitionScope() {
        return TestAutoDetectableIndexDefinitionScope.builder()
                .packages("test_support.indexing")
                .build();
    }
}

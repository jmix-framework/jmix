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

package io.jmix.multitenancy.observation;

import io.jmix.core.security.CurrentAuthentication;
import io.jmix.multitenancy.core.TenantProvider;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * An {@link ObservationFilter} that enriches all spans with Jmix tenant-specific context:
 * {@code jmix.user.tenantId} - tenant ID of the current user
 */
public class JmixTenantContextObservationFilter implements ObservationFilter {

    @Autowired
    protected TenantProvider tenantProvider;
    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Override
    public Observation.Context map(Observation.Context context) {
        if (currentAuthentication.isSet()) {
            String currentUserTenantId = tenantProvider.getCurrentUserTenantId();
            context.addLowCardinalityKeyValue(KeyValue.of("jmix.user.tenantId", currentUserTenantId));
        }
        return context;
    }
}

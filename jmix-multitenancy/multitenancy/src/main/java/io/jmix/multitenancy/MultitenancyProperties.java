/*
 * Copyright 2021 Haulmont.
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

package io.jmix.multitenancy;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.multitenancy")
public class MultitenancyProperties {

    /**
     * Parameter name in URL to pass tenant Id.
     */
    final String tenantIdUrlParamName;

    /**
     * Do not load entities through -to-one references if referenced entity has different tenant.
     */
    final boolean joinSameTenantOnlyEnabled;

    public MultitenancyProperties(
            @DefaultValue("tenantId") String tenantIdUrlParamName,
            @DefaultValue("false") boolean joinSameTenantOnlyEnabled
    ) {
        this.tenantIdUrlParamName = tenantIdUrlParamName;
        this.joinSameTenantOnlyEnabled = joinSameTenantOnlyEnabled;
    }

    public String getTenantIdUrlParamName() {
        return tenantIdUrlParamName;
    }

    public boolean isJoinSameTenantOnlyEnabled() {
        return joinSameTenantOnlyEnabled;
    }
}

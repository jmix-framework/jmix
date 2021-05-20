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

package io.jmix.multitenancy.core;

import io.jmix.multitenancy.security.MultitenancyCurrentAuthentication;
import org.springframework.stereotype.Component;

/**
 * The implementation {@link } for Multitenancy.
 */
@Component(TenantProvider.NAME)
public class MultiTenantProviderBean implements TenantProvider {

    private final MultitenancyCurrentAuthentication currentAuthentication;

    public MultiTenantProviderBean(MultitenancyCurrentAuthentication currentAuthentication) {
        this.currentAuthentication = currentAuthentication;
    }

    /**
     * Returns the tenant ID of a logged in user.
     *
     * @return tenant ID of a logged in user, 'no_tenant' if the user doesn't have a tenant ID
     */
    @Override
    public String getCurrentUserTenantId() {
        if (currentAuthentication.isSet() && currentAuthentication.getTenantId() != null) {
            return currentAuthentication.getTenantId();
        }
        return TenantProvider.NO_TENANT;
    }
}

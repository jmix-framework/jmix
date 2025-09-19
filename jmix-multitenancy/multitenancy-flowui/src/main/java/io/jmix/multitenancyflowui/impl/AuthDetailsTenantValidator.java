/*
 * Copyright 2025 Haulmont.
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

package io.jmix.multitenancyflowui.impl;

import io.jmix.core.DataManager;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.multitenancyflowui.MultitenancyUiSupport;
import io.jmix.securityflowui.authentication.AuthDetails;
import io.jmix.securityflowui.authentication.AuthDetailsValidationResult;
import io.jmix.securityflowui.authentication.AuthDetailsValidator;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * Implementation of {@link AuthDetailsValidator} - it checks if tenant of user exists.
 */
public class AuthDetailsTenantValidator implements AuthDetailsValidator {

    protected final DataManager dataManager;
    protected final MultitenancyUiSupport multitenancyUiSupport;

    public AuthDetailsTenantValidator(DataManager dataManager,
                                      MultitenancyUiSupport multitenancyUiSupport) {
        this.dataManager = dataManager;
        this.multitenancyUiSupport = multitenancyUiSupport;
    }

    @Override
    @NonNull
    public AuthDetailsValidationResult validate(@NonNull AuthDetails authDetails) {
        String tenantId = multitenancyUiSupport.extractTenantFromUsername(authDetails.getUsername());
        if (tenantId == null) {
            return AuthDetailsValidationResult.createValid();
        }

        Optional<Tenant> loadedTenantOpt = dataManager.unconstrained().load(Tenant.class)
                .condition(PropertyCondition.equal("tenantId", tenantId))
                .optional();

        if (loadedTenantOpt.isEmpty()) {
            return AuthDetailsValidationResult.createInvalid(String.format("Tenant '%s' doesn't exist", tenantId));
        } else {
            return AuthDetailsValidationResult.createValid();
        }
    }
}

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

package io.jmix.multitenancy.security;

import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.impl.CurrentAuthenticationImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("mten_MultitenancyCurrentAuthentication")
@Primary
public class MultitenancyCurrentAuthenticationImpl extends CurrentAuthenticationImpl implements MultitenancyCurrentAuthentication {

    @Override
    public String getTenantId() {
        Authentication authentication = getAuthentication();
        if (authentication != null) {
            Object details = authentication.getDetails();
            String tenantId = null;
            if (details instanceof ClientDetails) {
                //TODO: compile
                //tenantId = ((ClientDetails) details).getTenantId();
            }
            return tenantId;
        }
        throw new IllegalStateException("Authentication is not set");
    }
}

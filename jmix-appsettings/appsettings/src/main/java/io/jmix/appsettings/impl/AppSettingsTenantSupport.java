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

package io.jmix.appsettings.impl;

import io.jmix.appsettings.AppSettingsTenantProvider;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component("appset_AppSettingsTenantSupport")
public class AppSettingsTenantSupport {

    @Autowired
    protected ObjectProvider<AppSettingsTenantProvider> appSettingsTenantProvider;

    @Nullable
    public String getCurrentTenantId() {
        AppSettingsTenantProvider tenantProvider = appSettingsTenantProvider.getIfAvailable();
        if (tenantProvider == null) {
            return null;
        }

        String tenantId = tenantProvider.getCurrentTenantId();
        return AppSettingsTenantProvider.NO_TENANT.equals(tenantId) ? null : tenantId;
    }
}

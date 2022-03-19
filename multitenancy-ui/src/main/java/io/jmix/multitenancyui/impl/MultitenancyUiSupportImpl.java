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

package io.jmix.multitenancyui.impl;

import com.google.common.base.Strings;
import io.jmix.core.DataManager;
import io.jmix.multitenancy.MultitenancyProperties;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.multitenancyui.MultitenancyUiSupport;
import io.jmix.ui.navigation.UrlRouting;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper for login screen.
 */
@Component("mten_MultitenancyUiSupport")
public class MultitenancyUiSupportImpl implements MultitenancyUiSupport {
    private final MultitenancyProperties multitenancyProperties;
    private final DataManager dataManager;

    private static final String TENANT_USERNAME_SEPARATOR = "|";

    public MultitenancyUiSupportImpl(MultitenancyProperties multitenancyProperties,
                                     DataManager dataManager) {
        this.multitenancyProperties = multitenancyProperties;
        this.dataManager = dataManager;
    }

    @Override
    public String getUsernameByUrl(String username, UrlRouting urlRouting) {
        if (Strings.isNullOrEmpty(multitenancyProperties.getTenantIdUrlParamName())) {
            return username;
        }
        Map<String, String> params = urlRouting.getState().getParams();
        String tenantId = null;
        if (params != null) {
            tenantId = params.get(multitenancyProperties.getTenantIdUrlParamName());
        }
        return concatUsername(username, tenantId);
    }

    @Override
    public List<String> getTenantOptions() {
        return dataManager.load(Tenant.class)
                .all()
                .list()
                .stream()
                .map(Tenant::getTenantId)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsernameByTenant(String username, String tenantId) {
        String newUsername = username != null ? username : "";
        if (newUsername.contains(TENANT_USERNAME_SEPARATOR)) {
            newUsername = newUsername.substring(username.indexOf(TENANT_USERNAME_SEPARATOR) + 1);
        }
        if (Strings.isNullOrEmpty(tenantId)) {
            return newUsername;
        } else {
            return concatUsername(newUsername, tenantId);
        }
    }

    private String concatUsername(String username, String tenantId) {
        if (!Strings.isNullOrEmpty(tenantId)) {
            username = String.format("%s%s%s", tenantId, TENANT_USERNAME_SEPARATOR, username);
        }
        return username;
    }
}

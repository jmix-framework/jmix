/*
 * Copyright 2022 Haulmont.
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

import com.google.common.base.Strings;
import com.vaadin.flow.router.Location;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.multitenancy.MultitenancyProperties;
import io.jmix.multitenancy.entity.Tenant;
import io.jmix.multitenancyflowui.MultitenancyUiSupport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper for login screen.
 */
@Component("mten_MultitenancyUiSupport")
public class MultitenancyUiSupportImpl implements MultitenancyUiSupport {

    protected final MultitenancyProperties multitenancyProperties;
    protected final DataManager dataManager;
    protected final Messages messages;

    protected static final String TENANT_USERNAME_SEPARATOR = "|";

    public MultitenancyUiSupportImpl(MultitenancyProperties multitenancyProperties,
                                     DataManager dataManager,
                                     Messages messages) {
        this.multitenancyProperties = multitenancyProperties;
        this.dataManager = dataManager;
        this.messages = messages;
    }

    @Override
    public String getUsernameByLocation(String username, Location location) {
        String tenantIdUrlParamName = multitenancyProperties.getTenantIdUrlParamName();

        if (Strings.isNullOrEmpty(tenantIdUrlParamName)) {
            return username;
        }

        Map<String, List<String>> params = location.getQueryParameters().getParameters();
        String tenantId = null;

        List<String> tenantIdParams = params.get(tenantIdUrlParamName);

        if (tenantIdParams != null && !tenantIdParams.isEmpty()) {
            tenantId = tenantIdParams.get(0);
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

    protected String concatUsername(String username, String tenantId) {
        if (!Strings.isNullOrEmpty(tenantId)) {
            username = String.format("%s%s%s", tenantId, TENANT_USERNAME_SEPARATOR, username);
        }
        return username;
    }
}

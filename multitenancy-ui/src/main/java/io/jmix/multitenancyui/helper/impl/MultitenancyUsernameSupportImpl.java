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

package io.jmix.multitenancyui.helper.impl;

import com.google.common.base.Strings;
import io.jmix.multitenancy.MultitenancyProperties;
import io.jmix.multitenancyui.helper.MultitenancyUsernameSupport;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Helper for login screen.
 */
@Component("mten_MultitenancyLoginHelper")
public class MultitenancyUsernameSupportImpl implements MultitenancyUsernameSupport {

    private final MultitenancyProperties multitenancyProperties;
    private static final String TENANT_USERNAME_SEPARATOR = "\\";

    public MultitenancyUsernameSupportImpl(MultitenancyProperties multitenancyProperties) {
        this.multitenancyProperties = multitenancyProperties;
    }

    @Override
    public String getMultitenancyUsername(String username, Map<String, String> params) {
        String tenantId = null;
        if (params != null) {
            tenantId = params.get(multitenancyProperties.getTenantIdUrlParamName());
        }
        return createMultitenancyUsername(username, tenantId);
    }

    private String createMultitenancyUsername(String username, String tenantId) {
        if (!Strings.isNullOrEmpty(tenantId)) {
            username = String.format("%s%s%s", tenantId, TENANT_USERNAME_SEPARATOR, username);
        }
        return username;
    }

    @Override
    public String getMultitenancyUsername(String username, String newTenantId) {
        username = username != null ? username : "";
        newTenantId = newTenantId != null ? newTenantId : "";
        if (username.contains(TENANT_USERNAME_SEPARATOR)) {
            username = username.substring(username.indexOf(TENANT_USERNAME_SEPARATOR) + 1);
            return username;
        }
        return createMultitenancyUsername(username, newTenantId);
    }
}

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

package io.jmix.multitenancy.core.impl;

import io.jmix.core.annotation.TenantId;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.multitenancy.core.TenantProvider;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.WeakHashMap;

/**
 * Implementation of {@link TenantProvider} based on {@link CurrentAuthentication}.
 */
@Component("mten_TenantProvider")
public class TenantProviderImpl implements TenantProvider {

    protected WeakHashMap<Class<?>, String> fieldsCache = new WeakHashMap<>();

    private final CurrentAuthentication currentAuthentication;
    private final CurrentUserSubstitution currentUserSubstitution;

    public TenantProviderImpl(CurrentAuthentication currentAuthentication,
                              CurrentUserSubstitution currentUserSubstitution) {
        this.currentAuthentication = currentAuthentication;
        this.currentUserSubstitution = currentUserSubstitution;
    }

    /**
     * Returns the tenant ID of a logged in user.
     *
     * @return tenant ID of a logged in user, 'no_tenant' if the user doesn't have a tenant ID
     */
    @Override
    public String getCurrentUserTenantId() {
        if (!currentAuthentication.isSet()) {
            return TenantProvider.NO_TENANT;
        }

        if (!(currentAuthentication.getAuthentication().getPrincipal() instanceof UserDetails)) {
            return TenantProvider.NO_TENANT;
        }

        UserDetails userDetails = currentUserSubstitution.getEffectiveUser();
        String tenantIdFieldName = getTenantIdFieldName(userDetails.getClass());

        if (tenantIdFieldName == null) {
            return TenantProvider.NO_TENANT;
        }

        String tenantId = (String) ReflectionHelper.getFieldValue(userDetails, tenantIdFieldName);
        //noinspection ConstantValue
        return tenantId != null ? tenantId : TenantProvider.NO_TENANT;
    }

    @Nullable
    protected String getTenantIdFieldName(Class<?> clazz) {
        String tenantField = fieldsCache.get(clazz);
        if (tenantField != null) {
            return tenantField;
        }

        for (Class<?> type = clazz; type != null; type = type.getSuperclass()) {
            Field[] declaredFields = type.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(TenantId.class)) {
                    fieldsCache.put(clazz, field.getName());
                    return field.getName();
                }
            }
        }

        return null;
    }
}

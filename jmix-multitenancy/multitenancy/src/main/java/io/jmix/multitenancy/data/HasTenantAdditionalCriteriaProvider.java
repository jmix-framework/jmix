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

package io.jmix.multitenancy.data;

import io.jmix.core.Metadata;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.eclipselink.persistence.AdditionalCriteriaProvider;
import io.jmix.multitenancy.core.TenantEntityOperation;
import io.jmix.multitenancy.core.TenantProvider;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation of additional criteria for multi tenants.
 */
@Component("mten_HasTenantAdditionalCriteriaProvider")
public class HasTenantAdditionalCriteriaProvider implements AdditionalCriteriaProvider {

    private static final String TENANT_ID = "tenantId";

    private final TenantProvider tenantProvider;
    private final TenantEntityOperation tenantEntityOperation;
    private final Metadata metadata;

    public HasTenantAdditionalCriteriaProvider(TenantProvider tenantProvider,
                                               TenantEntityOperation tenantEntityOperation,
                                               Metadata metadata) {
        this.tenantProvider = tenantProvider;
        this.tenantEntityOperation = tenantEntityOperation;
        this.metadata = metadata;
    }

    @Override
    public boolean requiresAdditionalCriteria(Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        if (entityClass.equals(metaClass.getJavaClass())) {
            return tenantEntityOperation.findTenantProperty(entityClass) != null;
        } else {
            return false;
        }
    }

    @Override
    public String getAdditionalCriteria(Class<?> entityClass) {
        MetaProperty metaProperty = tenantEntityOperation.findTenantProperty(entityClass);
        return String.format("(:tenantId = '%s' or this.%s = :tenantId)", TenantProvider.NO_TENANT, metaProperty.getName());
    }

    @Nullable
    @Override
    public Map<String, Object> getCriteriaParameters() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(TENANT_ID, tenantProvider.getCurrentUserTenantId());
        return parameters;
    }
}

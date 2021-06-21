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

import io.jmix.core.Metadata;
import io.jmix.core.annotation.TenantId;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.multitenancy.core.TenantEntityOperation;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Helper for working with tenant entity.
 */
@Component("mten_TenantEntityOperation")
public class TenantEntityOperationImpl implements TenantEntityOperation {

    private static final Logger log = LoggerFactory.getLogger(TenantEntityOperationImpl.class);

    private final Metadata metadata;

    public TenantEntityOperationImpl(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Returns MetaProperty of Tenant Id for some class
     *
     * @param entityClass entity class
     * @return MetaProperty instance. Return if not found.
     */
    public MetaProperty findTenantProperty(Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        Field tenantField = findTenantField(entityClass);
        if (tenantField == null) {
            log.trace("Entity {} does not have an field marked @TenantId annotation", metaClass.getName());
            return null;
        }
        return metaClass.getProperty(tenantField.getName());

    }

    @Nullable
    private Field findTenantField(Class<?> entityClass) {
        return Arrays.stream(FieldUtils.getAllFields(entityClass))
                .filter(f -> f.isAnnotationPresent(TenantId.class))
                .findFirst().orElse(null);
    }


    /**
     * Set the Tenant Id for some entity
     *
     * @param entity   instance
     * @param tenantId tenant id
     */
    public void setTenant(Object entity, String tenantId) {
        MetaProperty property = findTenantProperty(entity.getClass());
        if (property != null) {
            EntityValues.setValue(entity, property.getName(), tenantId);
        }
    }


}

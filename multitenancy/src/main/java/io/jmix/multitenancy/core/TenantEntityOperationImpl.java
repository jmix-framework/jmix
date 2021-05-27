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

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.multitenancy.data.TenantAssigmentRepository;
import io.jmix.multitenancy.data.TenantRepository;
import io.jmix.multitenancy.entity.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Helper for working with Tenant.
 */
@Component("mten_TenantEntityOperation")
public class TenantEntityOperationImpl {

    private static final Logger log = LoggerFactory.getLogger(TenantEntityOperationImpl.class);

    private final Metadata metadata;
    private final TenantRepository tenantRepository;
    private final MetadataTools metadataTools;
    private final TenantAssigmentRepository tenantAssigmentRepository;

    public TenantEntityOperationImpl(Metadata metadata,
                                     TenantRepository tenantRepository,
                                     MetadataTools metadataTools,
                                     TenantAssigmentRepository tenantAssigmentRepository) {
        this.metadata = metadata;
        this.tenantRepository = tenantRepository;
        this.metadataTools = metadataTools;
        this.tenantAssigmentRepository = tenantAssigmentRepository;
    }

    /**
     * Returns MetaProperty of Tenant Id for some class
     *
     * @param entityClass entity class
     * @return MetaProperty instance. Return if not found.
     */
    public MetaProperty getTenantMetaProperty(Class<?> entityClass) {
        MetaClass metaClass = metadata.getClass(entityClass);
        //TODO: compile
        String tenantIdNameProperty = null;
        //String tenantIdNameProperty = metadataTools.findTenantIdProperty(metaClass.getJavaClass());
        if (tenantIdNameProperty == null) {
            log.warn("Entity {} does not have an field marked @TenantId annotation", metaClass.getName());
            return null;
        }
        return metaClass.getProperty(tenantIdNameProperty);

    }


    /**
     * Set the Tenant Id for some entity
     *
     * @param entity   instance
     * @param tenantId tenant id
     */
    public void setTenant(Object entity, String tenantId) {
        MetaProperty property = getTenantMetaProperty(entity.getClass());
        if (property != null) {
            EntityValues.setValue(entity, property.getName(), tenantId);
        }
    }


    /**
     * Returns the Tenant for some entity
     *
     * @param entity instance
     * @return Tenant instance.
     */

    public Tenant getTenant(Object entity) {
        MetaProperty property = getTenantMetaProperty(entity.getClass());
        if (property != null) {
            String tenantId = EntityValues.getValue(entity, property.getName());
            return tenantRepository.findTenantById(tenantId);
        }
        return null;
    }

    /**
     * Returns the Tenant Id for some entity
     *
     * @param entity instance
     * @return String the value of Tenant Id.
     */

    public String getTenantId(Object entity) {
        MetaProperty property = getTenantMetaProperty(entity.getClass());
        if (property == null) {
            return null;
        }
        return EntityValues.getValue(entity, property.getName());
    }

    @Nullable
    public String getTenantIdByUsername(String username) {
        return tenantAssigmentRepository.findAssigmentByUsername(username).map(tenantAssigmentEntity -> tenantAssigmentEntity.getTenant().getTenantId()).orElse(null);
    }


}

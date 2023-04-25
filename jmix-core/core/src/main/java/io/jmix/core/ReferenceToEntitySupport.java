/*
 * Copyright 2019 Haulmont.
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

package io.jmix.core;

import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nullable;
import java.util.UUID;

/**
 * Utility class to provide common functionality for entities with different type of primary keys
 */
@Component("core_ReferenceToEntitySupport")
public class ReferenceToEntitySupport {

    @Autowired
    protected MetadataTools metadataTools;
    @Autowired
    protected Metadata metadata;

    /**
     * @param entity entity
     * @return entity id to store in database
     */
    public Object getReferenceId(Object entity) {
        if (EntityValues.isUuidSupported(entity)) {
            return EntityValues.getUuid(entity);
        }
        return EntityValues.getId(entity);
    }

    /**
     * @param entity entity
     * @return entity id for links
     */
    @Nullable
    public Object getReferenceIdForLink(Object entity) {
        Object entityId = EntityValues.getId(entity);
        if (entityId == null)
            return null;
        if (metadataTools.hasCompositePrimaryKey(metadata.getClass(entity))) {
            if (EntityValues.isUuidSupported(entity))
                return EntityValues.getUuid(entity);
            else
                throw new IllegalArgumentException(
                        String.format("Unsupported primary key type: %s", entityId.getClass().getSimpleName()));
        }
        return entityId;
    }

    /**
     * @param metaClass of entity
     * @return metaProperty name for storing corresponding primary key in the database
     */
    public String getReferenceIdPropertyName(MetaClass metaClass) {
        if (metadataTools.hasUuid(metaClass)) {
            return "entityId";
        }
        MetaProperty primaryKey = metadataTools.getPrimaryKeyProperty(metaClass);

        if (primaryKey != null) {
            Class type = primaryKey.getJavaType();
            if (UUID.class.equals(type)) {
                return "entityId";
            } else if (Long.class.equals(type)) {
                return "longEntityId";
            } else if (Integer.class.equals(type)) {
                return "intEntityId";
            } else if (String.class.equals(type)) {
                return "stringEntityId";
            } else {
                throw new IllegalStateException(
                        String.format("Unsupported primary key type: %s for %s", type.getSimpleName(), metaClass.getName()));
            }
        } else {
            throw new IllegalStateException(
                    String.format("Primary key not found for %s", metaClass.getName()));
        }
    }

    /**
     * @param metaClass of entity
     * @return metaProperty name for loading entity from database by primary key stored in the database
     */
    public String getPrimaryKeyForLoadingEntity(MetaClass metaClass) {
        if (metadataTools.hasUuid(metaClass)) {
            MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
            if (primaryKeyProperty != null && !UUID.class.isAssignableFrom(primaryKeyProperty.getJavaType()))
                return metadataTools.getUuidPropertyName(metaClass.getJavaClass());
        }
        return metadataTools.getPrimaryKeyName(metaClass);
    }

    /**
     * @param metaClass of entity
     * @return metaProperty name for loading entity from database by primary key for links
     */
    public String getPrimaryKeyForLoadingEntityFromLink(MetaClass metaClass) {
        if (!metadataTools.hasCompositePrimaryKey(metaClass))
            return metadataTools.getPrimaryKeyName(metaClass);
        if (metadataTools.hasUuid(metaClass)) {
            MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
            if (primaryKeyProperty != null && !UUID.class.isAssignableFrom(primaryKeyProperty.getJavaType()))
                return metadataTools.getUuidPropertyName(metaClass.getJavaClass());
        }
        throw new IllegalStateException(
                String.format("Unsupported primary key type for %s", metaClass.getName()));
    }
}

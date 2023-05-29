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

package com.haulmont.cuba.core.global;

import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.UuidProvider;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Builds {@link EntityLoadInfo} objects and provides additional methods for working with them.
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.core.IdSerialization}.
 */
@Deprecated
@Component(EntityLoadInfoBuilder.NAME)
public class EntityLoadInfoBuilder {

    public static final String NAME = "cuba_EntityLoadInfoBuilder";

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Create a new info instance.
     *
     * @param entity   entity instance
     * @param viewName view name, can be null
     * @return info instance
     */
    public EntityLoadInfo create(Entity entity, @Nullable String viewName) {
        Objects.requireNonNull(entity, "entity is null");

        MetaClass metaClass = metadata.getClass(entity);

        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        boolean stringKey = primaryKeyProperty != null && primaryKeyProperty.getJavaType().equals(String.class);

        return new EntityLoadInfo(EntityValues.getId(entity), metaClass, viewName, stringKey);
    }

    /**
     * Create a new info instance with empty view name.
     *
     * @param entity entity instance
     * @return info instance
     */
    public EntityLoadInfo create(Entity entity) {
        return create(entity, null);
    }

    /**
     * Parse an info from the string.
     *
     * @param str string representation of the info. See {@link EntityLoadInfo} for formats.
     * @return info instance or null if the string can not be parsed. Any exception is silently swallowed.
     */
    @Nullable
    public EntityLoadInfo parse(String str) {
        boolean isNew = false;
        if (str.startsWith(EntityLoadInfo.NEW_PREFIX)) {
            str = str.substring("NEW-".length());
            isNew = true;
        }

        int idDashPos = str.indexOf('-');
        if (idDashPos == -1) {
            if (isNew) {
                MetaClass metaClass = metadata.getSession().findClass(str);
                if (metaClass == null) {
                    return null;
                }
                Entity entity = (Entity) metadata.create(metaClass);
                MetaProperty primaryKeyProp = metadataTools.getPrimaryKeyProperty(metaClass);
                boolean stringKey = primaryKeyProp != null && primaryKeyProp.getJavaType().equals(String.class);
                return new EntityLoadInfo(EntityValues.getId(entity), metaClass, null, stringKey, true);
            }
            return null;
        }

        String entityName = str.substring(0, idDashPos);
        MetaClass metaClass = metadata.getSession().findClass(entityName);
        if (metaClass == null) {
            return null;
        }

        Object id;
        String viewName;
        boolean stringKey = false;

        MetaProperty primaryKeyProp = metadataTools.getPrimaryKeyProperty(metaClass);
        if (primaryKeyProp == null)
            return null;

        if (primaryKeyProp.getJavaType().equals(UUID.class)) {
            int viewDashPos = -1;
            int dashCount = StringUtils.countMatches(str, "-");
            if (dashCount < 5) {
                return null;
            }
            if (dashCount >= 6) {
                int i = 0;
                while (i < 6) {
                    viewDashPos = str.indexOf('-', viewDashPos + 1);
                    i++;
                }

                viewName = str.substring(viewDashPos + 1);
            } else {
                viewDashPos = str.length();
                viewName = null;
            }
            String entityIdStr = str.substring(idDashPos + 1, viewDashPos);
            try {
                id = UuidProvider.fromString(entityIdStr);
            } catch (Exception e) {
                return null;
            }
        } else {
            String entityIdStr;
            if (primaryKeyProp.getJavaType().equals(String.class)) {
                stringKey = true;
                int viewDashPos = str.indexOf("}-", idDashPos + 2);
                if (viewDashPos > -1) {
                    viewName = str.substring(viewDashPos + 2);
                } else {
                    viewDashPos = str.length() - 1;
                    viewName = null;
                }
                entityIdStr = str.substring(idDashPos + 2, viewDashPos);
            } else {
                int viewDashPos = str.indexOf('-', idDashPos + 1);
                if (viewDashPos > -1) {
                    viewName = str.substring(viewDashPos + 1);
                } else {
                    viewDashPos = str.length();
                    viewName = null;
                }
                entityIdStr = str.substring(idDashPos + 1, viewDashPos);
            }
            try {
                if (primaryKeyProp.getJavaType().equals(Long.class)) {
                    id = Long.valueOf(entityIdStr);
                } else if (primaryKeyProp.getJavaType().equals(Integer.class)) {
                    id = Integer.valueOf(entityIdStr);
                } else {
                    id = entityIdStr;
                }
            } catch (Exception e) {
                return null;
            }

        }

        return new EntityLoadInfo(id, metaClass, viewName, stringKey, isNew);
    }

    /**
     * Check whether an info about the given entity instance is contained in the collection.
     *
     * @param collection collection of EntityLoadInfo objects
     * @param entity     entity instance
     * @return true if the collection contains an info about the given entity instance. View part of the info is ignored.
     */
    public boolean contains(Collection<EntityLoadInfo> collection, Entity entity) {
        Preconditions.checkNotNullArgument(collection, "collection is null");
        Preconditions.checkNotNullArgument(entity, "entity is null");

        MetaClass metaClass = metadata.getClass(entity);

        for (EntityLoadInfo info : collection) {
            if (metaClass.equals(info.getMetaClass()) && EntityValues.getId(entity).equals(info.getId()))
                return true;
        }
        return false;
    }
}

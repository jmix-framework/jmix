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

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import java.util.Objects;
import java.util.UUID;

/**
 * Class that encapsulates an information needed to load an entity instance.
 * <p>
 * The same as {@link EntityLoadInfo} but always creates an entity of original class if an extended MetaClass is
 * provided.
 *
 */
public class OriginalEntityLoadInfo extends EntityLoadInfo {

    private OriginalEntityLoadInfo(UUID id, MetaClass metaClass, boolean isStringKey) {
        super(id, metaClass, null, isStringKey);
    }

    /**
     * Create a new info instance.
     * @param entity    entity instance
     * @return          info instance
     */
    public static OriginalEntityLoadInfo create(Entity entity) {
        Objects.requireNonNull(entity, "entity is null");

        Metadata metadata = AppBeans.get(Metadata.class);
        MetadataTools metadataTools = AppBeans.get(MetadataTools.class);
        ExtendedEntities extendedEntities = AppBeans.get(ExtendedEntities.class);
        MetaClass metaClass = metadata.getSession().getClass(entity.getClass());

        MetaClass originalMetaClass = extendedEntities.getOriginalMetaClass(metaClass);
        if (originalMetaClass != null) {
            metaClass = originalMetaClass;
        }

        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        boolean stringKey = primaryKeyProperty != null && primaryKeyProperty.getJavaType().equals(String.class);

        return new OriginalEntityLoadInfo((UUID) EntityValues.getId(entity), metaClass, stringKey);
    }
}

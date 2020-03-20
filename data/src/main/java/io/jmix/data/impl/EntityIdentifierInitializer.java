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

package io.jmix.data.impl;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.entity.BaseIntegerIdEntity;
import io.jmix.data.entity.BaseLongIdEntity;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import java.util.List;
import java.util.stream.Collectors;

@Component(EntityIdentifierInitializer.NAME)
public class EntityIdentifierInitializer implements EntityInitializer, Ordered {
    public static final String NAME = "jmix_EntityIdentifierInitializer";

    @Inject
    protected Metadata metadata;
    @Inject
    protected MetadataTools metadataTools;
    @Inject
    protected NumberIdSource numberIdSource;
    @Inject
    protected CoreProperties coreProperties;

    @Override
    public <T> void initEntity(Entity<T> entity) {
        MetaClass metaClass = metadata.getClass(entity.getClass());

        MetaProperty primaryKeyProperty = metadataTools.getPrimaryKeyProperty(metaClass);
        if (primaryKeyProperty != null && metadataTools.isEmbedded(primaryKeyProperty)) {
            // create an instance of embedded ID
            Entity key = metadata.create(primaryKeyProperty.getRange().asClass());
            //noinspection unchecked
            EntityValues.setId(entity, (T) key);
        } else if (entity instanceof BaseLongIdEntity || entity instanceof BaseIntegerIdEntity) {
            if (!coreProperties.isIdGenerationForEntitiesInAdditionalDataStoresEnabled()
                    && !Stores.MAIN.equals(metadataTools.getStoreName(metaClass))) {
                return;
            }
            if (metadataTools.isPersistent(metaClass)) {
                if (entity instanceof BaseLongIdEntity) {
                    ((BaseLongIdEntity) entity).setId(numberIdSource.createLongId(getEntityNameForIdGeneration(metaClass)));
                } else {
                    ((BaseIntegerIdEntity) entity).setId(numberIdSource.createIntegerId(getEntityNameForIdGeneration(metaClass)));
                }
            }
        }
    }

    protected String getEntityNameForIdGeneration(MetaClass metaClass) {
        List<MetaClass> persistentAncestors = metaClass.getAncestors().stream()
                .filter(mc -> metadataTools.isPersistent(mc)) // filter out all mapped superclasses
                .collect(Collectors.toList());
        if (persistentAncestors.size() > 0) {
            MetaClass root = persistentAncestors.get(persistentAncestors.size() - 1);
            Class<?> javaClass = root.getJavaClass();
            Inheritance inheritance = javaClass.getAnnotation(Inheritance.class);
            if (inheritance == null || inheritance.strategy() != InheritanceType.TABLE_PER_CLASS) {
                // use root of inheritance tree if the strategy is JOINED or SINGLE_TABLE because ID is stored in the root table
                return root.getName();
            }
        }
        return metaClass.getName();
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE;
    }
}

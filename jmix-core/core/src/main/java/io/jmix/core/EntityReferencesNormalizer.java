/*
 * Copyright (c) 2008-2020 Haulmont.
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
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Normalizes references between items of a collection.
 */
@Component("core_EntityReferencesNormalizer")
public class EntityReferencesNormalizer {

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private Metadata metadata;

    /**
     * For each entity in the collection, updates reference properties to point to instances which are items of
     * the collection.
     */
    public void updateReferences(Collection<Object> entities) {
        updateReferences(entities, entities);
    }

    /**
     * For each entity in the first collection, updates reference properties to point to instances from
     * the second collection.
     */
    public void updateReferences(Collection<Object> entities, Collection<Object> references) {
        for (Object entity : entities) {
            if (entity == null)
                continue;
            for (Object refEntity : references) {
                if (entity != refEntity) {
                    updateReferences(entity, refEntity, new HashSet<>());
                }
            }
        }
    }

    private void updateReferences(Object entity, Object refEntity, Set<Object> visited) {
        if (visited.contains(entity))
            return;
        visited.add(entity);

        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (!property.getRange().isClass() || !isPropertyAssignableFrom(property, refEntity))
                continue;
            if (entityStates.isLoaded(entity, property.getName())) {
                if (property.getRange().getCardinality().isMany()) {
                    Collection<?> collection = EntityValues.getValue(entity, property.getName());
                    if (collection != null) {
                        for (Object obj : new ArrayList<>(collection)) {
                            Object itemEntity = obj;
                            if (itemEntity != refEntity && getId(itemEntity).equals(getId(refEntity))) {
                                itemEntity = updateCollection(collection, itemEntity, refEntity);
                            }
                            updateReferences(itemEntity, refEntity, visited);
                        }
                    }
                } else {
                    Object propEntity = EntityValues.getValue(entity, property.getName());
                    if (propEntity != null) {
                        if (propEntity != refEntity && getId(propEntity).equals(getId(refEntity))) {
                            if (property.isReadOnly() && !metadataTools.isJpa(property)) {
                                continue;
                            }
                            EntityValues.setValue(entity, property.getName(), refEntity, false);
                        } else {
                            updateReferences(propEntity, refEntity, visited);
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object updateCollection(Collection collection, Object itemEntity, Object refEntity) {
        if (collection instanceof List) {
            List list = (List) collection;
            int i = list.indexOf(itemEntity);
            list.set(i, refEntity);
        } else {
            collection.remove(itemEntity);
            collection.add(refEntity);
        }
        return refEntity;
    }

    private boolean isPropertyAssignableFrom(MetaProperty property, Object entity) {
        Class<Object> propertyClass = property.getRange().asClass().getJavaClass();
        return propertyClass.isAssignableFrom(entity.getClass());
    }

    private Object getId(Object entity) {
        return EntityValues.getGeneratedId(entity);
    }
}

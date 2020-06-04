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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Normalizes references between items of a collection.
 */
@Component(EntityReferencesNormalizer.NAME)
public class EntityReferencesNormalizer {

    public static final String NAME = "core_EntityReferencesNormalizer";

    @Autowired
    private EntityStates entityStates;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private Metadata metadata;

    /**
     * For each entity in the collection, updates to-one reference properties to point to instances which are items of
     * the collection.
     */
    public void updateReferences(Collection<Entity> entities) {
        updateReferences(entities, entities);
    }

    /**
     * For each entity in the first collection, updates to-one reference properties to point to instances from
     * the second collection.
     */
    public void updateReferences(Collection<Entity> entities, Collection<Entity> references) {
        for (Entity entity : entities) {
            if (entity == null)
                continue;
            for (Entity refEntity : references) {
                if (entity != refEntity) {
                    updateReferences(entity, refEntity, new HashSet<>());                }
            }
        }
    }

    private void updateReferences(Entity entity, Entity refEntity, Set<Entity> visited) {
        if (visited.contains(entity))
            return;
        visited.add(entity);

        MetaClass refEntityMetaClass = metadata.getClass(refEntity);
        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (!property.getRange().isClass() || !property.getRange().asClass().equals(refEntityMetaClass))
                continue;
            if (entityStates.isLoaded(entity, property.getName())) {
                if (property.getRange().getCardinality().isMany()) {
                    Collection collection = EntityValues.getValue(entity, property.getName());
                    if (collection != null) {
                        for (Object obj : collection) {
                            updateReferences((Entity) obj, refEntity, visited);
                        }
                    }
                } else {
                    Entity value = EntityValues.getValue(entity, property.getName());
                    if (value != null) {
                        if (EntityValues.getId(value) != null && EntityValues.getId(value).equals(EntityValues.getId(refEntity))) {
                            if (property.isReadOnly() && !metadataTools.isPersistent(property)) {
                                continue;
                            }
                            EntityValues.setValue(entity, property.getName(), refEntity, false);
                        } else {
                            updateReferences(value, refEntity, visited);
                        }
                    }
                }
            }
        }
    }
}

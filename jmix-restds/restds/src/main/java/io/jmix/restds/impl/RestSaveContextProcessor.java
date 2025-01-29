/*
 * Copyright 2024 Haulmont.
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

package io.jmix.restds.impl;

import io.jmix.core.FileRef;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.SaveContext;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component("restds_RestSaveContextProcessor")
public class RestSaveContextProcessor {

    private static final Logger log = LoggerFactory.getLogger(RestSaveContextProcessor.class);
    private final Metadata metadata;
    private final MetadataTools metadataTools;

    public RestSaveContextProcessor(Metadata metadata, MetadataTools metadataTools) {
        this.metadata = metadata;
        this.metadataTools = metadataTools;
    }

    /**
     * Cleans up entitiesToSave and entitiesToRemove collections so they don't contain composition items that are
     * managed by the aggregate root entities also present in entitiesToSave.
     * <p>
     * Assigns root entity to inverse properties of composition items.
     * <p>
     * Looks for FileRef objects down to the object graphs and returns them.
     */
    public Set<FileRef> process(SaveContext saveContext) {
        Set<Object> compositionItems = new HashSet<>();
        Set<FileRef> fileRefs = new HashSet<>();

        for (Object rootEntity : saveContext.getEntitiesToSave()) {
            metadataTools.traverseAttributes(rootEntity, (entity, property) -> {
                updateCompositionItems(entity, property, compositionItems);
                collectFileRefs(entity, property, fileRefs);
            });
        }

        saveContext.getEntitiesToSave().removeAll(compositionItems);

        saveContext.getEntitiesToRemove().removeIf(entity ->
                isClassContainedInCompositions(saveContext.getEntitiesToSave(), entity));

        return fileRefs;
    }

    private void updateCompositionItems(Object entity, MetaProperty property, Set<Object> compositionItems) {
        if (property.getRange().isClass() && property.getType() == MetaProperty.Type.COMPOSITION) {
            Object value = EntityValues.getValue(entity, property.getName());
            if (value != null) {
                if (value instanceof Collection) {
                    for (Object item : ((Collection<?>) value)) {
                        updateInverseProperty(entity, property, item);
                        compositionItems.add(item);
                    }
                } else {
                    updateInverseProperty(entity, property, value);
                    compositionItems.add(value);
                }
            }
        }
    }

    private void collectFileRefs(Object entity, MetaProperty property, Set<FileRef> fileRefs) {
        if (property.getRange().isDatatype() && FileRef.class.isAssignableFrom(property.getRange().asDatatype().getJavaClass())) {
            FileRef value = EntityValues.getValue(entity, property.getName());
            if (value != null) {
                fileRefs.add(value);
            }
        }
    }

    private void updateInverseProperty(Object entity, MetaProperty compositionProperty, Object compositionItem) {
        MetaProperty inverseProperty = compositionProperty.getInverse();
        if (inverseProperty != null) {
            EntityValues.setValue(compositionItem, inverseProperty.getName(), entity);
        } else {
            log.warn("Cannot update inverse property for composition {}.{}. Use @Composition(inverse = \"foo\") to define inverse property",
                    entity.getClass().getName(), compositionProperty.getName());
        }
    }

    private boolean isClassContainedInCompositions(Set<Object> rootEntities, Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        for (Object rootEntity : rootEntities) {
            if (rootEntity.equals(entity) || metaClass.equals(metadata.getClass(rootEntity)))
                continue;
            if (isClassContainedInCompositionsRecursive(rootEntity, entity, metaClass, new HashSet<>()))
                return true;
        }
        return false;
    }

    private boolean isClassContainedInCompositionsRecursive(Object rootEntity, Object entity, MetaClass metaClass, HashSet<Object> visited) {
        if (visited.contains(entity))
            return false;
        visited.add(entity);

        for (MetaProperty property : metadata.getClass(rootEntity).getProperties()) {
            if (property.getRange().isClass()) {
                if (property.getType() == MetaProperty.Type.COMPOSITION && property.getRange().asClass().equals(metaClass)) {
                    return true;
                }
                Object value = EntityValues.getValue(rootEntity, property.getName());
                if (value != null) {
                    if (value instanceof Collection) {
                        for (Object item : ((Collection<?>) value)) {
                            if (isClassContainedInCompositionsRecursive(item, entity, metaClass, visited))
                                return true;
                        }
                    } else if (isClassContainedInCompositionsRecursive(value, entity, metaClass, visited)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}

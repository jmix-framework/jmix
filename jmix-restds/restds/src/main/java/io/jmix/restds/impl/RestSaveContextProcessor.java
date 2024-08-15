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

import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component("restds_RestSaveContextProcessor")
public class RestSaveContextProcessor {

    private final Metadata metadata;

    public RestSaveContextProcessor(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * Cleans up entitiesToSave and entitiesToRemove collections so they don't contain composition items that are
     * managed by the aggregate root entities also present in entitiesToSave.
     */
    public void normalizeCompositionItems(SaveContext saveContext) {
        Set<Object> entitiesToSave = saveContext.getEntitiesToSave().stream()
                .filter(entity ->
                        !isContainedInCompositions(saveContext.getEntitiesToSave(), entity))
                .collect(Collectors.toSet());

        Set<Object> entitiesToRemove = saveContext.getEntitiesToRemove().stream()
                .filter(entity ->
                        !isClassContainedInCompositions(entitiesToSave, entity))
                .collect(Collectors.toSet());

        saveContext.getEntitiesToSave().retainAll(entitiesToSave);
        saveContext.getEntitiesToRemove().retainAll(entitiesToRemove);
    }

    private boolean isContainedInCompositions(Set<?> rootEntities, Object entity) {
        for (Object rootEntity : rootEntities) {
            if (rootEntity.equals(entity))
                continue;
            if (isContainedInCompositionsRecursive(rootEntity, entity, new HashSet<>()))
                return true;
        }
        return false;
    }

    private boolean isContainedInCompositionsRecursive(Object rootEntity, Object entity, Set<Object> visited) {
        if (visited.contains(entity))
            return false;
        visited.add(entity);

        for (MetaProperty property : metadata.getClass(rootEntity).getProperties()) {
            if (property.getRange().isClass()) {
                Object value = EntityValues.getValue(rootEntity, property.getName());
                if (value != null) {
                    if (value instanceof Collection) {
                        for (Object item : ((Collection<?>) value)) {
                            if (property.getType() == MetaProperty.Type.COMPOSITION && item.equals(entity))
                                return true;
                            else if (isContainedInCompositionsRecursive(item, entity, visited))
                                return true;
                        }
                    } else {
                        if (property.getType() == MetaProperty.Type.COMPOSITION && value.equals(entity))
                            return true;
                        else if (isContainedInCompositionsRecursive(value, entity, visited))
                            return true;
                    }
                }
            }
        }

        return false;
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

/*
 * Copyright 2020 Haulmont.
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

import com.google.common.collect.Iterables;
import io.jmix.core.*;
import io.jmix.core.entity.EntityPreconditions;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.SecurityState;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.String.format;

@Component("data_EntityAttributesEraser")
public class EntityAttributesEraserImpl implements EntityAttributesEraser {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected UnconstrainedDataManager dataManager;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public ReferencesCollector collectErasingReferences(Collection entityList, Predicate predicate) {
        Set<Object> visited = new LinkedHashSet<>();
        ReferencesCollector referencesCollector = new ReferencesCollector();
        for (Object entity : entityList) {
            EntityPreconditions.checkEntityType(entity);
            traverseEntities(entity, visited, (e, reference, propertyName) -> {
                if (!predicate.test(reference)) {
                    referencesCollector.addReference(e, reference, propertyName);
                    return false;
                }
                return true;
            });
        }
        return referencesCollector;
    }

    public void eraseReferences(EntityAttributesEraser.ReferencesCollector referencesCollector) {
        for (Object entity : referencesCollector.getEntities()) {
            for (String attribute : referencesCollector.getAttributes(entity)) {
                Collection<Object> references = referencesCollector.getReferencesByAttribute(entity, attribute);

                for (Object reference : references) {
                    EntitySystemAccess.getSecurityState(entity).addErasedId(attribute, EntityValues.getId(reference));
                }

                Object value = EntityValues.getValue(entity, attribute);
                if (value instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    Collection<Object> entities = (Collection<Object>) value;
                    entities.removeAll(references);
                } else if (value instanceof Entity) {
                    if (references.contains(value)) {
                        EntityValues.setValue(entity, attribute, null);
                    }
                }
            }
        }
    }

    public void restoreAttributes(Object entity) {
        SecurityState securityState = EntitySystemAccess.getSecurityState(entity);
        MetaClass metaClass = metadata.getClass(entity);
        for (String attrName : securityState.getErasedAttributes()) {
            Collection<Object> ids = securityState.getErasedIds(attrName);
            if (!ids.isEmpty()) {
                MetaProperty metaProperty = metaClass.getProperty(attrName);
                if (Collection.class.isAssignableFrom(metaProperty.getJavaType())) {
                    restoreCollectionAttribute(entity, metaProperty, ids);
                } else if (Entity.class.isAssignableFrom(metaProperty.getJavaType())) {
                    restoreSingleAttribute(entity, metaProperty, ids);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void restoreCollectionAttribute(Object entity, MetaProperty metaProperty, Collection ids) {
        Collection items = EntityValues.getValue(entity, metaProperty.getName());
        if (items == null) {
            throw new RuntimeException(
                    format("Could not restore erased values for property %s because current value because is null. Entity %s.",
                            metaProperty.getName(), entity));
        }
        for (Object id : ids) {
            MetaClass metaClass = metaProperty.getRange().asClass();
            Object reference = dataManager.getReference(metaClass.getJavaClass(), id);
            items.add(reference);
        }
    }

    @SuppressWarnings("unchecked")
    protected void restoreSingleAttribute(Object entity, MetaProperty metaProperty, Collection<Object> ids) {
        Object id = Iterables.getFirst(ids, null);
        assert id != null;
        Object reference = dataManager.getReference((Class<?>) metaProperty.getJavaType(), id);
        EntityValues.setValue(entity, metaProperty.getName(), reference);
    }

    protected void traverseEntities(Object entity, Set<Object> visited, Visitor visitor) {
        if (visited.contains(entity)) {
            return;
        }

        visited.add(entity);

        for (MetaProperty property : metadata.getClass(entity).getProperties()) {
            if (isPersistentEntityProperty(property) && entityStates.isLoaded(entity, property.getName())) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (value instanceof Collection<?>) {
                    //noinspection unchecked
                    for (Object item : (Collection<Object>) value) {
                        if (visitor.visit(entity, item, property.getName())) {
                            traverseEntities(item, visited, visitor);
                        }
                    }
                } else if (value instanceof Entity) {
                    if (visitor.visit(entity, value, property.getName())) {
                        traverseEntities(value, visited, visitor);
                    }
                }
            }
        }
    }

    protected boolean isPersistentEntityProperty(MetaProperty metaProperty) {
        return metaProperty.getRange().isClass() && metadataTools.isJpa(metaProperty);
    }

    protected interface Visitor {
        boolean visit(Object entity, Object reference, String propertyName);
    }
}

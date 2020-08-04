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
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.SecurityState;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

import static java.lang.String.format;

@Component(EntityAttributesEraser.NAME)
public class EntityAttributesEraserImpl implements EntityAttributesEraser {

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected MetadataTools metadataTools;

    @Override
    public ReferencesCollector collectErasingReferences(JmixEntity entity, Predicate<JmixEntity> predicate) {
        return collectErasingReferences(Collections.singletonList(entity), predicate);
    }

    @Override
    public ReferencesCollector collectErasingReferences(Collection<? extends JmixEntity> entityList, Predicate<JmixEntity> predicate) {
        Set<JmixEntity> visited = new LinkedHashSet<>();
        ReferencesCollector referencesCollector = new ReferencesCollector();
        for (JmixEntity entity : entityList) {
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
        for (JmixEntity entity : referencesCollector.getEntities()) {
            for (String attribute : referencesCollector.getAttributes(entity)) {
                Collection<JmixEntity> references = referencesCollector.getReferencesByAttribute(entity, attribute);

                Object value = EntityValues.getValue(entity, attribute);
                if (value instanceof Collection) {
                    @SuppressWarnings("unchecked")
                    Collection<JmixEntity> entities = (Collection<JmixEntity>) value;
                    entities.removeAll(references);
                } else if (value instanceof JmixEntity) {
                    if (references.contains(value)) {
                        EntityValues.setValue((JmixEntity) value, attribute, null);
                    }
                }
            }
        }
    }

    public void restoreAttributes(JmixEntity entity) {
        SecurityState securityState = entity.__getEntityEntry().getSecurityState();
        MetaClass metaClass = metadata.getClass(entity.getClass());
        for (String attrName : securityState.getErasedAttributes()) {
            List<Object> ids = securityState.getErasedIds(attrName);
            if (!ids.isEmpty()) {
                MetaProperty metaProperty = metaClass.getProperty(attrName);
                if (Collection.class.isAssignableFrom(metaProperty.getJavaType())) {
                    restoreCollectionAttribute(entity, metaProperty, ids);
                } else if (JmixEntity.class.isAssignableFrom(metaProperty.getJavaType())) {
                    restoreSingleAttribute(entity, metaProperty, ids);
                }
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void restoreCollectionAttribute(JmixEntity entity, MetaProperty metaProperty, Collection ids) {
        Collection items = EntityValues.getValue(entity, metaProperty.getName());
        if (items == null) {
            throw new RuntimeException(
                    format("Could not restore erased values for property %s because current value because is null. Entity %s.",
                            metaProperty.getName(), entity));
        }
        for (Object id : ids) {
            JmixEntity reference = dataManager.getReference((Class<JmixEntity>) metaProperty.getJavaType(), id);
            items.add(reference);
        }
    }

    @SuppressWarnings("unchecked")
    protected void restoreSingleAttribute(JmixEntity entity, MetaProperty metaProperty, List<Object> ids) {
        Object id = Iterables.getFirst(ids, null);
        assert id != null;
        JmixEntity reference = dataManager.getReference((Class<JmixEntity>) metaProperty.getJavaType(), id);
        EntityValues.setValue(entity, metaProperty.getName(), reference);
    }

    protected void traverseEntities(JmixEntity entity, Set<JmixEntity> visited, Visitor visitor) {
        if (visited.contains(entity)) {
            return;
        }

        visited.add(entity);

        for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
            if (isPersistentEntityProperty(property) && entityStates.isLoaded(entity, property.getName())) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (value instanceof Collection<?>) {
                    //noinspection unchecked
                    for (JmixEntity item : (Collection<JmixEntity>) value) {
                        if (visitor.visit(entity, item, property.getName())) {
                            traverseEntities(item, visited, visitor);
                        }
                    }
                } else if (value instanceof JmixEntity) {
                    JmixEntity valueEntity = (JmixEntity) value;
                    if (visitor.visit(entity, valueEntity, property.getName())) {
                        traverseEntities(valueEntity, visited, visitor);
                    }
                }
            }
        }
    }

    protected boolean isPersistentEntityProperty(MetaProperty metaProperty) {
        return metaProperty.getRange().isClass() && metadataTools.isPersistent(metaProperty);
    }

    protected interface Visitor {
        boolean visit(JmixEntity entity, JmixEntity reference, String propertyName);
    }
}

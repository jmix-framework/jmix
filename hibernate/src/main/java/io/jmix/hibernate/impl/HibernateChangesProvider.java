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

package io.jmix.hibernate.impl;

import io.jmix.core.Entity;
import io.jmix.core.Id;
import io.jmix.core.Metadata;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.event.AttributeChanges;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

import static io.jmix.hibernate.impl.HibernateUtils.initializeAndUnproxy;

@Component("hibernate_HibernateChangesProvider")
public class HibernateChangesProvider {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected LoadedValueProvider loadedValueProvider;

    public boolean hasChanges(Object entity) {
        return !dirtyFields(entity).isEmpty();
    }

    public Set<String> dirtyFields(Object entity) {
        Set<String> dirtyFields = new HashSet<>();
        if (entity instanceof Entity) {
            for (String property : loadedValueProvider.getLoadedProperties(entity)) {
                if (!isEqual(entity, property)) {
                    dirtyFields.add(property);
                }
            }
        }
        return dirtyFields;
    }

    private boolean isEqual(Object entity, String property) {
        Object oldValue = loadedValueProvider.getLoadedValue(entity, property);
        Object newValue = EntityValues.getValue(entity, property);
        if (oldValue instanceof Entity || oldValue instanceof HibernateProxy) {
            return Objects.equals(
                    initializeAndUnproxy(oldValue),
                    initializeAndUnproxy(newValue));
        } else if (oldValue instanceof Collection) {
            Collection<Id> oldIdColl = getIds(oldValue);
            Collection<Id> newIdColl = getIds(newValue);
            return CollectionUtils.isEqualCollection(oldIdColl, newIdColl);
        } else {
            return Objects.equals(newValue, oldValue);
        }
    }

    private Collection<Id> getIds(Object oldValue) {
        Collection<Object> coll = (Collection<Object>) oldValue;
        Collection<Id> idColl = oldValue instanceof List ? new ArrayList<>() : new LinkedHashSet<>();
        for (Object item : coll) {
            Object initializedItem = initializeAndUnproxy(item);
            if (initializedItem != null) {
                idColl.add(Id.of(initializedItem));
            }
        }
        return idColl;
    }

    @SuppressWarnings("unchecked")
    public AttributeChanges getEntityAttributeChanges(@Nullable Object entity) {
        AttributeChanges.Builder builder = AttributeChanges.Builder.create();

        if (entity instanceof Entity) {
            for (String property : dirtyFields(entity)) {
                Object oldValue = loadedValueProvider.getLoadedValue(entity, property);
                if (oldValue instanceof Entity) {
                    builder.withChange(property, Id.of(oldValue));
                } else if (oldValue instanceof Collection) {
                    Collection<Id> idColl = getIds(oldValue);
                    builder.withChange(property, idColl);
                } else {
                    builder.withChange(property, oldValue);
                }
            }
        }

        return builder.build();
    }

    @SuppressWarnings("unchecked")
    public AttributeChanges getEntityAttributeChanges(Object entity, boolean deleted) {
        AttributeChanges.Builder builder = AttributeChanges.Builder.create();

        for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
            if (!property.isReadOnly()) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (deleted) {
                    if (value instanceof Entity) {
                        if (EntitySystemAccess.isEmbeddable(entity)) {
                            builder.withEmbedded(property.getName(),
                                    b -> builder.mergeChanges(getEntityAttributeChanges(value, true)));
                        } else {
                            builder.withChange(property.getName(), Id.of(value));
                        }
                    } else if (value instanceof Collection) {
                        Collection<Object> coll = (Collection<Object>) value;
                        Collection<Id> idColl = value instanceof List ? new ArrayList<>() : new LinkedHashSet<>();
                        for (Object item : coll) {
                            idColl.add(Id.of(item));
                        }
                        builder.withChange(property.getName(), idColl);
                    } else {
                        builder.withChange(property.getName(), value);
                    }
                } else {
                    if (value != null) {
                        builder.withChange(property.getName(), null);
                    }
                }
            }
        }

        if (deleted) {
            // todo dynamic attributes
//            addDynamicAttributeChanges(entity, changes, true);
        }

        return builder.build();
    }
}

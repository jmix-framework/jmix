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
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.hibernate.engine.spi.EntityEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.*;

@Component("hibernate_HibernateChangesProvider")
public class HibernateChangesProvider {

    @Autowired
    protected Metadata metadata;

    public boolean hasChanges(Object entity, EntityEntry entry) {
        return !dirtyFields(entity, entry).isEmpty();
    }

    public Set<String> dirtyFields(Object entity, EntityEntry entry) {
        Set<String> dirtyFields = new HashSet<>();
        for (String propertyName : entry.getPersister().getPropertyNames()) {
            if (!Objects.equals(EntityValues.getValue(entity, propertyName), entry.getLoadedValue(propertyName))) {
                dirtyFields.add(propertyName);
            }
        }
        return dirtyFields;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public AttributeChanges getEntityAttributeChanges(@Nullable Object entity, @Nullable EntityEntry entry) {
        if (entry == null)
            return null;
        Set<AttributeChanges.Change> changes = new HashSet<>();
        Map<String, AttributeChanges> embeddedChanges = new HashMap<>();

        for (String property : dirtyFields(entity, entry)) {
            Object oldValue = entry.getLoadedValue(property);
            if (oldValue instanceof Entity) {
                changes.add(new AttributeChanges.Change(property, Id.of(oldValue)));
            } else if (oldValue instanceof Collection) {
                Collection<Object> coll = (Collection<Object>) oldValue;
                Collection<Id> idColl = oldValue instanceof List ? new ArrayList<>() : new LinkedHashSet<>();
                for (Object item : coll) {
                    idColl.add(Id.of(item));
                }
                changes.add(new AttributeChanges.Change(property, idColl));
            } else {
                Object convertedValue;
                if (entity != null) {
                    MetaClass metaClass = metadata.getClass(entity);
                    convertedValue = convertValueIfNeeded(metaClass.getProperty(property), oldValue);
                } else {
                    convertedValue = oldValue;
                }
                changes.add(new AttributeChanges.Change(property, convertedValue));
            }
        }

        return new AttributeChanges(changes, embeddedChanges);
    }

    private Object convertValueIfNeeded(MetaProperty property, Object value) {
        if (property.getRange().isEnum() && !(value instanceof EnumClass)) {
            for (Object enumValue : property.getRange().asEnumeration().getValues()) {
                if (enumValue instanceof EnumClass && ((EnumClass<?>) enumValue).getId().equals(value)) {
                    return enumValue;
                }
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public AttributeChanges getEntityAttributeChanges(Object entity, boolean deleted) {
        Set<AttributeChanges.Change> changes = new HashSet<>();
        Map<String, AttributeChanges> embeddedChanges = new HashMap<>();

        for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
            if (!property.isReadOnly()) {
                Object value = EntityValues.getValue(entity, property.getName());
                if (deleted) {
                    if (value instanceof Entity) {
                        if (EntitySystemAccess.isEmbeddable(entity)) {
                            embeddedChanges.computeIfAbsent(property.getName(), s -> getEntityAttributeChanges(value, true));
                        } else {
                            changes.add(new AttributeChanges.Change(property.getName(), Id.of(value)));
                        }
                    } else if (value instanceof Collection) {
                        Collection<Object> coll = (Collection<Object>) value;
                        Collection<Id> idColl = value instanceof List ? new ArrayList<>() : new LinkedHashSet<>();
                        for (Object item : coll) {
                            idColl.add(Id.of(item));
                        }
                        changes.add(new AttributeChanges.Change(property.getName(), idColl));
                    } else {
                        changes.add(new AttributeChanges.Change(property.getName(), value));
                    }

                } else {
                    if (value != null) {
                        changes.add(new AttributeChanges.Change(property.getName(), null));
                    }
                }
            }
        }

        if (deleted) {
            // todo dynamic attributes
//            addDynamicAttributeChanges(entity, changes, true);
        }

        return new AttributeChanges(changes, embeddedChanges);
    }
}

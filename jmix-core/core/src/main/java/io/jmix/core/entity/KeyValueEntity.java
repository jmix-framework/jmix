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

package io.jmix.core.entity;

import io.jmix.core.CopyingSystemState;
import io.jmix.core.Entity;
import io.jmix.core.EntityEntry;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.annotation.DisableEnhancing;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.lang.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity that contains a variable set of attributes. For example:
 * <pre>
 * KeyValueEntity company = new KeyValueEntity();
 * company.setValue("email", "info@globex.com");
 * company.setValue("name", "Globex Corporation");
 *
 * KeyValueEntity person = new KeyValueEntity();
 * person.setValue("email", "homer.simpson@mail.com");
 * person.setValue("firstName", "Homer");
 * person.setValue("lastName", "Simpson");
 * </pre>
 */
@JmixEntity(name = "sys_KeyValueEntity")
@SystemLevel
@DisableEnhancing
public class KeyValueEntity
        implements HasInstanceMetaClass, Entity, CopyingSystemState<KeyValueEntity> {

    protected UUID uuid;

    protected Map<String, Object> properties = new LinkedHashMap<>();

    protected String idName;

    protected MetaClass metaClass;

    protected EntityEntry entityEntry;

    protected static class KeyValueEntityEntry extends BaseEntityEntry {
        public KeyValueEntityEntry(Entity source) {
            super(source);
        }

        @Override
        public Object getEntityId() {
            return ((KeyValueEntity) source).getId();
        }

        @Override
        public void setEntityId(Object id) {
            ((KeyValueEntity) source).setId(id);
        }

        @Override
        public Object getGeneratedIdOrNull() {
            return ((KeyValueEntity) source).uuid;
        }

        @Override
        public void setGeneratedId(Object id) {
            ((KeyValueEntity) source).uuid = (UUID) id;
        }

        @Override
        public <T> T getAttributeValue(@NonNull String name) {
            return ((KeyValueEntity) source).getValue(name);
        }

        @Override
        public void setAttributeValue(@NonNull String name, Object value, boolean checkEquals) {
            ((KeyValueEntity) source).setValue(name, value, checkEquals);
        }
    }

    public KeyValueEntity() {
        uuid = UuidProvider.createUuid();
    }

    public MetaClass getInstanceMetaClass() {
        if (metaClass == null)
            throw new IllegalStateException("metaClass is null");
        return metaClass;
    }

    /**
     * Sets a meta-class for this entity instance.
     */
    public void setInstanceMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public boolean hasInstanceMetaClass() {
        return metaClass != null;
    }

    /**
     * @return name of a property that represents this entity id, if set by {@link #setIdName(String)}
     */
    public String getIdName() {
        return idName;
    }

    /**
     * Sets the name of a property that represents this entity id.
     */
    public void setIdName(String idName) {
        this.idName = idName;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(String name) {
        return (T) properties.get(name);
    }

    public void setValue(String name, Object value) {
        setValue(name, value, true);
    }

    public void setValue(String name, Object value, boolean checkEquals) {
        Object oldValue = getValue(name);
        if ((!checkEquals) || (!EntityValues.propertyValueEquals(oldValue, value))) {
            properties.put(name, value);
            ((KeyValueEntityEntry) __getEntityEntry()).firePropertyChanged(name, oldValue, value);
        }
    }

    public Object getId() {
        if (idName == null)
            return uuid;
        else
            return properties.get(idName);
    }

    public void setId(Object id) {
        if (idName == null)
            throw new IllegalStateException("Id name is not set");
        properties.put(idName, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyValueEntity that = (KeyValueEntity) o;
        Object id = getId();
        Object thatId = that.getId();

        if (id != null && thatId != null)
            return id.equals(thatId);

        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        Object id = getId();
        if (id != null)
            return id.hashCode();
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        Object id = null;
        if (idName != null)
            id = properties.get(idName);
        if (id == null)
            id = "?(" + uuid + ")";
        return "sys_KeyValueEntity-" + id;
    }

    @Override
    public EntityEntry __getEntityEntry() {
        return entityEntry == null ? entityEntry = new KeyValueEntityEntry(this) : entityEntry;
    }

    @Override
    public void __copyEntityEntry() {
        KeyValueEntityEntry newEntityEntry = new KeyValueEntityEntry(this);
        newEntityEntry.copy(entityEntry);
        entityEntry = newEntityEntry;
    }

    @Override
    public void copyFrom(KeyValueEntity source) {
        uuid = source.uuid;
        idName = source.idName;
        metaClass = source.metaClass;
    }
}

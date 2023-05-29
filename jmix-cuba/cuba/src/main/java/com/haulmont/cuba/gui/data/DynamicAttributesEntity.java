/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.gui.data;

import io.jmix.core.Entity;
import io.jmix.core.EntityEntry;
import io.jmix.core.UuidProvider;
import io.jmix.core.entity.BaseEntityEntry;
import io.jmix.core.entity.EntityPropertyChangeListener;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.entity.annotation.DisableEnhancing;

import java.util.UUID;

/**
 * Specific entity, delegating all calls to internal BaseGenericIdEntity.
 * <p>
 * Obsolete. Will be removed in future releases.
 */
@DisableEnhancing
public class DynamicAttributesEntity implements Entity {
    private static final long serialVersionUID = -8091230910619941201L;
    protected Entity mainItem;
    protected UUID id;

    protected EntityEntry entityEntry;

    protected static class DynamicAttributesEntityEntry extends BaseEntityEntry {
        public DynamicAttributesEntityEntry(Entity source) {
            super(source);
        }

        @Override
        public Object getEntityId() {
            return ((DynamicAttributesEntity) source).id;
        }

        @Override
        public void setEntityId(Object id) {
        }

        @Override
        public Object getGeneratedIdOrNull() {
            return getEntityId();
        }

        @Override
        public void setGeneratedId(Object id) {
            setEntityId(id);
        }

        @Override
        public <T> T getAttributeValue(String name) {
            return (EntityValues.getValue(((DynamicAttributesEntity) source).mainItem, name));
        }

        @Override
        public void setAttributeValue(String name, Object value, boolean checkEquals) {
            EntityValues.setValue(((DynamicAttributesEntity) source).mainItem, name, value, checkEquals);
        }

        @Override
        public void addPropertyChangeListener(EntityPropertyChangeListener listener) {
            super.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(EntityPropertyChangeListener listener) {
            super.removePropertyChangeListener(listener);
        }

        @Override
        public void removeAllListeners() {
            super.removeAllListeners();
        }
    }

    public DynamicAttributesEntity(Entity mainItem) {
        this.mainItem = mainItem;
        this.id = UuidProvider.createUuid();
    }

    @Override
    public EntityEntry __getEntityEntry() {
        return entityEntry == null ? entityEntry = new DynamicAttributesEntityEntry(this) : entityEntry;
    }

    @Override
    public void __copyEntityEntry() {
        DynamicAttributesEntityEntry newEntityEntry = new DynamicAttributesEntityEntry(this);
        newEntityEntry.copy(entityEntry);
        entityEntry = newEntityEntry;
    }
}
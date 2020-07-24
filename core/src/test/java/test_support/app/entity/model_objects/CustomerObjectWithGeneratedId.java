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

package test_support.app.entity.model_objects;

import io.jmix.core.EntityEntry;
import io.jmix.core.JmixEntity;
import io.jmix.core.entity.BaseEntityEntry;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.impl.EntityInternals;
import io.jmix.core.metamodel.annotation.ModelObject;

import javax.annotation.Nullable;
import java.util.UUID;

@ModelObject
public class CustomerObjectWithGeneratedId implements JmixEntity {

    @JmixId
    @JmixGeneratedValue
    private UUID id;

    private String name;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // TODO Replace with enhancing - begin

    private static class JmixEntityEntry extends BaseEntityEntry {
        public JmixEntityEntry(JmixEntity source) {
            super(source);
        }

        @Nullable
        @Override
        public Object getEntityId() {
            return ((CustomerObjectWithGeneratedId) getSource()).getId();
        }

        @Override
        public void setEntityId(@Nullable Object id) {
            ((CustomerObjectWithGeneratedId) getSource()).setId((UUID) id);
        }

        @Nullable
        @Override
        public Object getGeneratedIdOrNull() {
            return ((CustomerObjectWithGeneratedId) getSource()).getId();
        }
    }

    private EntityEntry _jmixEntityEntry = new JmixEntityEntry(this);

    @Override
    public EntityEntry __getEntityEntry() {
        return _jmixEntityEntry;
    }

    @Override
    public void __copyEntityEntry() {
        JmixEntityEntry newEntry = new JmixEntityEntry(this);
        newEntry.copy(_jmixEntityEntry);
        _jmixEntityEntry = newEntry;
    }

    public boolean equals(Object var1) {
        return EntityInternals.equals(this, var1);
    }

    public int hashCode() {
        return EntityInternals.hashCode(this);
    }

    public String toString() {
        return EntityInternals.toString(this);
    }

    // TODO Replace with enhancing - end
}

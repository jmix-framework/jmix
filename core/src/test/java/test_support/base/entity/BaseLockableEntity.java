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

package test_support.base.entity;

import io.jmix.core.EntityEntry;
import io.jmix.core.entity.BaseEntityEntry;
import io.jmix.core.metamodel.annotation.ModelObject;

import javax.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
@ModelObject(name = "base_BaseLockableEntity")
public class BaseLockableEntity extends BaseUuidEntity {
    protected EntityEntry entityEntry;

    protected static class LockableEntityEntry extends BaseEntityEntry {
        public LockableEntityEntry(BaseLockableEntity source) {
            super(source);
        }

        @Override
        public UUID getEntityId() {
            return ((BaseLockableEntity) source).getId();
        }

        @Override
        public void setEntityId(Object id) {
            ((BaseLockableEntity) source).setId((UUID) id);
        }
    }

    @Override
    public EntityEntry __getEntityEntry() {
        return entityEntry == null ? entityEntry = new BaseLockableEntity.LockableEntityEntry(this) : entityEntry;
    }

    @Override
    public void __copyEntityEntry() {
        BaseLockableEntity.LockableEntityEntry newEntityEntry = new BaseLockableEntity.LockableEntityEntry(this);
        newEntityEntry.copy(entityEntry);
        entityEntry = newEntityEntry;
    }
}

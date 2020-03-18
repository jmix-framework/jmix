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

import io.jmix.core.Entity;
import io.jmix.core.EntityEntry;

public abstract class BaseDbGeneratedIdEntityEntry<K extends Number> extends BaseEntityEntry<IdProxy<K>> {
    protected IdProxy<K> idProxy;

    public BaseDbGeneratedIdEntityEntry(Entity<IdProxy<K>> source) {
        super(source);
    }

    @Override
    public IdProxy<K> getEntityId() {
        if (idProxy == null) {
            idProxy = new IdProxy<K>(getSource());
        }
        // return a copy cleaned from the reference to the entity
        return idProxy.copy();
    }

    @Override
    public void setEntityId(IdProxy<K> idProxy) {
        this.idProxy = idProxy.copy(false);
        setDbGeneratedId(this.idProxy.get());
        this.idProxy.setEntity(getSource());
    }

    public abstract void setDbGeneratedId(K dbId);

    public abstract K getDbGeneratedId();

    @Override
    public void copy(EntityEntry<?> entry) {
        super.copy(entry);
        if (entry instanceof BaseDbGeneratedIdEntityEntry) {
            //noinspection unchecked
            setDbGeneratedId((K) ((BaseDbGeneratedIdEntityEntry) entry).getDbGeneratedId());
        }
    }
}

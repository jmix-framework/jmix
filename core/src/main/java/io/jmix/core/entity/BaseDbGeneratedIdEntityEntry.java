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

public abstract class BaseDbGeneratedIdEntityEntry extends BaseEntityEntry {
    protected IdProxy idProxy;

    public BaseDbGeneratedIdEntityEntry(Entity source) {
        super(source);
    }

    @Override
    public Object getEntityId() {
        if (idProxy == null) {
            idProxy = new IdProxy(getSource());
        }
        // return a copy cleaned from the reference to the entity
        return idProxy.copy();
    }

    @Override
    public void setEntityId(Object idProxy) {
        this.idProxy = ((IdProxy) idProxy).copy(false);
        setDbGeneratedId(this.idProxy.get());
        this.idProxy.setEntity(getSource());
    }

    public abstract void setDbGeneratedId(Object dbId);

    public abstract Object getDbGeneratedId();

    @Override
    public void copy(EntityEntry entry) {
        super.copy(entry);
        if (entry instanceof BaseDbGeneratedIdEntityEntry) {
            //noinspection unchecked
            setDbGeneratedId(((BaseDbGeneratedIdEntityEntry) entry).getDbGeneratedId());
        }
    }
}

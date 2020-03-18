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

package io.jmix.core.entity;

import io.jmix.core.Entity;

/**
 * Used by enhancing process
 */
@SuppressWarnings("unsed")
public class EmbeddableEntityEntry<K> extends BaseEntityEntry<K> {

    @Override
    public boolean isNew() {
        return false;
    }

    @Override
    public boolean isManaged() {
        return false;
    }

    @Override
    public boolean isDetached() {
        return false;
    }

    @Override
    public boolean isRemoved() {
        return false;
    }

    @Override
    public void setNew(boolean _new) {
    }

    @Override
    public void setManaged(boolean managed) {
    }

    @Override
    public void setDetached(boolean detached) {
    }

    @Override
    public void setRemoved(boolean removed) {
    }

    public EmbeddableEntityEntry(Entity source) {
        super(source);
    }

    @Override
    public K getEntityId() {
        return (K) source;
    }

    @Override
    public void setEntityId(K id) {

    }

    @Override
    public boolean isEmbeddable() {
        return true;
    }
}

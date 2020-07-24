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

import io.jmix.core.EntityEntry;
import io.jmix.core.JmixEntity;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * An {@link EntityEntry} implementation for model objects without identifiers.
 * <p>
 * Such entities should not be saved in any persistent storage.
 */
@SuppressWarnings("unused")
public class NoIdEntityEntry extends BaseEntityEntry {

    private long generatedId;

    private static final AtomicLong idGenerator = new AtomicLong(0);

    public NoIdEntityEntry(JmixEntity source) {
        super(source);
        generatedId = idGenerator.incrementAndGet();
    }

    @Override
    public int hashCode() {
        return (int) generatedId;
    }

    @Nullable
    @Override
    public Object getEntityId() {
        return getGeneratedId();
    }

    @Override
    public void setEntityId(@Nullable Object id) {
    }

    @Override
    public Object getGeneratedIdOrNull() {
        return generatedId;
    }

    @Override
    public void copy(EntityEntry entry) {
        super.copy(entry);
        if (entry instanceof NoIdEntityEntry) {
            generatedId = ((NoIdEntityEntry) entry).generatedId;
        }
    }
}

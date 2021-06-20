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
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Used by enhancing process. Subclass of {@link NullableIdEntityEntry} will be created for entity that:
 * <ul>
 * <li>has primary key (an attribute annotated with {@link Id}, {@link EmbeddedId} or {@link JmixId})</li>
 * <li>Primary key is not annotated with {@link JmixGeneratedValue}</li>
 * <li>There is no {@link UUID} attribute annotated with {@link JmixGeneratedValue}</li>
 * </ul>
 */
@SuppressWarnings("unused")
@Internal
public abstract class NullableIdEntityEntry extends BaseEntityEntry {

    private static final long serialVersionUID = 472062751374259587L;

    private long generatedId;

    private static final AtomicLong idGenerator = new AtomicLong(0);

    public NullableIdEntityEntry(Entity source) {
        super(source);
        generatedId = idGenerator.incrementAndGet();
    }

    @Override
    public int hashCode() {
        return 111;
    }

    @Override
    public Object getGeneratedIdOrNull() {
        return generatedId;
    }

    @Override
    public void setGeneratedId(Object id) {
        this.generatedId = (long) id;
    }

    @Override
    public void copy(EntityEntry entry) {
        super.copy(entry);
        if (entry instanceof NullableIdEntityEntry) {
            generatedId = ((NullableIdEntityEntry) entry).generatedId;
        }
    }
}

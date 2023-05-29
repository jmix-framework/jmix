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

package io.jmix.core;

import io.jmix.core.entity.BaseEntityEntry;
import io.jmix.core.entity.EntityPropertyChangeListener;
import io.jmix.core.entity.NullableIdEntityEntry;
import io.jmix.core.entity.SecurityState;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.impl.EntityInternals;

import jakarta.annotation.Nullable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import java.io.Serializable;
import java.util.Collection;


public interface EntityEntry extends Serializable {

    Entity getSource();

    @Nullable
    Object getEntityId();

    void setEntityId(@Nullable Object id);

    /**
     * GeneratedId will be determined at enhancing time as follows:
     * <ol>
     *     <li>primary key ({@link Id}, {@link EmbeddedId} or {@link JmixId} attribute) will be used if it has {@link JmixGeneratedValue} annotation,</li>
     *     <li>any other UUID {@link JmixGeneratedValue} property will be chosen if primary key doesn't  have {@link JmixGeneratedValue} annotation, </li>
     *     <li>primary key or some synthetic id will be used if there is no {@link JmixGeneratedValue} satisfiyng conditions below (see {@code EntityEntry} implementations for details).</li>
     * </ol>
     * <br>
     * This algorithm used for {@link BaseEntityEntry} and {@link NullableIdEntityEntry}. See implementing classes description to clarify whether each of them will be used
     * (directly or through subclass creation during enhancing process)
     */
    @Nullable
    Object getGeneratedIdOrNull();

    default Object getGeneratedId() {
        Object id = getGeneratedIdOrNull();
        if (id == null) {
            throw new IllegalStateException(String.format("Generated ID is null in %s", getSource()));
        }
        return id;
    }

    /**
     * GeneratedId needed to identify entity, including hashCode calculation (see {@link EntityInternals#hashCode(Entity)}).
     * Thus it has to be copied at the very beginning of entity copy creation.
     *
     * @param id
     */
    void setGeneratedId(Object id);

    @Nullable
    <T> T getAttributeValue(String name);

    default void setAttributeValue(String name, @Nullable Object value) {
        setAttributeValue(name, value, true);
    }

    void setAttributeValue(String name, @Nullable Object value, boolean checkEquals);

    default boolean isEmbeddable() {
        return false;
    }

    boolean isNew();

    boolean isManaged();

    boolean isDetached();

    boolean isRemoved();

    void setNew(boolean _new);

    void setManaged(boolean managed);

    void setDetached(boolean detached);

    void setRemoved(boolean removed);

    SecurityState getSecurityState();

    void setSecurityState(SecurityState securityState);

    /**
     * Add listener to track attributes changes.
     *
     * @param listener listener
     */
    void addPropertyChangeListener(EntityPropertyChangeListener listener);

    /**
     * Remove listener.
     *
     * @param listener listener to remove
     */
    void removePropertyChangeListener(EntityPropertyChangeListener listener);

    /**
     * Remove all {@link EntityPropertyChangeListener}s.
     */
    void removeAllListeners();

    /**
     * Copies the state of entity entry.
     */
    void copy(@Nullable EntityEntry entry);

    void addExtraState(EntityEntryExtraState extraState);

    @Nullable
    EntityEntryExtraState getExtraState(Class<?> extraStateType);

    Collection<EntityEntryExtraState> getAllExtraState();
}

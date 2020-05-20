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

import io.jmix.core.entity.EntityPropertyChangeListener;
import io.jmix.core.entity.SecurityState;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;

public interface EntityEntry extends Serializable {

    Entity getSource();

    @Nullable
    Object getEntityId();

    void setEntityId(@Nullable Object id);

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

    @Nullable
    SecurityState getSecurityState();

    void setSecurityState(@Nullable SecurityState securityState);

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
     * Copies the state.
     */
    void copy(@Nullable EntityEntry entry);

    void addExtraState(EntityEntryExtraState extraState);

    @Nullable
    EntityEntryExtraState getExtraState(Class<?> extraStateType);

    Collection<EntityEntryExtraState> getAllExtraState();
}

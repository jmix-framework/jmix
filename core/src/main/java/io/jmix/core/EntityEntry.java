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

public interface EntityEntry<K> extends Serializable {

    Entity<K> getSource();

    K getEntityId();

    void setEntityId(K id);

    <T> T getAttributeValue(String name);

    default void setAttributeValue(String name, Object value) {
        setAttributeValue(name, value, true);
    }

    void setAttributeValue(String name, Object value, boolean checkEquals);

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
     * Copies the state.
     */
    void copy(EntityEntry<?> entry);

    void addExtraState(EntityEntryExtraState extraState);

    @Nullable
    <T extends EntityEntryExtraState> T getExtraState(Class<T> extraStateType);

    @Nullable
    Collection<EntityEntryExtraState> getAllExtraState();
}

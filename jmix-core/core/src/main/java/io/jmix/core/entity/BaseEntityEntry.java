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
import io.jmix.core.EntityEntryExtraState;
import io.jmix.core.EntityValuesProvider;
import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.entity.annotation.JmixId;
import io.jmix.core.metamodel.model.utils.MethodsCache;
import io.jmix.core.metamodel.model.utils.RelatedPropertiesCache;
import org.springframework.lang.NonNull;

import org.springframework.lang.Nullable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Id;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Used by enhancing process. Direct subclass of {@link BaseEntityEntry} will be created for entity, that
 * has primary key (an attribute annotated with {@link Id}, {@link EmbeddedId} or {@link JmixId}) and this primary key
 * annotated with {@link JmixGeneratedValue}
 */
@Internal
public abstract class BaseEntityEntry implements EntityEntry, Cloneable {
    protected byte state = NEW;
    protected SecurityState securityState = new SecurityState();
    protected transient Collection<WeakReference<EntityPropertyChangeListener>> propertyChangeListeners;
    protected Entity source;
    protected Map<Class<?>, EntityEntryExtraState> extraStateMap;
    protected Map<Class<?>, EntityValuesProvider> entityValuesProviders;

    public static final int NEW = 1;
    public static final int DETACHED = 2;
    public static final int MANAGED = 4;
    public static final int REMOVED = 8;

    protected static final int PROPERTY_CHANGE_LISTENERS_INITIAL_CAPACITY = 4;

    public BaseEntityEntry(Entity source) {
        this.source = source;
    }

    @Override
    @NonNull
    public Entity getSource() {
        return source;
    }

    @Override
    public int hashCode() {
        return getGeneratedId().hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttributeValue(@NonNull String name) {
        if (entityValuesProviders != null) {
            for (EntityValuesProvider valuesProvider : entityValuesProviders.values()) {
                if (valuesProvider.supportAttribute(name)) {
                    return valuesProvider.getAttributeValue(name);
                }
            }
        }
        return (T) MethodsCache.getOrCreate(getSource().getClass()).getGetter(name).apply(getSource());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setAttributeValue(@NonNull String name, Object value, boolean checkEquals) {
        EntityValuesProvider valuesProvider = null;
        if (entityValuesProviders != null) {
            valuesProvider = entityValuesProviders.values().stream()
                    .filter(provider -> provider.supportAttribute(name))
                    .findFirst()
                    .orElse(null);
        }
        if (valuesProvider != null) {
            valuesProvider.setAttributeValue(name, value, checkEquals);
        } else {
            Object oldValue = getAttributeValue(name);
            if (!checkEquals || !EntityValues.propertyValueEquals(oldValue, value)) {
                BiConsumer setter = MethodsCache.getOrCreate(getSource().getClass()).getSetter(name);
                setter.accept(getSource(), value);
            }
        }
    }

    @Override
    public boolean isNew() {
        return (state & NEW) == NEW;
    }

    @Override
    public boolean isManaged() {
        return (state & MANAGED) == MANAGED;
    }

    @Override
    public boolean isDetached() {
        return (state & DETACHED) == DETACHED;
    }

    @Override
    public boolean isRemoved() {
        return (state & REMOVED) == REMOVED;
    }

    @Override
    public void setNew(boolean _new) {
        state = (byte) (_new ? state | NEW : state & ~NEW);
    }

    @Override
    public void setManaged(boolean managed) {
        state = (byte) (managed ? state | MANAGED : state & ~MANAGED);
    }

    @Override
    public void setDetached(boolean detached) {
        state = (byte) (detached ? state | DETACHED : state & ~DETACHED);
    }

    @Override
    public void setRemoved(boolean removed) {
        state = (byte) (removed ? state | REMOVED : state & ~REMOVED);
    }

    @NonNull
    @Override
    public SecurityState getSecurityState() {
        return securityState;
    }


    @Override
    public void setSecurityState(@NonNull SecurityState securityState) {
        this.securityState = securityState;
    }

    @Override
    public void addPropertyChangeListener(@NonNull EntityPropertyChangeListener listener) {
        if (propertyChangeListeners == null) {
            propertyChangeListeners = new ArrayList<>(PROPERTY_CHANGE_LISTENERS_INITIAL_CAPACITY);
        }
        propertyChangeListeners.add(new WeakReference<>(listener));
    }

    @Override
    public void removePropertyChangeListener(@NonNull EntityPropertyChangeListener listener) {
        if (propertyChangeListeners != null) {
            for (Iterator<WeakReference<EntityPropertyChangeListener>> it = propertyChangeListeners.iterator(); it.hasNext(); ) {
                EntityPropertyChangeListener iteratorListener = it.next().get();
                if (iteratorListener == null || iteratorListener.equals(listener)) {
                    it.remove();
                }
            }
        }
    }

    public void removeAllListeners() {
        if (propertyChangeListeners != null) {
            propertyChangeListeners.clear();
        }
    }

    public void firePropertyChanged(String propertyName, Object prev, Object curr) {
        if (propertyChangeListeners != null) {

            for (Object referenceObject : propertyChangeListeners.toArray()) {
                @SuppressWarnings("unchecked")
                WeakReference<EntityPropertyChangeListener> reference = (WeakReference<EntityPropertyChangeListener>) referenceObject;

                EntityPropertyChangeListener listener = reference.get();
                if (listener == null) {
                    propertyChangeListeners.remove(reference);
                } else {
                    listener.propertyChanged(new EntityPropertyChangeEvent(getSource(), propertyName, prev, curr));

                    Collection<String> related = RelatedPropertiesCache.getOrCreate(getSource().getClass())
                            .getRelatedReadOnlyProperties(propertyName);
                    if (related != null) {
                        for (String property : related) {
                            listener.propertyChanged(
                                    new EntityPropertyChangeEvent(getSource(), property, null, getAttributeValue(property)));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void copy(@Nullable EntityEntry entry) {
        if (entry != null) {
            setNew(entry.isNew());
            setDetached(entry.isDetached());
            setManaged(entry.isManaged());
            setRemoved(entry.isRemoved());

            setSecurityState(entry.getSecurityState());

            for (EntityEntryExtraState extraState : entry.getAllExtraState()) {
                try {
                    EntityEntryExtraState newExtraState = ReflectionHelper.newInstance(extraState.getClass(), this);
                    newExtraState.copy(extraState);
                    addExtraState(newExtraState);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException(String.format("Error while create extra state of type: %s", extraState.getClass().getSimpleName()), e);
                }
            }

        }
    }

    @Override
    public void addExtraState(@NonNull EntityEntryExtraState extraState) {
        if (extraStateMap == null) {
            extraStateMap = new HashMap<>();
        }
        extraStateMap.put(extraState.getClass(), extraState);
        if (extraState instanceof EntityValuesProvider) {
            if (entityValuesProviders == null) {
                entityValuesProviders = new HashMap<>();
            }
            entityValuesProviders.put(extraState.getClass(), (EntityValuesProvider) extraState);
        }
    }

    @Override
    public EntityEntryExtraState getExtraState(@NonNull Class<?> extraStateType) {
        return extraStateMap == null ? null : extraStateMap.get(extraStateType);
    }

    @NonNull
    @Override
    public Collection<EntityEntryExtraState> getAllExtraState() {
        return extraStateMap == null ? Collections.emptyList() : Collections.unmodifiableCollection(extraStateMap.values());
    }

    private void writeObject(java.io.ObjectOutputStream oos) throws IOException {
        if (isManaged()) {
            setManaged(false);
            setDetached(true);
        }
        oos.defaultWriteObject();
    }
}

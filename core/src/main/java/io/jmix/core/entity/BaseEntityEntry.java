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
import io.jmix.core.commons.util.ReflectionHelper;
import io.jmix.core.metamodel.model.utils.MethodsCache;
import io.jmix.core.metamodel.model.utils.RelatedPropertiesCache;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class BaseEntityEntry<K> implements EntityEntry<K>, Cloneable {
    protected byte state = NEW;
    protected SecurityState securityState = new SecurityState();
    protected transient Collection<WeakReference<EntityPropertyChangeListener>> propertyChangeListeners;
    protected Entity<K> source;
    protected Map<Class, EntityEntryExtraState> extraStateMap;
    protected List<EntityValuesProvider> entityValuesProviders;

    public static final int NEW = 1;
    public static final int DETACHED = 2;
    public static final int MANAGED = 4;
    public static final int REMOVED = 8;

    protected static final int PROPERTY_CHANGE_LISTENERS_INITIAL_CAPACITY = 4;

    public BaseEntityEntry(Entity<K> source) {
        this.source = source;
    }

    @Override
    public Entity<K> getSource() {
        return source;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAttributeValue(String name) {
        if (entityValuesProviders != null) {
            for (EntityValuesProvider valuesProvider : entityValuesProviders) {
                if (valuesProvider.supportAttribute(name)) {
                    return valuesProvider.getAttributeValue(name);
                }
            }
        }
        return (T) MethodsCache.getOrCreate(getSource().getClass()).getGetterNN(name).apply(getSource());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setAttributeValue(String name, Object value, boolean checkEquals) {
        EntityValuesProvider valuesProvider = null;
        if (entityValuesProviders != null) {
            valuesProvider = entityValuesProviders.stream()
                    .filter(provider -> provider.supportAttribute(name))
                    .findFirst()
                    .orElse(null);
        }
        if (valuesProvider != null) {
            valuesProvider.setAttributeValue(name, value, checkEquals);
        } else {
            Object oldValue = getAttributeValue(name);
            if (!checkEquals || !EntityValues.propertyValueEquals(oldValue, value)) {
                BiConsumer setter = MethodsCache.getOrCreate(getSource().getClass()).getSetterNN(name);
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

    @Override
    public SecurityState getSecurityState() {
        return securityState;
    }

    @Override
    public void setSecurityState(SecurityState securityState) {
        this.securityState = securityState;
    }

    @Override
    public void addPropertyChangeListener(EntityPropertyChangeListener listener) {
        if (propertyChangeListeners == null) {
            propertyChangeListeners = new ArrayList<>(PROPERTY_CHANGE_LISTENERS_INITIAL_CAPACITY);
        }
        propertyChangeListeners.add(new WeakReference<>(listener));
    }

    @Override
    public void removePropertyChangeListener(EntityPropertyChangeListener listener) {
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
    public void copy(EntityEntry<?> entry) {
        if (entry != null) {
            setNew(entry.isNew());
            setDetached(entry.isDetached());
            setManaged(entry.isManaged());
            setRemoved(entry.isRemoved());

            setSecurityState(entry.getSecurityState());

            if (entry.getExtraState() != null) {
                for (EntityEntryExtraState extraState : entry.getExtraState()) {
                    try {
                        EntityEntryExtraState newExtraState = ReflectionHelper.newInstance(extraState.getClass());
                        newExtraState.copy(extraState);
                        addExtraState(newExtraState);
                    } catch (NoSuchMethodException e) {
                        throw new IllegalStateException(String.format("Error while create extra state of type: %s", extraState.getClass().getSimpleName()), e);
                    }
                }
            }
        }
    }

    @Override
    public void addExtraState(EntityEntryExtraState extraState) {
        if (extraStateMap == null) {
            extraStateMap = new HashMap<>();
        }
        extraStateMap.put(extraState.getClass(), extraState);
        if (extraState instanceof EntityValuesProvider) {
            if (entityValuesProviders == null) {
                entityValuesProviders = new ArrayList<>();
            }
            entityValuesProviders.add((EntityValuesProvider) extraState);
        }
    }

    @Override
    public <T extends EntityEntryExtraState> T getExtraState(Class<T> extraStateType) {
        //noinspection unchecked
        return extraStateMap == null ? null : (T) extraStateMap.get(extraStateType);
    }

    @Nullable
    @Override
    public Collection<EntityEntryExtraState> getExtraState() {
        return extraStateMap == null ? null : Collections.unmodifiableCollection(extraStateMap.values());
    }
}

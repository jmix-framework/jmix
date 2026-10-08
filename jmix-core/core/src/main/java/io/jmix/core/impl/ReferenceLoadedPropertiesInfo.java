/*
 * Copyright 2026 Haulmont.
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

package io.jmix.core.impl;

import io.jmix.core.EntityStates.PropertyLoadedState;
import io.jmix.core.PersistentAttributesLoadChecker;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.EntityPropertyChangeEvent;
import io.jmix.core.entity.EntityPropertyChangeListener;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.LoadedPropertiesInfo;
import org.jspecify.annotations.NullMarked;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link LoadedPropertiesInfo} of an instance returned by
 * {@link io.jmix.core.UnconstrainedDataManager#getReference(Class, Object)}. Reports as loaded only the primary key
 * and the properties set later (see {@link MarkingLoadedOnSetListener}). Never calls getters.
 */
@Internal
@NullMarked
public class ReferenceLoadedPropertiesInfo implements LoadedPropertiesInfo {

    private final Set<String> loadedProperties = new HashSet<>();

    @Override
    public boolean isLoaded(Object entity, String property, PersistentAttributesLoadChecker checker) {
        return loadedProperties.contains(property);
    }

    @Override
    public PropertyLoadedState isLoadedSafe(Object entity, String property, PersistentAttributesLoadChecker checker) {
        return PropertyLoadedState.fromBoolean(loadedProperties.contains(property));
    }

    @Override
    public void registerProperty(String name, boolean loaded) {
        if (loaded) {
            loadedProperties.add(name);
        } else {
            loadedProperties.remove(name);
        }
    }

    @Override
    public LoadedPropertiesInfo copy() {
        ReferenceLoadedPropertiesInfo dstInfo = new ReferenceLoadedPropertiesInfo();
        dstInfo.loadedProperties.addAll(loadedProperties);
        return dstInfo;
    }

    /**
     * @return names of the loaded properties
     */
    public Set<String> getLoadedProperties() {
        return Collections.unmodifiableSet(loadedProperties);
    }

    /**
     * Marks a set property as loaded while the entity's info is a {@link ReferenceLoadedPropertiesInfo}.
     * Has no state: one shared {@link #INSTANCE} is used, also after Java serialization.
     */
    public static class MarkingLoadedOnSetListener implements EntityPropertyChangeListener, Serializable {

        public static final MarkingLoadedOnSetListener INSTANCE = new MarkingLoadedOnSetListener();

        @Serial
        private static final long serialVersionUID = 1L;

        private MarkingLoadedOnSetListener() {
        }

        @Serial
        private Object readResolve() {
            return INSTANCE;
        }

        @Override
        public void propertyChanged(EntityPropertyChangeEvent e) {
            LoadedPropertiesInfo info = EntitySystemAccess.getEntityEntry(e.getItem()).getLoadedPropertiesInfo();
            if (info instanceof ReferenceLoadedPropertiesInfo) {
                info.registerProperty(e.getProperty(), true);
            }
        }
    }
}

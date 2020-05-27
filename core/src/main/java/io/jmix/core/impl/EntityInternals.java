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

package io.jmix.core.impl;


import io.jmix.core.entity.BaseEntityEntry;
import io.jmix.core.Entity;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.EntityEntry;

import javax.annotation.Nullable;
import java.io.ObjectOutputStream;
import java.util.Objects;

/**
 * Used by enhancing process
 */
@SuppressWarnings("unsed")
public class EntityInternals {

    public static String toString(Entity entity) {
        EntityEntry entityEntry = entity.__getEntityEntry();

        String state = "";
        if (entityEntry.isNew())
            state += "new,";
        if (entityEntry.isManaged())
            state += "managed,";
        if (entityEntry.isDetached())
            state += "detached,";
        if (entityEntry.isRemoved())
            state += "removed,";

        if (state.length() > 0)
            state = state.substring(0, state.length() - 1);

        return entity.getClass().getName() + "-" + entityEntry.getEntityId() + " [" + state + "]";
    }

    public static boolean equals(Entity o1, @Nullable Object o2) {
        if (o1 == o2)
            return true;

        if (o2 == null || o1.getClass() != o2.getClass())
            return false;

        if (o1.__getEntityEntry().getEntityId() == null && ((Entity) o2).__getEntityEntry().getEntityId() == null)
            return false;

        return Objects.equals(o1.__getEntityEntry().getEntityId(), ((Entity) o2).__getEntityEntry().getEntityId());
    }

    @SuppressWarnings("unused")
    public static void fireListeners(Entity entity, String property, @Nullable Object prevValue, @Nullable Object newValue) {
        if (!EntityValues.propertyValueEquals(prevValue, newValue)) {
            ((BaseEntityEntry) entity.__getEntityEntry()).firePropertyChanged(property, prevValue, newValue);
        }
    }

    @SuppressWarnings("unused")
    public static void writeObject(Entity entity, ObjectOutputStream outputStream) {
        EntityEntry entityEntry = entity.__getEntityEntry();
        if (entityEntry.isManaged()) {
            entityEntry.setManaged(false);
            entityEntry.setDetached(true);
        }
    }
}





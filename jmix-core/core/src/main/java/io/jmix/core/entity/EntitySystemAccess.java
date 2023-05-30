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
import io.jmix.core.EntityEntry;
import io.jmix.core.EntityEntryExtraState;
import io.jmix.core.annotation.Internal;

import org.springframework.lang.Nullable;

import static io.jmix.core.entity.EntityValues.isAuditSupported;
import static io.jmix.core.entity.EntityValues.isSoftDeletionSupported;

@Internal
public class EntitySystemAccess {

    public static <T extends EntityEntry> T getEntityEntry(Object entity) {
        EntityPreconditions.checkEntityType(entity);
        //noinspection unchecked
        return (T) ((Entity) entity).__getEntityEntry();
    }

    public static <T extends EntityEntry> T getUncheckedEntityEntry(Object entity) {
        //noinspection unchecked
        return (T) ((Entity) entity).__getEntityEntry();
    }

    public static SecurityState getSecurityState(Object entity) {
        return getEntityEntry(entity).getSecurityState();
    }

    public static boolean isEmbeddable(Object entity) {
        return getEntityEntry(entity).isEmbeddable();
    }

    public static Class<?> getDeletedDateClass(Object entity) {
        if (isSoftDeletionSupported(entity)) {
            return ((EntityEntrySoftDelete) getUncheckedEntityEntry(entity)).getDeletedDateClass();
        }
        return null;
    }

    public static Class<?> getDeletedByClass(Object entity) {
        if (isSoftDeletionSupported(entity)) {
            return ((EntityEntrySoftDelete) getUncheckedEntityEntry(entity)).getDeletedByClass();
        }
        return null;
    }

    public static Class<?> getCreatedDateClass(Object entity) {
        if (isAuditSupported(entity)) {
            return ((EntityEntryAuditable) getUncheckedEntityEntry(entity)).getCreatedDateClass();
        }
        return null;
    }

    public static Class<?> getCreatedByClass(Object entity) {
        if (isAuditSupported(entity)) {
            return ((EntityEntryAuditable) getUncheckedEntityEntry(entity)).getCreatedByClass();
        }
        return null;
    }

    public static Class<?> getLastModifiedDateClass(Object entity) {
        if (isAuditSupported(entity)) {
            return ((EntityEntryAuditable) getUncheckedEntityEntry(entity)).getLastModifiedDateClass();
        }
        return null;
    }

    public static Class<?> getLastModifiedByClass(Object entity) {
        if (isAuditSupported(entity)) {
            return ((EntityEntryAuditable) getUncheckedEntityEntry(entity)).getLastModifiedByClass();
        }
        return null;
    }

    public static void addExtraState(Object entity, EntityEntryExtraState extraState) {
        getEntityEntry(entity).addExtraState(extraState);
    }

    @Nullable
    public static <T extends EntityEntryExtraState> T getExtraState(Object entity, Class<?> extraStateType) {
        //noinspection unchecked
        return (T) getEntityEntry(entity).getExtraState(extraStateType);
    }

    public static void addPropertyChangeListener(Object entity, EntityPropertyChangeListener listener) {
        getEntityEntry(entity).addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(Object entity, EntityPropertyChangeListener listener) {
        getEntityEntry(entity).removePropertyChangeListener(listener);
    }
}

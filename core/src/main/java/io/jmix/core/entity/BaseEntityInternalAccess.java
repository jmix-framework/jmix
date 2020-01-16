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

import com.google.common.collect.Multimap;
import com.google.common.collect.ObjectArrays;
import io.jmix.core.AppBeans;
import io.jmix.core.Metadata;
import io.jmix.core.commons.util.Preconditions;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * INTERNAL
 */
public final class BaseEntityInternalAccess {

    public static final int NEW = 1;
    public static final int DETACHED = 2;
    public static final int MANAGED = 4;
    public static final int REMOVED = 8;

    private BaseEntityInternalAccess() {
    }

    public static boolean isNew(BaseGenericIdEntity entity) {
        return (entity.__state & NEW) == NEW;
    }

    public static void setNew(BaseGenericIdEntity entity, boolean _new) {
        entity.__state = (byte) (_new ? entity.__state | NEW : entity.__state & ~NEW);
    }

    public static boolean isManaged(BaseGenericIdEntity entity) {
        return (entity.__state & MANAGED) == MANAGED;
    }

    public static void setManaged(BaseGenericIdEntity entity, boolean managed) {
        entity.__state = (byte) (managed ? entity.__state | MANAGED : entity.__state & ~MANAGED);
    }

    public static boolean isDetached(BaseGenericIdEntity entity) {
        return (entity.__state & DETACHED) == DETACHED;
    }

    public static void setDetached(BaseGenericIdEntity entity, boolean detached) {
        entity.__state = (byte) (detached ? entity.__state | DETACHED : entity.__state & ~DETACHED);
    }

    public static boolean isRemoved(BaseGenericIdEntity entity) {
        return (entity.__state & REMOVED) == REMOVED;
    }

    public static void setRemoved(BaseGenericIdEntity entity, boolean removed) {
        entity.__state = (byte) (removed ? entity.__state | REMOVED : entity.__state & ~REMOVED);
    }

    public static String[] getInaccessibleAttributes(Entity entity) {
        SecurityState state = getSecurityState(entity);
        return state != null ? getInaccessibleAttributes(state) : null;
    }

    public static String[] getInaccessibleAttributes(SecurityState state) {
        return state.inaccessibleAttributes;
    }

    public static void setInaccessibleAttributes(SecurityState state, String[] inaccessibleAttributes) {
        state.inaccessibleAttributes = inaccessibleAttributes;
    }

    public static Multimap<String, Object> getFilteredData(Entity entity) {
        SecurityState state = getSecurityState(entity);
        return state != null ? getFilteredData(state) : null;
    }

    public static Multimap<String, Object> getFilteredData(SecurityState state) {
        return state.filteredData;
    }

    public static void setFilteredData(SecurityState state, Multimap<String, Object> filteredData) {
        state.filteredData = filteredData;
    }

    public static byte[] getSecurityToken(Entity entity) {
        SecurityState state = getSecurityState(entity);
        return state != null ? getSecurityToken(state) : null;
    }

    public static byte[] getSecurityToken(SecurityState state) {
        return state.securityToken;
    }

    public static void setSecurityToken(SecurityState state, byte[] securityToken) {
        state.securityToken = securityToken;
    }

    public static String[] getFilteredAttributes(BaseGenericIdEntity entity) {
        SecurityState state = getSecurityState(entity);
        return state != null ? getFilteredAttributes(state) : null;
    }

    public static String[] getFilteredAttributes(SecurityState state) {
        return state.filteredAttributes;
    }

    public static void setFilteredAttributes(SecurityState state, String[] filteredAttributes) {
        state.filteredAttributes = filteredAttributes;
    }

    public static boolean supportsSecurityState(Entity entity) {
        return entity instanceof BaseGenericIdEntity || entity instanceof EmbeddableEntity;
    }

    public static SecurityState getSecurityState(Entity entity) {
        Preconditions.checkNotNullArgument(entity, "Entity is null");
        SecurityState securityState;
        if (entity instanceof BaseGenericIdEntity) {
            BaseGenericIdEntity baseGenericIdEntity = (BaseGenericIdEntity) entity;
            securityState = baseGenericIdEntity.__securityState;
        } else if (entity instanceof EmbeddableEntity) {
            EmbeddableEntity embeddableEntity = (EmbeddableEntity) entity;
            securityState = embeddableEntity.__securityState;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Entity with type [%s] does not support security state", AppBeans.get(Metadata.class).getClass(entity).getName()));
        }
        return securityState;
    }

    public static void setSecurityState(Entity entity, SecurityState securityState) {
        Preconditions.checkNotNullArgument(entity, "Entity is null");
        if (entity instanceof BaseGenericIdEntity) {
            BaseGenericIdEntity baseGenericIdEntity = (BaseGenericIdEntity) entity;
            baseGenericIdEntity.__securityState = securityState;
        } else if (entity instanceof EmbeddableEntity) {
            EmbeddableEntity embeddableEntity = (EmbeddableEntity) entity;
            embeddableEntity.__securityState = securityState;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Entity with type [%s] does not support security state", AppBeans.get(Metadata.class).getClass(entity).getName()));
        }
    }

    public static SecurityState getOrCreateSecurityState(Entity entity) {
        Preconditions.checkNotNullArgument(entity, "Entity is null");
        SecurityState securityState;
        if (entity instanceof BaseGenericIdEntity) {
            BaseGenericIdEntity baseGenericIdEntity = (BaseGenericIdEntity) entity;
            if (baseGenericIdEntity.__securityState == null) {
                baseGenericIdEntity.__securityState = new SecurityState();
            }
            securityState = baseGenericIdEntity.__securityState;
        } else if (entity instanceof EmbeddableEntity) {
            EmbeddableEntity embeddableEntity = (EmbeddableEntity) entity;
            if (embeddableEntity.__securityState == null) {
                embeddableEntity.__securityState = new SecurityState();
            }
            securityState = embeddableEntity.__securityState;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Entity with type [%s] does not support security state", AppBeans.get(Metadata.class).getClass(entity).getName()));
        }
        return securityState;
    }

    public static void setValue(Entity entity, String attribute, @Nullable Object value) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        Field field = FieldUtils.getField(entity.getClass(), attribute, true);
        if (field == null)
            throw new RuntimeException(String.format("Cannot find field '%s' in class %s", attribute, entity.getClass().getName()));
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unable to set value to %s.%s", entity.getClass().getSimpleName(), attribute), e);
        }
    }

    public static void setValueForHolder(Entity entity, String attribute, @Nullable Object value) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        Field field = FieldUtils.getField(entity.getClass(), String.format("_persistence_%s_vh",attribute), true);
        if (field == null)
            return;
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unable to set value to %s.%s", entity.getClass().getSimpleName(), attribute), e);
        }
    }

    public static Object getValue(Entity entity, String attribute) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        Field field = FieldUtils.getField(entity.getClass(), attribute, true);
        if (field == null)
            throw new RuntimeException(String.format("Cannot find field '%s' in class %s", attribute, entity.getClass().getName()));
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unable to set value to %s.%s", entity.getClass().getSimpleName(), attribute), e);
        }
    }

    public static void copySystemState(BaseGenericIdEntity src, BaseGenericIdEntity dst) {
        dst.copySystemState(src);
    }
}
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

package io.jmix.security.user;

import io.jmix.core.Metadata;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Helper for working with the user entity field marked with the {@link PasswordChangeRequired} annotation.
 * <p>
 * If the user entity does not have such a field, all methods of this class behave as no-ops:
 * {@link #isPasswordChangeRequired(Object)} returns {@code false} and {@link #setPasswordChangeRequired(Object, boolean)}
 * does nothing. This guarantees that the feature is fully optional and does not affect projects whose
 * user entity is not annotated.
 */
@Component("sec_PasswordChangeRequiredSupport")
public class PasswordChangeRequiredSupport {

    @Autowired
    protected Metadata metadata;

    protected final ConcurrentHashMap<Class<?>, Optional<MetaProperty>> propertyCache = new ConcurrentHashMap<>();

    /**
     * @return the {@link MetaProperty} corresponding to the field marked with {@link PasswordChangeRequired}
     * in the given user class, or {@code null} if the class does not have such a field, or the field is not
     * registered in the metamodel
     */
    @Nullable
    public MetaProperty findFlagProperty(Class<?> userClass) {
        return propertyCache.computeIfAbsent(userClass, this::lookupFlagProperty)
                .orElse(null);
    }

    /**
     * @return {@code true} if the given user has the field marked with {@link PasswordChangeRequired} and its
     * value is {@code true}
     */
    public boolean isPasswordChangeRequired(@Nullable Object user) {
        if (!EntityValues.isEntity(user)) {
            return false;
        }

        MetaProperty property = findFlagProperty(user.getClass());
        if (property == null) {
            return false;
        }

        Boolean value = EntityValues.getValue(user, property.getName());
        return Boolean.TRUE.equals(value);
    }

    /**
     * Sets the value of the field marked with {@link PasswordChangeRequired} on the given user.
     * Does nothing if the user is not a Jmix entity or does not have such a field.
     */
    public void setPasswordChangeRequired(Object user, boolean value) {
        if (!EntityValues.isEntity(user)) {
            return;
        }

        MetaProperty property = findFlagProperty(user.getClass());
        if (property == null) {
            return;
        }

        EntityValues.setValue(user, property.getName(), value);
    }

    protected Optional<MetaProperty> lookupFlagProperty(Class<?> userClass) {
        MetaClass metaClass = metadata.findClass(userClass);
        if (metaClass == null) {
            return Optional.empty();
        }

        Field field = Arrays.stream(FieldUtils.getAllFields(userClass))
                .filter(f -> f.isAnnotationPresent(PasswordChangeRequired.class))
                .findFirst()
                .orElse(null);
        if (field == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(metaClass.findProperty(field.getName()));
    }
}

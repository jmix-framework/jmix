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

package io.jmix.eclipselink.impl.lazyloading;

import io.jmix.core.entity.NoValueCollection;
import jakarta.annotation.Nullable;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.indirection.IndirectCollection;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.expressions.ExpressionIterator;
import org.eclipse.persistence.internal.expressions.ParameterExpression;
import org.eclipse.persistence.internal.indirection.QueryBasedValueHolder;
import org.eclipse.persistence.internal.indirection.UnitOfWorkQueryValueHolder;
import io.jmix.core.common.util.ReflectionHelper;
import org.eclipse.persistence.internal.indirection.WrappingValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

public class ValueHoldersSupport {

    private static final Logger log = LoggerFactory.getLogger(ValueHoldersSupport.class);

    /**
     * Returns a value holder of the given single-value reference attribute, woven into the entity class
     * by the enhancing process.
     * <p>
     * Returns {@code null} if no value holder field is woven for the attribute, which means the attribute
     * was not lazy at enhancing time (e.g. a {@code @OneToOne(mappedBy = ...)} attribute declared without
     * {@code fetch = FetchType.LAZY}). Such attributes are always loaded eagerly by the ORM and do not
     * participate in lazy loading.
     */
    @Nullable
    public static Object getSingleValueHolder(Object entity, String propertyName) {
        Object valueHolder;
        try {
            Field valueHolderField = ReflectionHelper.findField(entity.getClass(), String.format("_persistence_%s_vh", propertyName));
            if (valueHolderField == null) {
                log.trace("No value holder field is woven for property '{}' of entity {} - the attribute is not lazy",
                        propertyName, entity.getClass().getName());
                return null;
            }

            ReflectionUtils.makeAccessible(valueHolderField);
            valueHolder = valueHolderField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unable to access value holder for property: %s on entity %s",
                    propertyName, entity.getClass().getName()), e);
        }

        return valueHolder;
    }

    public static void setSingleValueHolder(Object entity, String propertyName, Object valueHolder) {
        try {
            Field valueHolderField = ReflectionHelper.findField(entity.getClass(), String.format("_persistence_%s_vh", propertyName));
            if (valueHolderField == null) {
                throw new RuntimeException(String.format("Unable to access value holder for property: %s on entity %s",
                        propertyName, entity.getClass().getName()));
            }
            ReflectionUtils.makeAccessible(valueHolderField);
            valueHolderField.set(entity, valueHolder);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unable to access value holder for property: %s on entity %s",
                    propertyName, entity.getClass().getName()), e);
        }
    }

        public static void setSingleValue(Object entity, String propertyName, Object valueHolder) {
        try {
            Field field = ReflectionHelper.findField(entity.getClass(), propertyName);
            if (field == null) {
                throw new RuntimeException(String.format("Unable to access property: %s on entity %s",
                        propertyName, entity.getClass().getName()));
            }
            ReflectionUtils.makeAccessible(field);
            field.set(entity, valueHolder);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unable to access property: %s on entity %s",
                    propertyName, entity.getClass().getName()), e);
        }
    }

    public static Object getCollectionProperty(Object entity, String propertyName) {
        Object value;
        try {
            Field valueField = ReflectionHelper.findField(entity.getClass(), propertyName);
            if (valueField == null) {
                throw new RuntimeException(String.format("Unable to access value for property: %s on entity %s",
                        propertyName, entity.getClass().getName()));
            }
            ReflectionUtils.makeAccessible(valueField);
            value = valueField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unable to access value for property: %s on entity %s",
                    propertyName, entity.getClass().getName()), e);
        }

        return value;
    }

    public static void setCollectionProperty(Object entity, String propertyName, Object value) {
        try {
            Field valueField = ReflectionHelper.findField(entity.getClass(), propertyName);
            if (valueField == null) {
                throw new RuntimeException(String.format("Unable to access value for property: %s on entity %s",
                        propertyName, entity.getClass().getName()));
            }
            ReflectionUtils.makeAccessible(valueField);
            valueField.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("Unable to access value for property: %s on entity %s",
                    propertyName, entity.getClass().getName()), e);
        }
    }

    public static Object getCollectionValueHolder(Object entity, String propertyName) {
        Object value = getCollectionProperty(entity, propertyName);
        if (value instanceof IndirectCollection) {
            return ((IndirectCollection) value).getValueHolder();
        }
        return null;
    }

    public static void setCollectionValueHolder(Object entity, String propertyName, Object valueHolder) {
        Object value = getCollectionProperty(entity, propertyName);
        if (value instanceof IndirectCollection) {
            ((IndirectCollection) value).setValueHolder((ValueHolderInterface) valueHolder);
        } else {
            throw new RuntimeException(String.format("Unable to access value holder for property: %s on entity %s",
                    propertyName, entity.getClass().getName()));
        }
    }

    public static QueryBasedValueHolder unwrapToQueryBasedValueHolder(Object valueHolder) {
        if (valueHolder instanceof UnitOfWorkQueryValueHolder) {
            UnitOfWorkQueryValueHolder unitOfWorkQueryValueHolder = (UnitOfWorkQueryValueHolder) valueHolder;
            if (unitOfWorkQueryValueHolder.getWrappedValueHolder() instanceof QueryBasedValueHolder) {
                QueryBasedValueHolder queryBasedValueHolder = (QueryBasedValueHolder) unitOfWorkQueryValueHolder.getWrappedValueHolder();
                return queryBasedValueHolder.isInstantiated() ? null : queryBasedValueHolder;
            }
        }
        return null;
    }

    @Nullable
    public static Object unwrapToValueIfInstantiated(Object valueHolder) {
        if (valueHolder instanceof WrappingValueHolder<?> wrappingValueHolder && wrappingValueHolder.getWrappedValueHolder() != null) {
            ValueHolderInterface<?> wrappedHolder = wrappingValueHolder.getWrappedValueHolder();
            if (wrappedHolder.isInstantiated() && !(wrappedHolder instanceof NoValueCollection)) {
                return wrappingValueHolder.getWrappedValueHolder().getValue();
            }
        }
        return null;
    }

    public static Object getEntityIdFromValueHolder(QueryBasedValueHolder queryBasedValueHolder) {
        AtomicReference<String> fieldName = new AtomicReference<>();
        ExpressionIterator iterator = new ExpressionIterator() {
            @Override
            public void iterate(Expression each) {
                if (each instanceof ParameterExpression) {
                    fieldName.set(((ParameterExpression) each).getField().getQualifiedName());
                }
            }
        };
        iterator.iterateOn(queryBasedValueHolder.getQuery().getSelectionCriteria());
        return queryBasedValueHolder.getRow().get(fieldName.get());
    }
}

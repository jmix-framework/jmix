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

import io.jmix.core.JmixEntity;
import io.jmix.core.metamodel.model.PropertyPath;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Collection;

import static io.jmix.core.metamodel.model.utils.ObjectPathUtils.formatValuePath;
import static io.jmix.core.metamodel.model.utils.ObjectPathUtils.parseValuePath;

public class EntityValues {

    @Nullable
    public static Object getId(JmixEntity entity) {
        return entity.__getEntityEntry().getEntityId();
    }

    public static void setId(JmixEntity entity, Object key) {
        entity.__getEntityEntry().setEntityId(key);
    }

    public static Object getIdOrEntity(JmixEntity entity) {
        Object id = entity.__getEntityEntry().getEntityId();
        return id != null ? id : entity;
    }

    public static Object getGeneratedId(JmixEntity entity) {
        return entity.__getEntityEntry().getGeneratedId();
    }

    /**
     * Set an attribute value.
     * <br>
     * An implementor should first read a current value of the attribute, and then call an appropriate setter
     * method only if the new value differs. This ensures triggering of {@link EntityPropertyChangeListener}s only if the attribute
     * was actually changed.
     *
     * @param name  attribute name according to JavaBeans notation
     * @param value attribute value
     */
    public static void setValue(JmixEntity entity, String name, Object value) {
        entity.__getEntityEntry().setAttributeValue(name, value, true);
    }

    /**
     * Set an attribute value.
     * <br>
     * An implementor should first read a current value of the attribute, and then call an appropriate setter
     * method only if the new value differs. This ensures triggering of {@link EntityPropertyChangeListener}s only if the attribute
     * was actually changed.
     *
     * @param name        attribute name according to JavaBeans notation
     * @param value       attribute value
     * @param checkEquals check equals for previous and new value.
     *                    If flag is true and objects equals, then setter will not be invoked
     */
    public static void setValue(JmixEntity entity, String name, Object value, boolean checkEquals) {
        entity.__getEntityEntry().setAttributeValue(name, value, checkEquals);
    }

    /**
     * Get an attribute value.
     *
     * @param name attribute name according to JavaBeans notation
     * @return attribute value
     */
    @Nullable
    public static <T> T getValue(JmixEntity entity, String name) {
        return entity.__getEntityEntry().getAttributeValue(name);
    }

    /**
     * Get an attribute value. Locates the attribute by the given path in object graph starting from this instance.
     * <br>
     * The path must consist of attribute names according to JavaBeans notation, separated by dots, e.g.
     * {@code car.driver.name}.
     *
     * @param propertyPath path to an attribute
     * @return attribute value. If any traversing attribute value is null or is not an {@link JmixEntity}, this method
     * stops here and returns this value.
     */
    @Nullable
    public static <T> T getValueEx(JmixEntity entity, String propertyPath) {
        return getValueEx(entity, parseValuePath(propertyPath));
    }

    /**
     * Get an attribute value. Locates the attribute by the given path in object graph starting from this instance.
     * <br>
     * The path must consist of attribute names according to JavaBeans notation, separated by dots, e.g.
     * {@code car.driver.name}.
     *
     * @param propertyPath path to an attribute
     * @return attribute value. If any traversing attribute value is null or is not an {@link JmixEntity}, this method
     * stops here and returns this value.
     */
    @Nullable
    public static <T> T getValueEx(JmixEntity entity, PropertyPath propertyPath) {
        if (propertyPath.isDirectProperty()) {
            return getValue(entity, propertyPath.getFirstPropertyName());
        } else {
            return getValueEx(entity, propertyPath.getPropertyNames());
        }
    }

    /**
     * Set an attribute value. Locates the attribute by the given path in object graph starting from this instance.
     * <br> The path must consist of attribute names according to JavaBeans notation, separated by dots, e.g.
     * {@code car.driver.name}.
     * <br> In the example above this method first gets value of {@code car.driver} attribute, and if it is not
     * null and is an {@link JmixEntity}, sets value of {@code name} attribute in it.
     * <br> An implementor should first read a current value of the attribute, and then call an appropriate setter
     * method only if the new value differs. This ensures triggering of {@link EntityPropertyChangeListener}s only if the attribute
     * was actually changed.
     *
     * @param propertyPath path to an attribute
     * @param value        attribute value
     */
    public static void setValueEx(JmixEntity entity, String propertyPath, Object value) {
        setValueEx(entity, parseValuePath(propertyPath), value);
    }

    /**
     * Set an attribute value. Locates the attribute by the given path in object graph starting from this instance.
     * <br> The path must consist of attribute names according to JavaBeans notation, separated by dots, e.g.
     * {@code car.driver.name}.
     * <br> In the example above this method first gets value of {@code car.driver} attribute, and if it is not
     * null and is an {@link JmixEntity}, sets value of {@code name} attribute in it.
     * <br> An implementor should first read a current value of the attribute, and then call an appropriate setter
     * method only if the new value differs. This ensures triggering of {@link EntityPropertyChangeListener}s only if the attribute
     * was actually changed.
     *
     * @param propertyPath path to an attribute
     * @param value        attribute value
     */
    public static void setValueEx(JmixEntity entity, PropertyPath propertyPath, Object value) {
        if (propertyPath.isDirectProperty()) {
            setValue(entity, propertyPath.getFirstPropertyName(), value);
        } else {
            String[] properties = propertyPath.getPropertyNames();
            setValueEx(entity, properties, value);
        }
    }

    /**
     * Set value of an attribute according to the rules described in {@link EntityValues#setValueEx(JmixEntity, String, Object)}.
     *
     * @param entity     instance
     * @param properties path to the attribute
     * @param value      attribute value
     */
    public static void setValueEx(JmixEntity entity, String[] properties, Object value) {
        if (properties.length > 1) {

            if (properties.length == 2) {
                entity = getValue(entity, properties[0]);
            } else {
                String[] subarray = ArrayUtils.subarray(properties, 0, properties.length - 1);
                String path = formatValuePath(subarray);
                entity = getValueEx(entity, path);
            }

            if (entity != null) {
                setValue(entity, properties[properties.length - 1], value);
            }
        } else {
            setValue(entity, properties[0], value);
        }
    }

    /**
     * Get value of an attribute according to the rules described in {@link EntityValues#getValueEx(JmixEntity, String)}.
     *
     * @param entity     entity
     * @param properties path to the attribute
     * @return attribute value
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValueEx(JmixEntity entity, String[] properties) {
        if (properties == null) {
            return null;
        }

        Object currentValue = null;
        JmixEntity currentEntity = entity;
        for (String property : properties) {
            if (currentEntity == null) {
                break;
            }

            currentValue = getValue(currentEntity, property);

            if (currentValue == null) {
                break;
            }


            currentEntity = currentValue instanceof JmixEntity ? (JmixEntity) currentValue : null;
        }

        return (T) currentValue;
    }

    /**
     * Used by {@link } to check whether a property value has been changed.
     *
     * @param a an object
     * @param b an object
     * @return true if {@code a} equals to {@code b}, but in case of {@code a} is {@link } or {@code Collection} returns
     * true only if {@code a} is the same instance as {@code b}
     */
    public static boolean propertyValueEquals(@Nullable Object a, @Nullable Object b) {
        if (a == b) {
            return true;
        }
        if (a instanceof JmixEntity || a instanceof Collection) {
            return false;
        }
        return a != null && a.equals(b);
    }
}

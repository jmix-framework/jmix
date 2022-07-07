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
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.PropertyPath;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.UUID;

import static io.jmix.core.entity.EntitySystemAccess.getEntityEntry;
import static io.jmix.core.entity.EntitySystemAccess.getUncheckedEntityEntry;
import static io.jmix.core.metamodel.model.utils.ObjectPathUtils.formatValuePath;
import static io.jmix.core.metamodel.model.utils.ObjectPathUtils.parseValuePath;

@Internal
public class EntityValues {

    public static boolean isEntity(Object entity) {
        return entity instanceof Entity;
    }

    @Nullable
    public static Object getId(Object entity) {
        return getEntityEntry(entity).getEntityId();
    }

    public static void setId(Object entity, Object key) {
        getEntityEntry(entity).setEntityId(key);
    }

    public static Object getIdOrEntity(Object entity) {
        Object id = getEntityEntry(entity).getEntityId();
        return id != null ? id : entity;
    }

    public static Object getGeneratedId(Object entity) {
        return getEntityEntry(entity).getGeneratedId();
    }

    public static void setGeneratedId(Object entity, Object id) {
        getEntityEntry(entity).setGeneratedId(id);
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
    public static void setValue(Object entity, String name, Object value) {
        getEntityEntry(entity).setAttributeValue(name, value, true);
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
    public static void setValue(Object entity, String name, Object value, boolean checkEquals) {
        getEntityEntry(entity).setAttributeValue(name, value, checkEquals);
    }

    /**
     * Get an attribute value.
     *
     * @param name attribute name according to JavaBeans notation
     * @return attribute value
     */
    @Nullable
    public static <T> T getValue(Object entity, String name) {
        return getEntityEntry(entity).getAttributeValue(name);
    }

    /**
     * Get an attribute value. Locates the attribute by the given path in object graph starting from this instance.
     * <br>
     * The path must consist of attribute names according to JavaBeans notation, separated by dots, e.g.
     * {@code car.driver.name}.
     *
     * @param propertyPath path to an attribute
     * @return attribute value. If any traversing attribute value is null or is not an {@link Entity}, this method
     * stops here and returns this value.
     */
    @Nullable
    public static <T> T getValueEx(Object entity, String propertyPath) {
        return getValueEx(entity, parseValuePath(propertyPath));
    }

    /**
     * Get an attribute value. Locates the attribute by the given path in object graph starting from this instance.
     * <br>
     * The path must consist of attribute names according to JavaBeans notation, separated by dots, e.g.
     * {@code car.driver.name}.
     *
     * @param propertyPath path to an attribute
     * @return attribute value. If any traversing attribute value is null or is not an {@link Entity}, this method
     * stops here and returns this value.
     */
    @Nullable
    public static <T> T getValueEx(Object entity, PropertyPath propertyPath) {
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
     * null and is an {@link Entity}, sets value of {@code name} attribute in it.
     * <br> An implementor should first read a current value of the attribute, and then call an appropriate setter
     * method only if the new value differs. This ensures triggering of {@link EntityPropertyChangeListener}s only if the attribute
     * was actually changed.
     *
     * @param propertyPath path to an attribute
     * @param value        attribute value
     */
    public static void setValueEx(Object entity, String propertyPath, Object value) {
        setValueEx(entity, parseValuePath(propertyPath), value);
    }

    /**
     * Set an attribute value. Locates the attribute by the given path in object graph starting from this instance.
     * <br> The path must consist of attribute names according to JavaBeans notation, separated by dots, e.g.
     * {@code car.driver.name}.
     * <br> In the example above this method first gets value of {@code car.driver} attribute, and if it is not
     * null and is an {@link Entity}, sets value of {@code name} attribute in it.
     * <br> An implementor should first read a current value of the attribute, and then call an appropriate setter
     * method only if the new value differs. This ensures triggering of {@link EntityPropertyChangeListener}s only if the attribute
     * was actually changed.
     *
     * @param propertyPath path to an attribute
     * @param value        attribute value
     */
    public static void setValueEx(Object entity, PropertyPath propertyPath, Object value) {
        if (propertyPath.isDirectProperty()) {
            setValue(entity, propertyPath.getFirstPropertyName(), value);
        } else {
            String[] properties = propertyPath.getPropertyNames();
            setValueEx(entity, properties, value);
        }
    }

    /**
     * Set value of an attribute according to the rules described in {@link EntityValues#setValueEx(Object, String, Object)}.
     *
     * @param entity     instance
     * @param properties path to the attribute
     * @param value      attribute value
     */
    public static void setValueEx(Object entity, String[] properties, Object value) {
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
     * Get value of an attribute according to the rules described in {@link EntityValues#getValueEx(Object, String)}.
     *
     * @param entity     entity
     * @param properties path to the attribute
     * @return attribute value
     */
    @SuppressWarnings("unchecked")
    public static <T> T getValueEx(Object entity, String[] properties) {
        if (properties == null) {
            return null;
        }

        Object currentValue = null;
        Object currentEntity = entity;
        for (String property : properties) {
            if (currentEntity == null) {
                break;
            }

            currentValue = getValue(currentEntity, property);

            if (currentValue == null) {
                break;
            }

            currentEntity = currentValue instanceof Entity ? (Entity) currentValue : null;
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
        if (a instanceof Entity || a instanceof Collection) {
            return false;
        }
        return a != null && a.equals(b);
    }

    public static boolean isUuidSupported(Object entity) {
        return getEntityEntry(entity) instanceof EntityEntryHasUuid;
    }

    @Nullable
    public static UUID getUuid(Object entity) {
        if (isUuidSupported(entity)) {
            return ((EntityEntryHasUuid) getUncheckedEntityEntry(entity)).getUuid();
        }
        return null;
    }

    public static void setUuid(Object entity, UUID uuid) {
        if (isUuidSupported(entity)) {
            ((EntityEntryHasUuid) getUncheckedEntityEntry(entity)).setUuid(uuid);
        }
    }

    public static boolean isVersionSupported(Object entity) {
        return getUncheckedEntityEntry(entity) instanceof EntityEntryVersioned;
    }

    @Nullable
    public static Object getVersion(Object entity) {
        if (isVersionSupported(entity)) {
            return ((EntityEntryVersioned) getUncheckedEntityEntry(entity)).getVersion();
        }
        return null;
    }

    public static void setVersion(Object entity, Object version) {
        if (isVersionSupported(entity)) {
            ((EntityEntryVersioned) getUncheckedEntityEntry(entity)).setVersion(version);
        }
    }

    public static boolean isSoftDeletionSupported(Object entity) {
        return getEntityEntry(entity) instanceof EntityEntrySoftDelete;
    }

    public static boolean isSoftDeleted(Object entity) {
        return isSoftDeletionSupported(entity)
                && ((EntityEntrySoftDelete) getUncheckedEntityEntry(entity)).isDeleted();
    }

    public static Object getDeletedDate(Object entity) {
        if (isSoftDeletionSupported(entity)) {
            return ((EntityEntrySoftDelete) getUncheckedEntityEntry(entity)).getDeletedDate();
        }
        return null;
    }

    public static void setDeletedDate(Object entity, Object value) {
        if (isSoftDeletionSupported(entity)) {
            ((EntityEntrySoftDelete) getUncheckedEntityEntry(entity)).setDeletedDate(value);
        }
    }

    public static Object getDeletedBy(Object entity) {
        if (isSoftDeletionSupported(entity)) {
            return ((EntityEntrySoftDelete) getUncheckedEntityEntry(entity)).getDeletedBy();
        }
        return null;
    }

    public static void setDeletedBy(Object entity, Object value) {
        if (isSoftDeletionSupported(entity)) {
            ((EntityEntrySoftDelete) getUncheckedEntityEntry(entity)).setDeletedBy(value);
        }
    }

    public static boolean isAuditSupported(Object entity) {
        return getEntityEntry(entity) instanceof EntityEntryAuditable;
    }

    public static Object getCreatedDate(Object entity) {
        if (isAuditSupported(entity)) {
            EntityEntryAuditable auditable = getUncheckedEntityEntry(entity);
            return auditable.getCreatedDateClass() == null ? null : auditable.getCreatedDate();
        }
        return null;
    }

    public static void setCreatedDate(Object entity, Object value) {
        if (isAuditSupported(entity)) {
            ((EntityEntryAuditable) getUncheckedEntityEntry(entity)).setCreatedDate(value);
        }
    }

    public static Object getCreatedBy(Object entity) {
        if (isAuditSupported(entity)) {
            EntityEntryAuditable auditable = getUncheckedEntityEntry(entity);
            return auditable.getCreatedByClass() == null ? null : auditable.getCreatedBy();
        }
        return null;
    }

    public static void setCreatedBy(Object entity, Object value) {
        if (isAuditSupported(entity)) {
            ((EntityEntryAuditable) getUncheckedEntityEntry(entity)).setCreatedBy(value);
        }
    }

    public static Object getLastModifiedDate(Object entity) {
        if (isAuditSupported(entity)) {
            EntityEntryAuditable auditable = getUncheckedEntityEntry(entity);
            return auditable.getLastModifiedDateClass() == null ? null : auditable.getLastModifiedDate();
        }
        return null;
    }

    public static void setLastModifiedDate(Object entity, Object value) {
        if (isAuditSupported(entity)) {
            ((EntityEntryAuditable) getUncheckedEntityEntry(entity)).setLastModifiedDate(value);
        }
    }

    public static Object getLastModifiedBy(Object entity) {
        if (isAuditSupported(entity)) {
            EntityEntryAuditable auditable = getUncheckedEntityEntry(entity);
            return auditable.getLastModifiedByClass() == null ? null : auditable.getLastModifiedBy();
        }
        return null;
    }

    public static void setLastModifiedBy(Object entity, Object value) {
        if (isAuditSupported(entity)) {
            ((EntityEntryAuditable) getUncheckedEntityEntry(entity)).setLastModifiedBy(value);
        }
    }
}

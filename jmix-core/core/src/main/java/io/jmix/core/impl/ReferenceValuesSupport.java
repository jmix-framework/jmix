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

import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.annotation.JmixGeneratedValue;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Removes initial values from an instance returned by
 * {@link io.jmix.core.UnconstrainedDataManager#getReference(Class, Object)} and from its copies.
 *
 * @see ReferenceLoadedPropertiesInfo
 */
@Internal
@NullMarked
@Component("core_ReferenceValuesSupport")
public class ReferenceValuesSupport {

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Sets to null, directly in the fields, the values assigned when the instance was created, so that a later set of
     * any value fires a change event and marks the attribute loaded.
     * <p>
     * Keeps the primary key, {@link JmixGeneratedValue} values ({@code equals()} and {@code hashCode()} can depend on
     * them), embedded instances and element collections (entity code can expect them to be not null).
     */
    public void clearInitialValues(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        String primaryKeyName = metadataTools.getPrimaryKeyName(metaClass);
        for (MetaProperty property : metaClass.getProperties()) {
            if (property.getName().equals(primaryKeyName)
                    || property.getAnnotations().get(JmixGeneratedValue.class.getName()) != null
                    || property.getType() == MetaProperty.Type.EMBEDDED
                    || metadataTools.isElementCollection(property)) {
                continue;
            }
            Field field = FieldUtils.getField(entity.getClass(), property.getName(), true);
            if (field == null
                    || field.getType().isPrimitive()
                    || Modifier.isStatic(field.getModifiers())
                    || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            try {
                if (FieldUtils.readField(field, entity) != null) {
                    FieldUtils.writeField(field, entity, null);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error clearing attribute value of a reference", e);
            }
        }
    }
}

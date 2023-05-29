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

package com.haulmont.cuba.gui.data.impl;

import com.haulmont.cuba.core.global.AppBeans;
import io.jmix.core.Entity;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.model.MetaProperty;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class EntityCopyUtils {

    public static Entity copyCompositions(Entity src) {
        Preconditions.checkNotNullArgument(src, "source is null");

        Entity dest;
        try {
            dest = src.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        copyCompositions(src, dest);

        dest.__getEntityEntry().setSecurityState(src.__getEntityEntry().getSecurityState());

        return dest;
    }

    public static void copyCompositions(Entity source, Entity dest) {
        Preconditions.checkNotNullArgument(source, "source is null");
        Preconditions.checkNotNullArgument(dest, "dest is null");

        EntityValues.setId(dest, EntityValues.getId(source));

        Metadata metadata = AppBeans.get(Metadata.class);

        for (MetaProperty srcProperty : metadata.getClass(source).getProperties()) {
            String name = srcProperty.getName();
            MetaProperty dstProperty = metadata.getClass(dest).findProperty(name);
            if (dstProperty != null && !dstProperty.isReadOnly()) {
                try {
                    Object value = EntityValues.getValue(source, name);

                    if (value != null && srcProperty.getRange().getCardinality().isMany()
                            && srcProperty.getType() == MetaProperty.Type.COMPOSITION) {
                        //noinspection unchecked
                        Collection<Entity> srcCollection = (Collection) value;

                        // Copy first to a Set to remove duplicates that could be created on repeated editing newly
                        // added items
                        Collection<Entity> tmpCollection = new LinkedHashSet<>();
                        for (Entity item : srcCollection) {
                            Entity copy = copyCompositions(item);
                            tmpCollection.add(copy);
                        }

                        Collection<Entity> dstCollection;
                        if (!(value instanceof Set))
                            dstCollection = new ArrayList<>(tmpCollection);
                        else
                            dstCollection = tmpCollection;
                        EntityValues.setValue(dest, name, dstCollection);

                    } else {
                        EntityValues.setValue(dest, name, EntityValues.getValue(source, name));
                    }
                } catch (RuntimeException e) {
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    if (cause == null)
                        cause = e;
                    // ignore exception on copy for not loaded fields
                    if (!isNotLoadedAttributeException(cause))
                        throw e;
                }
            }
        }

        dest.__getEntityEntry().setDetached(source.__getEntityEntry().isDetached());
        dest.__getEntityEntry().setNew(source.__getEntityEntry().isNew());
        // todo dynamic attributes
//            destGenericEntity.setDynamicAttributes(sourceGenericEntity.getDynamicAttributes());
    }

    public static void copyCompositionsBack(Entity source, Entity dest) {
        Preconditions.checkNotNullArgument(source, "source is null");
        Preconditions.checkNotNullArgument(dest, "dest is null");

        Metadata metadata = AppBeans.get(Metadata.class);
        for (MetaProperty srcProperty : metadata.getClass(source).getProperties()) {
            String name = srcProperty.getName();
            MetaProperty dstProperty = metadata.getClass(dest).findProperty(name);
            if (dstProperty != null && !dstProperty.isReadOnly()) {
                try {
                    Object value = EntityValues.getValue(source, name);

                    if (value != null && srcProperty.getRange().getCardinality().isMany()
                            && srcProperty.getType() == MetaProperty.Type.COMPOSITION) {
                        EntityValues.setValue(dest, name, EntityValues.getValue(source, name), false);
                    } else {
                        EntityValues.setValue(dest, name, EntityValues.getValue(source, name));
                    }
                } catch (RuntimeException e) {
                    Throwable cause = ExceptionUtils.getRootCause(e);
                    if (cause == null)
                        cause = e;
                    // ignore exception on copy for not loaded fields
                    if (!isNotLoadedAttributeException(cause))
                        throw e;
                }
            }
        }
    }

    private static boolean isNotLoadedAttributeException(Throwable e) {
        return e instanceof IllegalStateException
                || e instanceof org.eclipse.persistence.exceptions.ValidationException && e.getMessage() != null
                && e.getMessage().contains("An attempt was made to traverse a relationship using indirection that had a null Session");
    }
}

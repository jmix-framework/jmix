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

package io.jmix.eclipselink.impl;

import io.jmix.core.EntityStates;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.ReferenceIdProvider;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.indirection.DatabaseValueHolder;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.mappings.foundation.AbstractColumnMapping;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.Method;
import java.util.Vector;

import static io.jmix.core.entity.EntityValues.getId;
import static io.jmix.core.entity.EntityValues.getValue;

/**
 * Utility class to provide common functionality related to persistence.
 * <br> Implemented as Spring bean to allow extension in application projects.
 */
@Component("eclipselink_EclipselinkReferenceIdProvider")
public class EclipselinkReferenceIdProvider implements ReferenceIdProvider {
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected MetadataTools metadataTools;
    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    public RefId getReferenceId(Object entity, String property) {
        MetaClass metaClass = metadata.getClass(entity);
        MetaProperty metaProperty = metaClass.getProperty(property);

        if (!metaProperty.getRange().isClass() || metaProperty.getRange().getCardinality().isMany())
            throw new IllegalArgumentException("Property is not a reference");

        if (!entityStates.isManaged(entity))
            throw new IllegalStateException("Entity must be in managed state");

        if (entity instanceof FetchGroupTracker) {
            FetchGroup fetchGroup = ((FetchGroupTracker) entity)._persistence_getFetchGroup();
            if (fetchGroup != null) {
                if (!fetchGroup.containsAttributeInternal(property))
                    return RefId.createNotLoaded(property);
                else {
                    if (entityStates.isLoaded(entity, property)) {
                        Object refEntity = getValue(entity, property);
                        return RefId.create(property, refEntity == null ? null : getId(refEntity));
                    } else {
                        return RefId.createNotLoaded(property);
                    }
                }
            }
        }

        try {
            Class<?> declaringClass = metaProperty.getDeclaringClass();
            if (declaringClass == null) {
                throw new RuntimeException("Property does not belong to persistent class");
            }

            Method vhMethod = declaringClass.getDeclaredMethod(String.format("_persistence_get_%s_vh", property));
            vhMethod.setAccessible(true);

            ValueHolderInterface vh = (ValueHolderInterface) vhMethod.invoke(entity);
            if (vh instanceof DatabaseValueHolder) {
                AbstractRecord row = ((DatabaseValueHolder) vh).getRow();
                if (row != null) {
                    Session session = entityManager.unwrap(Session.class);
                    ClassDescriptor descriptor = session.getDescriptor(entity);
                    DatabaseMapping mapping = descriptor.getMappingForAttributeName(property);
                    Vector<DatabaseField> fields = mapping.getFields();
                    if (fields.size() != 1) {
                        throw new IllegalStateException("Invalid number of columns in mapping: " + fields);
                    }
                    Object value = row.get(fields.get(0));
                    if (value != null) {
                        ClassDescriptor refDescriptor = mapping.getReferenceDescriptor();
                        DatabaseMapping refMapping = refDescriptor.getMappingForAttributeName(metadataTools.getPrimaryKeyName(metaClass));
                        if (refMapping instanceof AbstractColumnMapping) {
                            Converter converter = ((AbstractColumnMapping) refMapping).getConverter();
                            if (converter != null) {
                                return RefId.create(property, converter.convertDataValueToObjectValue(value, session));
                            }
                        }
                    }
                    return RefId.create(property, value);
                } else {
                    return RefId.create(property, null);
                }
            }
            return RefId.createNotLoaded(property);
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Error retrieving reference ID from %s.%s", entity.getClass().getSimpleName(), property),
                    e);
        }
    }
}


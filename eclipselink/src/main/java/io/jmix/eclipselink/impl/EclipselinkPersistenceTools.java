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
import io.jmix.core.FetchPlan;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.entity.EntityPreconditions;
import io.jmix.core.entity.EntityValues;
import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import io.jmix.data.PersistenceTools;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.descriptors.changetracking.ChangeTracker;
import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.eclipse.persistence.internal.descriptors.changetracking.AttributeChangeListener;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.internal.indirection.DatabaseValueHolder;
import org.eclipse.persistence.internal.sessions.AbstractRecord;
import org.eclipse.persistence.internal.sessions.ObjectChangeSet;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.mappings.foundation.AbstractColumnMapping;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.changesets.ChangeRecord;
import org.eclipse.persistence.sessions.changesets.CollectionChangeRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.*;

import static io.jmix.core.entity.EntityValues.getId;
import static io.jmix.core.entity.EntityValues.getValue;

/**
 * Utility class to provide common functionality related to persistence.
 * <br> Implemented as Spring bean to allow extension in application projects.
 */
@Component("eclipse_persistenceTools")
public class EclipselinkPersistenceTools implements PersistenceTools {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected EntityStates entityStates;

    @Autowired
    protected MetadataTools metadataTools;

    /**
     * Returns the set of dirty attributes (changed since the last load from the database).
     * <p> If the entity is new, returns all its attributes.
     * <p> If the entity is not persistent or not in the Managed state, returns empty set.
     *
     * @param entity entity instance
     * @return dirty attribute names
     * @see #isDirty(Object, String...)
     */
    public Set<String> getDirtyFields(Object entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        EntityPreconditions.checkEntityType(entity);

        if (!(entity instanceof ChangeTracker) || !entityStates.isManaged(entity))
            return Collections.emptySet();

        HashSet<String> result = new HashSet<>();
        if (entityStates.isNew(entity)) {
            for (MetaProperty property : metadata.getClass(entity.getClass()).getProperties()) {
                if (metadataTools.isPersistent(property))
                    result.add(property.getName());
            }
        } else {
            PropertyChangeListener propertyChangeListener = ((ChangeTracker) entity)._persistence_getPropertyChangeListener();
            if (propertyChangeListener == null)
                throw new IllegalStateException("Entity '" + entity + "' is a ChangeTracker but has no PropertyChangeListener");
            ObjectChangeSet objectChanges = ((AttributeChangeListener) propertyChangeListener).getObjectChangeSet();
            if (objectChanges != null) // can be null for example in AFTER_DELETE entity listener
                result.addAll(objectChanges.getChangedAttributeNames());
        }
        return result;
    }

    /**
     * Returns true if the given entity has dirty attributes (changed since the last load from the database).
     * <br> If the entity is new, returns true.
     * <br> If the entity is not persistent or not in the Managed state, returns false.
     *
     * @param entity entity instance
     * @see #getDirtyFields(Object)
     * @see #isDirty(Object, String...)
     */
    public boolean isDirty(Object entity) {
        Preconditions.checkNotNullArgument(entity, "entity is null");
        EntityPreconditions.checkEntityType(entity);

        if (!(entity instanceof ChangeTracker) || !entityStates.isManaged(entity))
            return false;

        if (entityStates.isNew(entity))
            return true;

        AttributeChangeListener attributeChangeListener = (AttributeChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener();
        return attributeChangeListener != null && attributeChangeListener.hasChanges();
    }

    /**
     * Returns true if at least one of the given attributes is dirty (i.e. changed since the last load from the database).
     * <p> If the entity is new, always returns true.
     * <p> If the entity is not persistent or not in the Managed state, always returns false.
     *
     * @param entity     entity instance
     * @param attributes attributes to check
     * @see #getDirtyFields(Object)
     */
    public boolean isDirty(Object entity, String... attributes) {
        EntityPreconditions.checkEntityType(entity);

        Set<String> dirtyFields = getDirtyFields(entity);
        for (String attribute : attributes) {
            if (dirtyFields.contains(attribute))
                return true;
        }
        return false;
    }

    /**
     * Returns an old value of an attribute changed in the current transaction. The entity must be in the Managed state.
     * For enum attributes returns enum id. <br>
     * You can check if the value has been changed using {@link #isDirty(Object, String...)} method.
     *
     * @param entity    entity instance
     * @param attribute attribute name
     * @return an old value stored in the database. For a new entity returns null.
     * @throws IllegalArgumentException if the entity is not persistent or not in the Managed state
     * @see #getOldEnumValue(Object, String)
     * @see #isDirty(Object, String...)
     * @see #getDirtyFields(Object)
     */
    @Nullable
    public Object getOldValue(Object entity, String attribute) {
        Preconditions.checkNotNullArgument(entity, "entity is null");

        if (!(entity instanceof ChangeTracker))
            throw new IllegalArgumentException("The entity " + entity + " is not a ChangeTracker");

        if (!entityStates.isManaged(entity))
            throw new IllegalArgumentException("The entity " + entity + " is not in the Managed state");

        if (entityStates.isNew(entity)) {
            return null;

        } else if (!isDirty(entity, attribute)) {
            return getValue(entity, attribute);

        } else {
            ObjectChangeSet objectChanges =
                    ((AttributeChangeListener) ((ChangeTracker) entity)._persistence_getPropertyChangeListener()).getObjectChangeSet();
            if (objectChanges != null) { // can be null for example in AFTER_DELETE entity listener
                ChangeRecord changeRecord = objectChanges.getChangesForAttributeNamed(attribute);
                if (changeRecord instanceof CollectionChangeRecord) {
                    if (PersistenceHints.isSoftDeletion(entityManager) && changeRecord.getOldValue() != null) {
                        MetaProperty metaProperty = metadata.getClass(entity).getProperty(attribute);
                        if (metadataTools.isSoftDeletable(metaProperty.getRange().asClass().getJavaClass())) {
                            Collection oldValue = (Collection) changeRecord.getOldValue();
                            Collection<Object> filteredValue;
                            Class<?> propertyType = metaProperty.getJavaType();
                            if (List.class.isAssignableFrom(propertyType)) {
                                filteredValue = new ArrayList<>();
                            } else if (Set.class.isAssignableFrom(propertyType)) {
                                filteredValue = new LinkedHashSet<>();
                            } else {
                                throw new RuntimeException(String.format("Could not instantiate collection with class [%s].", propertyType));
                            }
                            for (Object item : oldValue) {
                                boolean isDeleted = EntityValues.isSoftDeleted(item);
                                if (!isDeleted || isDirty(item, metadataTools.getDeletedDateProperty(item))) {
                                    filteredValue.add(item);
                                }
                            }
                            return filteredValue;
                        }
                    }
                    return changeRecord.getOldValue();
                } else if (changeRecord != null) {
                    return changeRecord.getOldValue();
                }
            }
        }
        return null;
    }

    /**
     * Returns an old value of an enum attribute changed in the current transaction. The entity must be in the Managed state.
     * <p>
     * Unlike {@link #getOldValue(Object, String)}, returns enum value and not its id.
     *
     * @param entity    entity instance
     * @param attribute attribute name
     * @return an old value stored in the database. For a new entity returns null.
     * @throws IllegalArgumentException if the entity is not persistent or not in the Managed state
     */
    @Nullable
    public EnumClass getOldEnumValue(Object entity, String attribute) {
        EntityPreconditions.checkEntityType(entity);

        Object value = getOldValue(entity, attribute);
        if (value == null)
            return null;

        MetaClass metaClass = metadata.getClass(entity.getClass());
        MetaProperty metaProperty = metaClass.getProperty(attribute);
        if (metaProperty.getRange().isEnum()) {
            for (Object o : metaProperty.getRange().asEnumeration().getValues()) {
                EnumClass enumValue = (EnumClass) o;
                if (value.equals(enumValue.getId()))
                    return enumValue;
            }
        }
        return null;
    }


    /**
     * Checks if the property is loaded from DB.
     *
     * @param entity   entity
     * @param property name of the property
     * @return true if loaded
     */
    public boolean isLoaded(Object entity, String property) {
        return entityStates.isLoaded(entity, property);
    }

    /**
     * Returns an ID of directly referenced entity without loading it from DB.
     * <p>
     * If the fetchPlan does not contain the reference and {@link FetchPlan#loadPartialEntities()} is true,
     * the returned {@link RefId} will have {@link RefId#isLoaded()} = false.
     *
     * <p>Usage example:
     * <pre>
     *   PersistenceTools.RefId refId = persistenceTools.getReferenceId(doc, "currency");
     *   if (refId.isLoaded()) {
     *       String currencyCode = (String) refId.getValue();
     *   }
     * </pre>
     *
     * @param entity   entity instance in managed state
     * @param property name of reference property
     * @return {@link RefId} instance which contains the referenced entity ID
     * @throws IllegalArgumentException if the specified property is not a reference
     * @throws IllegalStateException    if the entity is not in Managed state
     * @throws RuntimeException         if anything goes wrong when retrieving the ID
     */
    public RefId getReferenceId(Object entity, String property) {
        MetaClass metaClass = metadata.getClass(entity.getClass());
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

    /**
     * Deletes records corresponding to the given entity instances from the database. Soft deletion is not considered.
     * Only primary table is affected in case of inheritance.
     * <p>
     * Should be used only in tests or in other non-standard situations.
     *
     * @param entities instances to remove from the database.
     */
    public void deleteRecord(Object... entities) {
        if (entities == null)
            return;
        for (Object entity : entities) {
            if (entity == null)
                continue;

            MetaClass metaClass = metadata.getClass(entity.getClass());

            String table = metadataTools.getDatabaseTable(metaClass);
            String primaryKey = metadataTools.getPrimaryKeyName(metaClass);
            if (table == null || primaryKey == null)
                throw new RuntimeException("Unable to determine table or primary key name for " + entity);

            deleteRecord(table, primaryKey, EntityValues.<Object>getId(entity));
        }
    }

    /**
     * Deletes records from the database.
     * <p>
     * Should be used only in tests or in other non-standard situations.
     *
     * @param table         table name
     * @param primaryKeyCol PK column name
     * @param ids           PK values of the records to delete
     */
    public void deleteRecord(String table, String primaryKeyCol, Object... ids) {
        for (Object id : ids) {
            String sql = "delete from " + table + " where " + primaryKeyCol + " = '" + id.toString() + "'";
            new JdbcTemplate(dataSource).update(sql);
        }
    }
}

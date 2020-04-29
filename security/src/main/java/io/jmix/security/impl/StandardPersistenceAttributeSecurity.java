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

package io.jmix.security.impl;

import io.jmix.core.*;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.*;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.Security;
import io.jmix.data.PersistenceAttributeSecurity;
import io.jmix.data.impl.JmixEntityFetchGroup;
import io.jmix.security.SecurityProperties;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;

@Component(PersistenceAttributeSecurity.NAME)
public class StandardPersistenceAttributeSecurity implements PersistenceAttributeSecurity {

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected Security security;

    @Inject
    protected SecurityProperties properties;

    @Inject
    protected EntityStates entityStates;

    /**
     * Removes restricted attributes from a fetch plan.
     *
     * @param fetchPlan source fetch plan
     * @return restricted fetch plan
     */
    @Override
    public FetchPlan createRestrictedFetchPlan(FetchPlan fetchPlan) {
        if (!properties.isEntityAttributePermissionChecking()) {
            return fetchPlan;
        }
        Preconditions.checkNotNullArgument(fetchPlan, "fetch plan is null");

        FetchPlan restrictedView = new FetchPlan(fetchPlan.getEntityClass(),
                StringUtils.isEmpty(fetchPlan.getName()) ? "" : fetchPlan.getName() + "_restricted",
                false); // do not include system properties in constructor because they will be copied later if exist
        copyViewConsideringPermissions(fetchPlan, restrictedView);
        return restrictedView;
    }

    private void copyViewConsideringPermissions(FetchPlan srcPlan, FetchPlan dstPlan) {
        MetaClass metaClass = metadata.getClass(srcPlan.getEntityClass());
        for (FetchPlanProperty property : srcPlan.getProperties()) {
            if (security.isEntityAttrReadPermitted(metaClass, property.getName())) {
                FetchPlan viewCopy = null;
                if (property.getFetchPlan() != null) {
                    viewCopy = new FetchPlan(property.getFetchPlan().getEntityClass(),
                            property.getFetchPlan().getName() + "(restricted)", false);
                    copyViewConsideringPermissions(property.getFetchPlan(), viewCopy);
                }
                dstPlan.addProperty(property.getName(), viewCopy, property.getFetchMode());
            }
        }
    }

    /**
     * Should be called after loading an entity from the database.
     *
     * @param entity just loaded detached entity
     */
    @Override
    public void afterLoad(Entity entity) {
        if (!properties.isEntityAttributePermissionChecking()) {
            return;
        }
        if (entity != null) {
            metadataTools.traverseAttributes(entity, new FillingInaccessibleAttributesVisitor());
        }
    }

    /**
     * Should be called after loading a list of entities from the database.
     *
     * @param entities list of just loaded detached entities
     */
    @Override
    public void afterLoad(Collection<? extends Entity> entities) {
        Preconditions.checkNotNullArgument(entities, "entities list is null");

        for (Entity entity : entities) {
            afterLoad(entity);
        }
    }

    /**
     * Should be called before persisting a new entity.
     *
     * @param entity new entity
     */
    @Override
    public void beforePersist(Entity entity) {
        if (!properties.isEntityAttributePermissionChecking()) {
            return;
        }
        // check only immediate attributes, otherwise persisted entity can be unusable for calling code
        MetaClass metaClass = metadata.getClass(entity.getClass());
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (!metadataTools.isSystem(metaProperty)
                    && !metaProperty.isReadOnly()
                    && !security.isEntityAttrUpdatePermitted(metaClass, metaProperty.getName())) {
                EntityValues.setValue(entity, metaProperty.getName(), null);
            }
        }
    }

    /**
     * Should be called before merging an entity.
     *
     * @param entity detached entity
     */
    @Override
    public void beforeMerge(Entity entity) {
        if (!properties.isEntityAttributePermissionChecking()) {
            return;
        }
        applySecurityToFetchGroup(entity);
        //apply fetch group constraints to embedded
        for (MetaProperty metaProperty : metadata.getClass(entity).getProperties()) {
            String name = metaProperty.getName();
            if (metadataTools.isEmbedded(metaProperty) && entityStates.isLoaded(entity, name)) {
                Entity embedded = EntityValues.getValue(entity, name);
                applySecurityToFetchGroup(embedded);
            }
        }
    }

    /**
     * Should be called after merging an entity and transaction commit.
     *
     * @param entity detached entity
     */
    @Override
    public void afterCommit(Entity entity) {
        if (!properties.isEntityAttributePermissionChecking()) {
            return;
        }
        if (entity != null) {
            metadataTools.traverseAttributes(entity, new ClearInaccessibleAttributesVisitor());
        }
    }

    protected void applySecurityToFetchGroup(Entity entity) {
        if (entity == null) {
            return;
        }
        MetaClass metaClass = metadata.getClass(entity.getClass());
        FetchGroupTracker fetchGroupTracker = (FetchGroupTracker) entity;
        FetchGroup fetchGroup = fetchGroupTracker._persistence_getFetchGroup();
        if (fetchGroup != null) {
            List<String> attributesToRemove = new ArrayList<>();
            for (String attrName : fetchGroup.getAttributeNames()) {
                String[] parts = attrName.split("\\.");
                MetaClass currentMetaClass = metaClass;
                for (String part : parts) {
                    if (!security.isEntityAttrUpdatePermitted(currentMetaClass, part)) {
                        attributesToRemove.add(attrName);
                        break;
                    }
                    MetaProperty metaProperty = currentMetaClass.getProperty(part);
                    if (metaProperty.getRange().isClass()) {
                        currentMetaClass = metaProperty.getRange().asClass();
                    }
                }
            }
            if (!attributesToRemove.isEmpty()) {
                List<String> attributeNames = new ArrayList<>(fetchGroup.getAttributeNames());
                attributeNames.removeAll(attributesToRemove);
                fetchGroupTracker._persistence_setFetchGroup(new JmixEntityFetchGroup(attributeNames));
            }
        } else {
            List<String> attributeNames = new ArrayList<>();
            for (MetaProperty metaProperty : metaClass.getProperties()) {
                String propertyName = metaProperty.getName();
                if (metadataTools.isSystem(metaProperty)) {
                    attributeNames.add(propertyName);
                }
                if (security.isEntityAttrUpdatePermitted(metaClass, propertyName)) {
                    attributeNames.add(metaProperty.getName());
                }
            }
            fetchGroupTracker._persistence_setFetchGroup(new JmixEntityFetchGroup(attributeNames));
        }
    }

    private void addInaccessibleAttribute(Entity entity, String property) {
        EntityEntry entityEntry = entity.__getEntityEntry();

        String[] attributes = entityEntry.getSecurityState().getInaccessibleAttributes();

        attributes = attributes == null ? new String[1] : Arrays.copyOf(attributes, attributes.length + 1);
        attributes[attributes.length - 1] = property;

        entityEntry.getSecurityState().setInaccessibleAttributes(attributes);
    }

    protected void setNullPropertyValue(Entity entity, MetaProperty property) {
        // Using reflective access to field because the attribute can be unfetched if loading not partial entities,
        // which is the case when in-memory constraints exist
        Range range = property.getRange();
        if (range.isClass()) {
            Object nullValue = null;
            if (range.getCardinality().isMany()) {
                Class<?> propertyType = property.getJavaType();
                if (List.class.isAssignableFrom(propertyType)) {
                    nullValue = new ArrayList<>();
                } else if (Set.class.isAssignableFrom(propertyType)) {
                    nullValue = new LinkedHashSet<>();
                }
            }
            setValue(entity, property.getName(), nullValue);
            setValueForHolder(entity, property.getName(), nullValue);
        } else {
            setValue(entity, property.getName(), null);
        }
    }

    protected class FillingInaccessibleAttributesVisitor implements EntityAttributeVisitor {
        @Override
        public boolean skip(MetaProperty property) {
            return !metadataTools.isPersistent(property);
        }

        @Override
        public void visit(Entity entity, MetaProperty property) {
            MetaClass metaClass = metadata.getClass(entity.getClass());
            if (!security.isEntityAttrReadPermitted(metaClass, property.getName())) {
                addInaccessibleAttribute(entity, property.getName());
                if (!metadataTools.isSystem(property) && !property.isReadOnly()) {
                    setNullPropertyValue(entity, property);
                }
            }
        }
    }

    protected class ClearInaccessibleAttributesVisitor implements EntityAttributeVisitor {
        @Override
        public void visit(Entity entity, MetaProperty property) {
            MetaClass metaClass = metadata.getClass(entity.getClass());
            String propertyName = property.getName();
            if (!security.isEntityAttrReadPermitted(metaClass, propertyName)) {
                addInaccessibleAttribute(entity, propertyName);
                if (!metadataTools.isSystem(property) && !property.isReadOnly()) {
                    setNullPropertyValue(entity, property);
                }
            }
        }
    }

    private static void setValue(Entity entity, String attribute, @Nullable Object value) {
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

    private static void setValueForHolder(Entity entity, String attribute, @Nullable Object value) {
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
}

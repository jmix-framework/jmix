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

import com.google.common.collect.Sets;
import io.jmix.core.*;
import io.jmix.core.commons.util.Preconditions;
import io.jmix.core.entity.BaseEntityInternalAccess;
import io.jmix.core.entity.BaseGenericIdEntity;
import io.jmix.core.entity.Entity;
import io.jmix.core.entity.SecurityState;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.core.metamodel.model.Range;
import io.jmix.core.security.Security;
import io.jmix.data.PersistenceAttributeSecurity;
import io.jmix.data.impl.JmixEntityFetchGroup;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.inject.Inject;
import java.util.*;

import static io.jmix.core.entity.BaseEntityInternalAccess.*;

@Component(PersistenceAttributeSecurity.NAME)
public class StandardPersistenceAttributeSecurity implements PersistenceAttributeSecurity {

    @Inject
    protected Metadata metadata;

    @Inject
    protected MetadataTools metadataTools;

    @Inject
    protected Security security;

    @Inject
    protected ServerConfig config;

    @Inject
    protected EntityStates entityStates;

    /**
     * Removes restricted attributes from a view.
     *
     * @param view source view
     * @return restricted view
     */
    @Override
    public View createRestrictedView(View view) {
        if (!config.getEntityAttributePermissionChecking()) {
            return view;
        }
        Preconditions.checkNotNullArgument(view, "view is null");

        View restrictedView = new View(view.getEntityClass(),
                StringUtils.isEmpty(view.getName()) ? "" : view.getName() + "_restricted",
                false); // do not include system properties in constructor because they will be copied later if exist
        copyViewConsideringPermissions(view, restrictedView);
        return restrictedView;
    }

    private void copyViewConsideringPermissions(View srcView, View dstView) {
        MetaClass metaClass = metadata.getClassNN(srcView.getEntityClass());
        for (ViewProperty property : srcView.getProperties()) {
            if (security.isEntityAttrReadPermitted(metaClass, property.getName())) {
                View viewCopy = null;
                if (property.getView() != null) {
                    viewCopy = new View(property.getView().getEntityClass(), property.getView().getName() + "(restricted)", false);
                    copyViewConsideringPermissions(property.getView(), viewCopy);
                }
                dstView.addProperty(property.getName(), viewCopy, property.getFetchMode());
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
        if (!config.getEntityAttributePermissionChecking()) {
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
        if (!config.getEntityAttributePermissionChecking()) {
            return;
        }
        // check only immediate attributes, otherwise persisted entity can be unusable for calling code
        MetaClass metaClass = metadata.getClassNN(entity.getClass());
        for (MetaProperty metaProperty : metaClass.getProperties()) {
            if (!metadataTools.isSystem(metaProperty)
                    && !metaProperty.isReadOnly()
                    && !security.isEntityAttrUpdatePermitted(metaClass, metaProperty.getName())) {
                entity.setValue(metaProperty.getName(), null);
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
        if (!config.getEntityAttributePermissionChecking()) {
            return;
        }
        applySecurityToFetchGroup(entity);
        //apply fetch group constraints to embedded
        for (MetaProperty metaProperty : metadata.getClass(entity).getProperties()) {
            String name = metaProperty.getName();
            if (metadataTools.isEmbedded(metaProperty) && entityStates.isLoaded(entity, name)) {
                Entity embedded = entity.getValue(name);
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
        if (!config.getEntityAttributePermissionChecking()) {
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
        MetaClass metaClass = metadata.getClassNN(entity.getClass());
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
                    MetaProperty metaProperty = currentMetaClass.getPropertyNN(part);
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
        SecurityState securityState = getOrCreateSecurityState(entity);
        String[] attributes = getInaccessibleAttributes(securityState);
        attributes = attributes == null ? new String[1] : Arrays.copyOf(attributes, attributes.length + 1);
        attributes[attributes.length - 1] = property;
        setInaccessibleAttributes(securityState, attributes);
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
            BaseEntityInternalAccess.setValue(entity, property.getName(), nullValue);
            BaseEntityInternalAccess.setValueForHolder(entity, property.getName(), nullValue);
        } else {
            BaseEntityInternalAccess.setValue(entity, property.getName(), null);
        }
    }

    protected class FillingInaccessibleAttributesVisitor implements EntityAttributeVisitor {
        @Override
        public boolean skip(MetaProperty property) {
            return metadataTools.isNotPersistent(property);
        }

        @Override
        public void visit(Entity entity, MetaProperty property) {
            MetaClass metaClass = metadata.getClassNN(entity.getClass());
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
            MetaClass metaClass = metadata.getClassNN(entity.getClass());
            String propertyName = property.getName();
            if (!security.isEntityAttrReadPermitted(metaClass, propertyName)) {
                addInaccessibleAttribute(entity, propertyName);
                if (!metadataTools.isSystem(property) && !property.isReadOnly()) {
                    setNullPropertyValue(entity, property);
                }
            }
        }
    }
}

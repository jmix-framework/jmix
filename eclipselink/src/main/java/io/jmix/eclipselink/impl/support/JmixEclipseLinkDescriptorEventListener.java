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

package io.jmix.eclipselink.impl.support;

import io.jmix.core.*;
import io.jmix.core.entity.EntityValues;
import io.jmix.data.AttributeChangesProvider;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.impl.EntityAuditValues;
import io.jmix.data.impl.EntityListenerManager;
import io.jmix.data.impl.EntityListenerType;
import io.jmix.eclipselink.impl.EclipselinkPersistenceSupport;
import io.jmix.eclipselink.impl.JmixEntityFetchGroup;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.eclipse.persistence.descriptors.DescriptorEventManager;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static io.jmix.core.entity.EntitySystemAccess.getUncheckedEntityEntry;

@Component("eclipselink_EclipseLinkDescriptorEventListener")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixEclipseLinkDescriptorEventListener implements DescriptorEventListener {

    private final Logger logger = LoggerFactory.getLogger(JmixEclipseLinkDescriptorEventListener.class);

    @Autowired
    protected EntityListenerManager entityListenerManager;
    @Autowired
    protected AuditInfoProvider auditInfoProvider;
    @Autowired
    protected EntityAuditValues entityAuditInfoProvider;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected EclipselinkPersistenceSupport persistenceSupport;
    @Autowired
    protected AttributeChangesProvider attributeChangesProvider;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected MetadataTools metadataTools;

    protected boolean isJustSoftDeleted(Object entity) {
        if (EntityValues.isSoftDeletionSupported(entity)) {
            return EntityValues.isSoftDeleted(entity)
                    && attributeChangesProvider.isChanged(entity, metadataTools.getDeletedDateProperty(entity));
        }
        return false;
    }

    @Override
    public void aboutToDelete(DescriptorEvent event) {
    }

    @Override
    public void aboutToInsert(DescriptorEvent event) {
    }

    @Override
    public void aboutToUpdate(DescriptorEvent event) {
    }

    @Override
    public boolean isOverriddenEvent(DescriptorEvent event, List<DescriptorEventManager> eventManagers) {
        return false;
    }

    @Override
    public void postBuild(DescriptorEvent event) {
        if (event.getObject() instanceof Entity) {
            getUncheckedEntityEntry(event.getObject()).setNew(false);
            getUncheckedEntityEntry(event.getObject()).setDetached(false);
        }
        if (event.getObject() instanceof FetchGroupTracker) {
            FetchGroupTracker entity = (FetchGroupTracker) event.getObject();
            FetchGroup fetchGroup = entity._persistence_getFetchGroup();
            if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup, entityStates));
        }
    }

    @Override
    public void postClone(DescriptorEvent event) {
        // in shared cache mode, postBuild event is missed, so we repeat it here
        if (event.getObject() instanceof Entity) {
            ((Entity) event.getObject()).__copyEntityEntry();
            getUncheckedEntityEntry(event.getObject()).setNew(false);
            getUncheckedEntityEntry(event.getObject()).setDetached(false);
        }
        if (event.getObject() instanceof FetchGroupTracker) {
            FetchGroupTracker entity = (FetchGroupTracker) event.getObject();
            FetchGroup fetchGroup = entity._persistence_getFetchGroup();
            if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup, entityStates));
        }

        if (event.getObject() instanceof Entity)
            persistenceSupport.registerInstance(event.getObject(), event.getSession());
    }

    @Override
    public void postDelete(DescriptorEvent event) {
        String storeName = persistenceSupport.getStorageName(event.getSession());
        entityListenerManager.fireListener(event.getSource(), EntityListenerType.AFTER_DELETE, storeName);
    }

    @Override
    public void postInsert(DescriptorEvent event) {
        Object entity = event.getSource();
        String storeName = persistenceSupport.getStorageName(event.getSession());
        entityListenerManager.fireListener(entity, EntityListenerType.AFTER_INSERT, storeName);
        persistenceSupport.getSavedInstances(storeName).add(entity);
    }

    @Override
    public void postMerge(DescriptorEvent event) {
        EntityEntry targetEntry = ((Entity) event.getObject()).__getEntityEntry();
        EntityEntry sourceEntry = ((Entity) event.getOriginalObject()).__getEntityEntry();
        targetEntry.copy(sourceEntry);
        targetEntry.setManaged(true);
    }

    @Override
    public void postRefresh(DescriptorEvent event) {
        if (event.getObject() instanceof FetchGroupTracker) {
            FetchGroupTracker entity = (FetchGroupTracker) event.getObject();
            FetchGroup fetchGroup = entity._persistence_getFetchGroup();
            if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup, entityStates));
        }
    }

    @Override
    public void postUpdate(DescriptorEvent event) {
        String storeName = persistenceSupport.getStorageName(event.getSession());
        Object entity = event.getSource();
        if (isJustSoftDeleted(entity)) {
            entityListenerManager.fireListener(entity, EntityListenerType.AFTER_DELETE, storeName);
        } else {
            entityListenerManager.fireListener(entity, EntityListenerType.AFTER_UPDATE, storeName);
        }
    }

    @Override
    public void postWrite(DescriptorEvent event) {
    }

    @Override
    public void preDelete(DescriptorEvent event) {
    }

    @Override
    public void preInsert(DescriptorEvent event) {
    }

    @Override
    public void prePersist(DescriptorEvent event) {
        Object entity = event.getObject();

        Date currentDate = timeSource.currentTimestamp();
        UserDetails currentUser = auditInfoProvider.getCurrentUser();

        if (EntityValues.isAuditSupported(entity)) {
            entityAuditInfoProvider.setCreateInfo(entity, currentDate, currentUser);
            entityAuditInfoProvider.setUpdateInfo(entity, currentDate, currentUser, true);
        }
    }

    @Override
    public void preRemove(DescriptorEvent event) {
    }

    @Override
    public void preUpdate(DescriptorEvent event) {
        Object entity = event.getObject();
        if (!(isJustSoftDeleted(entity)) && EntityValues.isAuditSupported(entity)) {
            entityAuditInfoProvider.setUpdateInfo(entity, timeSource.currentTimestamp(), auditInfoProvider.getCurrentUser(), false);
        }
    }

    @Override
    public void preUpdateWithChanges(DescriptorEvent event) {
    }

    @Override
    public void preWrite(DescriptorEvent event) {
    }
}

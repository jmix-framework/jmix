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

package io.jmix.data.impl.eclipselink;

import io.jmix.core.EntityStates;
import io.jmix.core.Entity;
import io.jmix.core.MetadataTools;
import io.jmix.core.TimeSource;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.entity.EntitySystemAccess;
import io.jmix.core.entity.EntityValues;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.PersistenceTools;
import io.jmix.data.impl.EntityListenerManager;
import io.jmix.data.impl.EntityListenerType;
import io.jmix.data.impl.JmixEntityFetchGroup;
import io.jmix.data.impl.PersistenceSupport;
import io.jmix.data.impl.converters.AuditConversionService;
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
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

@Component(JmixEclipseLinkDescriptorEventListener.NAME)
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class JmixEclipseLinkDescriptorEventListener implements DescriptorEventListener {

    public static final String NAME = "data_EclipseLinkDescriptorEventListener";

    private final Logger logger = LoggerFactory.getLogger(JmixEclipseLinkDescriptorEventListener.class);

    @Autowired
    protected EntityListenerManager entityListenerManager;
    @Autowired
    protected AuditInfoProvider auditInfoProvider;
    @Autowired
    protected AuditConversionService auditConversionService;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected PersistenceSupport persistenceSupport;
    @Autowired
    protected PersistenceTools persistenceTools;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected MetadataTools metadataTools;

    protected boolean isJustSoftDeleted(Object entity) {
        if (EntityValues.isSoftDeletionSupported(entity)) {
            return EntityValues.isSoftDeleted(entity)
                    && persistenceTools.isDirty(entity, metadataTools.getDeletedDateProperty(entity));
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
            ((Entity) event.getObject()).__getEntityEntry().setNew(false);
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
            ((Entity) event.getObject()).__getEntityEntry().setNew(false);
        }
        if (event.getObject() instanceof FetchGroupTracker) {
            FetchGroupTracker entity = (FetchGroupTracker) event.getObject();
            FetchGroup fetchGroup = entity._persistence_getFetchGroup();
            if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup, entityStates));
        }

        if (event.getObject() instanceof Entity)
            persistenceSupport.registerInstance((Entity) event.getObject(), event.getSession());
    }

    @Override
    public void postDelete(DescriptorEvent event) {
        String storeName = persistenceSupport.getStorageName(event.getSession());
        entityListenerManager.fireListener((Entity) event.getSource(), EntityListenerType.AFTER_DELETE, storeName);
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
        BaseUser currentUser = auditInfoProvider.getCurrentUser();

        if (EntityValues.isAuditSupported(entity)) {
            setCreateInfo(entity, currentDate, currentUser);
            setUpdateInfo(entity, currentDate, currentUser, true);
        }
    }

    @Override
    public void preRemove(DescriptorEvent event) {
    }

    @Override
    public void preUpdate(DescriptorEvent event) {
        Object entity = event.getObject();
        if (!(isJustSoftDeleted(entity)) && EntityValues.isAuditSupported(entity)) {
            setUpdateInfo(entity, timeSource.currentTimestamp(), auditInfoProvider.getCurrentUser(), false);
        }
    }

    protected void setCreateInfo(Object entity, Date currentDate, @Nullable BaseUser currentUser) {
        Class<?> createdDateClass = EntitySystemAccess.getCreatedDateClass(entity);
        if (createdDateClass != null) {
            if (auditConversionService.canConvert(currentDate.getClass(), createdDateClass)) {
                EntityValues.setCreatedDate(entity, auditConversionService.convert(currentDate, createdDateClass));
            } else {
                logger.debug("Cannot find converter from java.util.Date to {}", createdDateClass);
            }
        }

        Class<?> createdByClass = EntitySystemAccess.getCreatedByClass(entity);
        if (createdByClass != null) {
            if (currentUser != null) {
                if (auditConversionService.canConvert(currentUser.getClass(), createdByClass)) {
                    EntityValues.setCreatedBy(entity, auditConversionService.convert(currentUser, createdByClass));
                } else {
                    logger.debug("Cannot find converter from {} to {}", currentUser.getClass().getName(), createdByClass);
                }
            } else {
                EntityValues.setCreatedBy(entity, null);
            }
        }
    }

    protected void setUpdateInfo(Object entity, Date currentDate, @Nullable BaseUser user, boolean dateOnly) {
        Class<?> lastModifiedDateClass = EntitySystemAccess.getLastModifiedDateClass(entity);
        if (lastModifiedDateClass != null) {
            if (auditConversionService.canConvert(currentDate.getClass(), lastModifiedDateClass)) {
                EntityValues.setLastModifiedDate(entity, auditConversionService.convert(currentDate, lastModifiedDateClass));
            } else {
                logger.debug("Cannot find converter from java.util.Date to {}", lastModifiedDateClass.getName());
            }
        }

        Class<?> lastModifiedByClass = EntitySystemAccess.getLastModifiedByClass(entity);
        if (lastModifiedByClass != null && !dateOnly) {
            if (user != null) {
                if (auditConversionService.canConvert(user.getClass(), lastModifiedByClass)) {
                    EntityValues.setLastModifiedBy(entity, auditConversionService.convert(user, lastModifiedByClass));
                } else {
                    logger.debug("Cannot find converter from {} to {}", user.getClass().getName(), lastModifiedByClass);
                }
            } else {
                EntityValues.setLastModifiedBy(entity, null);
            }
        }
    }

    @Override
    public void preUpdateWithChanges(DescriptorEvent event) {
    }

    @Override
    public void preWrite(DescriptorEvent event) {
    }
}

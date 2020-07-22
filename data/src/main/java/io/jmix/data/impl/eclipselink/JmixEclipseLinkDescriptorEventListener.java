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

import io.jmix.core.JmixEntity;
import io.jmix.core.TimeSource;
import io.jmix.core.entity.BaseUser;
import io.jmix.core.entity.EntityEntryAuditable;
import io.jmix.core.entity.SoftDelete;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.PersistenceTools;
import io.jmix.data.impl.EntityListenerManager;
import io.jmix.data.impl.EntityListenerType;
import io.jmix.data.impl.JmixEntityFetchGroup;
import io.jmix.data.impl.PersistenceSupport;
import io.jmix.data.impl.converters.AuditConvertionService;
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
    protected AuditConvertionService auditConversionService;
    @Autowired
    protected TimeSource timeSource;
    @Autowired
    protected PersistenceSupport persistenceSupport;
    @Autowired
    protected PersistenceTools persistenceTools;

    protected boolean justDeleted(SoftDelete entity) {
        return entity.isDeleted() && persistenceTools.getDirtyFields((JmixEntity) entity).contains("deleteTs");
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
        if (event.getObject() instanceof JmixEntity) {
            ((JmixEntity) event.getObject()).__getEntityEntry().setNew(false);
        }
        if (event.getObject() instanceof FetchGroupTracker) {
            FetchGroupTracker entity = (FetchGroupTracker) event.getObject();
            FetchGroup fetchGroup = entity._persistence_getFetchGroup();
            if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup));
        }
    }

    @Override
    public void postClone(DescriptorEvent event) {
        // in shared cache mode, postBuild event is missed, so we repeat it here
        if (event.getObject() instanceof JmixEntity) {
            ((JmixEntity) event.getObject()).__copyEntityEntry();
            ((JmixEntity) event.getObject()).__getEntityEntry().setNew(false);
        }
        if (event.getObject() instanceof FetchGroupTracker) {
            FetchGroupTracker entity = (FetchGroupTracker) event.getObject();
            FetchGroup fetchGroup = entity._persistence_getFetchGroup();
            if (fetchGroup != null && !(fetchGroup instanceof JmixEntityFetchGroup))
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup));
        }

        if (event.getObject() instanceof JmixEntity)
            persistenceSupport.registerInstance((JmixEntity) event.getObject(), event.getSession());
    }

    @Override
    public void postDelete(DescriptorEvent event) {
        String storeName = persistenceSupport.getStorageName(event.getSession());
        entityListenerManager.fireListener((JmixEntity) event.getSource(), EntityListenerType.AFTER_DELETE, storeName);
    }

    @Override
    public void postInsert(DescriptorEvent event) {
        JmixEntity entity = (JmixEntity) event.getSource();
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
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup));
        }
    }

    @Override
    public void postUpdate(DescriptorEvent event) {
        String storeName = persistenceSupport.getStorageName(event.getSession());
        JmixEntity entity = (JmixEntity) event.getSource();
        if (entity instanceof SoftDelete && persistenceTools.isDirty(entity, "deleteTs") && ((SoftDelete) entity).isDeleted()) {
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
        JmixEntity entity = (JmixEntity) event.getObject();
        Date ts = timeSource.currentTimestamp();
        BaseUser user = auditInfoProvider.getCurrentUser();

        if (entity.__getEntityEntry() instanceof EntityEntryAuditable) {
            setCreateInfo((EntityEntryAuditable) entity.__getEntityEntry(), ts, user);
            setUpdateInfo((EntityEntryAuditable) entity.__getEntityEntry(), ts, user, true);
        }
    }

    @Override
    public void preRemove(DescriptorEvent event) {
    }

    @Override
    public void preUpdate(DescriptorEvent event) {
        JmixEntity entity = (JmixEntity) event.getObject();
        if (!((entity instanceof SoftDelete) && justDeleted((SoftDelete) entity))
                && entity.__getEntityEntry() instanceof EntityEntryAuditable) {
            setUpdateInfo((EntityEntryAuditable) entity.__getEntityEntry(),
                    timeSource.currentTimestamp(),
                    auditInfoProvider.getCurrentUser(),
                    false);
        }
    }

    protected void setCreateInfo(EntityEntryAuditable auditable, Date ts, @Nullable BaseUser user) {
        if (auditable.getCreatedDateClass() != null) {
            Class<?> dateClass = auditable.getCreatedDateClass();
            if (auditConversionService.canConvert(ts.getClass(), dateClass)) {
                auditable.setCreatedDate(auditConversionService.convert(ts, dateClass));
            } else {
                logger.error("Cannot find converter from java.util.Date to " + dateClass.getName());
            }
        }

        if (auditable.getCreatedByClass() != null) {
            if (user != null) {
                Class<?> byClass = auditable.getCreatedByClass();
                if (auditConversionService.canConvert(user.getClass(), byClass)) {
                    auditable.setCreatedBy(auditConversionService.convert(user, byClass));
                } else {
                    logger.error("Cannot find converter from " + user.getClass().getName() + " to " + byClass.getName());
                }
            } else {
                auditable.setCreatedBy(null);
            }
        }
    }

    protected void setUpdateInfo(EntityEntryAuditable auditable, Date ts, @Nullable BaseUser user, boolean dateOnly) {
        if (auditable.getLastModifiedDateClass() != null) {
            Class<?> dateClass = auditable.getLastModifiedDateClass();
            if (auditConversionService.canConvert(ts.getClass(), dateClass)) {
                auditable.setLastModifiedDate(auditConversionService.convert(ts, dateClass));
            } else {
                logger.error("Cannot find converter from java.util.Date to " + dateClass.getName());
            }
        }

        if (auditable.getLastModifiedByClass() != null && !dateOnly) {
            if (user != null) {
                Class<?> byClass = auditable.getLastModifiedByClass();
                if (auditConversionService.canConvert(user.getClass(), byClass)) {
                    auditable.setLastModifiedBy(auditConversionService.convert(user, byClass));
                } else {
                    logger.error("Cannot find converter from " + user.getClass().getName() + " to " + byClass.getName());
                }
            } else {
                auditable.setLastModifiedBy(null);
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

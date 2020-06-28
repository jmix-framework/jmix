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

package io.jmix.data.impl;

import io.jmix.core.Entity;
import io.jmix.core.TimeSource;
import io.jmix.core.entity.*;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.PersistenceTools;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventListener;
import org.eclipse.persistence.descriptors.DescriptorEventManager;
import org.eclipse.persistence.queries.FetchGroup;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

@Component(EclipseLinkDescriptorEventListener.NAME)
public class EclipseLinkDescriptorEventListener implements DescriptorEventListener {

    public static final String NAME = "data_EclipseLinkDescriptorEventListener";

    private final Logger logger = LoggerFactory.getLogger(EclipseLinkDescriptorEventListener.class);

    @Autowired
    protected EntityListenerManager manager;

    @Autowired
    protected PersistenceTools persistenceTools;

    @Autowired
    protected AuditInfoProvider auditInfoProvider;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected PersistenceSupport support;

    @Autowired
    @Qualifier("jmix_auditConverter")
    protected ConversionService conversionService;

    protected boolean justDeleted(SoftDelete entity) {
        return entity.isDeleted() && persistenceTools.getDirtyFields((Entity) entity).contains("deleteTs");
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
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup));
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
                entity._persistence_setFetchGroup(new JmixEntityFetchGroup(fetchGroup));
        }

        if (event.getObject() instanceof Entity)
            support.registerInstance((Entity) event.getObject(), event.getSession());
    }

    @Override
    public void postDelete(DescriptorEvent event) {
        String storeName = support.getStorageName(event.getSession());
        manager.fireListener((Entity) event.getSource(), EntityListenerType.AFTER_DELETE, storeName);
    }

    @Override
    public void postInsert(DescriptorEvent event) {
        Entity entity = (Entity) event.getSource();
        String storeName = support.getStorageName(event.getSession());
        manager.fireListener(entity, EntityListenerType.AFTER_INSERT, storeName);
        support.getSavedInstances(storeName).add(entity);
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
        String storeName = support.getStorageName(event.getSession());
        Entity entity = (Entity) event.getSource();
        if (entity instanceof SoftDelete && persistenceTools.isDirty(entity, "deleteTs") && ((SoftDelete) entity).isDeleted()) {
            manager.fireListener(entity, EntityListenerType.AFTER_DELETE, storeName);
        } else {
            manager.fireListener(entity, EntityListenerType.AFTER_UPDATE, storeName);
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
        Entity entity = (Entity) event.getObject();
        Date ts = timeSource.currentTimestamp();
        BaseUser user = auditInfoProvider.getCurrentUser();

        if (entity instanceof Creatable) {
            ((Creatable) entity).setCreatedBy(auditInfoProvider.getCurrentUserUsername());
            ((Creatable) entity).setCreateTs(ts);
        }
        if (entity instanceof Updatable) {
            ((Updatable) entity).setUpdateTs(ts);
        }

        if (entity.__getEntityEntry() instanceof JmixAuditable) {
            setCreateInfo((JmixAuditable) entity.__getEntityEntry(), ts, user);
            setUpdateInfo((JmixAuditable) entity.__getEntityEntry(), ts, user, true);
        }
    }

    @Override
    public void preRemove(DescriptorEvent event) {
    }

    @Override
    public void preUpdate(DescriptorEvent event) {
        Entity entity = (Entity) event.getObject();
        if (!((entity instanceof SoftDelete) && justDeleted((SoftDelete) entity))) {
            if (entity instanceof Updatable) {
                Updatable updatable = (Updatable) event.getObject();
                updatable.setUpdatedBy(auditInfoProvider.getCurrentUserUsername());
                updatable.setUpdateTs(timeSource.currentTimestamp());
            }
            if (entity.__getEntityEntry() instanceof JmixAuditable) {
                setUpdateInfo((JmixAuditable) entity.__getEntityEntry(),
                        timeSource.currentTimestamp(),
                        auditInfoProvider.getCurrentUser(),
                        false);
            }
        }
    }

    protected void setCreateInfo(JmixAuditable auditable, Date ts, @Nullable BaseUser user) {
        if (auditable.getCreatedDateClass() != null) {
            Class<?> dateClass = auditable.getCreatedDateClass();
            if (conversionService.canConvert(ts.getClass(), dateClass)) {
                auditable.setCreatedDate(conversionService.convert(ts, dateClass));
            } else {
                logger.error("Cannot find converter from java.util.Date to " + dateClass.getName());
            }
        }

        if (auditable.getCreatedByClass() != null) {
            if (user != null) {
                Class<?> byClass = auditable.getCreatedByClass();
                if (conversionService.canConvert(user.getClass(), byClass)) {
                    auditable.setCreatedBy(conversionService.convert(user, byClass));
                } else {
                    logger.error("Cannot find converter from " + user.getClass().getName() + " to " + byClass.getName());
                }
            } else {
                auditable.setCreatedBy(null);
            }
        }
    }

    protected void setUpdateInfo(JmixAuditable auditable, Date ts, @Nullable BaseUser user, boolean dateOnly) {
        if (auditable.getUpdatedDateClass() != null) {
            Class<?> dateClass = auditable.getUpdatedDateClass();
            if (conversionService.canConvert(ts.getClass(), dateClass)) {
                auditable.setUpdatedDate(conversionService.convert(ts, dateClass));
            } else {
                logger.error("Cannot find converter from java.util.Date to " + dateClass.getName());
            }
        }

        if (auditable.getUpdatedByClass() != null && !dateOnly) {
            if (user != null) {
                Class<?> byClass = auditable.getUpdatedByClass();
                if (conversionService.canConvert(user.getClass(), byClass)) {
                    auditable.setUpdatedBy(conversionService.convert(user, byClass));
                } else {
                    logger.error("Cannot find converter from " + user.getClass().getName() + " to " + byClass.getName());
                }
            } else {
                auditable.setUpdatedBy(null);
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

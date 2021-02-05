/*
 * Copyright 2020 Haulmont.
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

package io.jmix.hibernate.impl.listeners;

import io.jmix.core.MetadataTools;
import io.jmix.core.TimeSource;
import io.jmix.core.entity.EntityValues;
import io.jmix.data.AuditInfoProvider;
import io.jmix.data.EntityAuditInfoProvider;
import io.jmix.hibernate.impl.HibernatePersistenceTools;
import io.jmix.hibernate.impl.HibernatePersistenceSupport;
import io.jmix.data.impl.EntityListenerManager;
import io.jmix.data.impl.EntityListenerType;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component("hibernate_HibernateFireEventsEventListener")
public class HibernateFireEventsEventListener implements PreInsertEventListener,
        PostInsertEventListener,
        PreUpdateEventListener,
        PostUpdateEventListener,
        PreDeleteEventListener,
        PostDeleteEventListener {

    private final Logger logger = LoggerFactory.getLogger(HibernateFireEventsEventListener.class);

    @Autowired
    protected EntityListenerManager entityListenerManager;

    @Autowired
    protected HibernatePersistenceSupport persistenceSupport;

    @Lazy
    @Autowired
    protected HibernatePersistenceTools persistenceTools;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected AuditInfoProvider auditInfoProvider;

    @Autowired
    protected EntityAuditInfoProvider entityAuditInfoProvider;

    protected boolean isJustSoftDeleted(Object entity) {
        if (EntityValues.isSoftDeletionSupported(entity)) {
            return EntityValues.isSoftDeleted(entity)
                    && persistenceTools.isDirty(entity, metadataTools.getDeletedDateProperty(entity));
        }
        return false;
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        String storeName = persistenceSupport.getStorageName(event.getSession());
        entityListenerManager.fireListener(event.getEntity(), EntityListenerType.AFTER_DELETE, storeName);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        Object entity = event.getEntity();
        String storeName = persistenceSupport.getStorageName(event.getSession());
        entityListenerManager.fireListener(entity, EntityListenerType.AFTER_INSERT, storeName);
        persistenceSupport.getSavedInstances(storeName).add(entity);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        String storeName = persistenceSupport.getStorageName(event.getSession());
        Object entity = event.getEntity();
        if (isJustSoftDeleted(entity)) {
            entityListenerManager.fireListener(entity, EntityListenerType.AFTER_DELETE, storeName);
        } else {
            entityListenerManager.fireListener(entity, EntityListenerType.AFTER_UPDATE, storeName);
        }
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        return false;
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        Object entity = event.getEntity();

        Date currentDate = timeSource.currentTimestamp();
        UserDetails currentUser = auditInfoProvider.getCurrentUser();

        if (EntityValues.isAuditSupported(entity)) {
            entityAuditInfoProvider.setCreateInfo(entity, currentDate, currentUser);
            entityAuditInfoProvider.setUpdateInfo(entity, currentDate, currentUser, true);
            updateStateFields(event.getSession(), entity, event.getState());
        }
        return false;
    }

    private void updateStateFields(SessionImplementor session, Object entity, Object[] currentState) {
        EntityEntry entry = session.getPersistenceContextInternal().getEntry(entity);
        Object[] valuesToInsert = entry.getPersister().getPropertyValuesToInsert(entity, null, session);
        for (int i = 0; i < currentState.length; i++) {
            currentState[i] = valuesToInsert[i];
        }
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        Object entity = event.getEntity();
        if (!(isJustSoftDeleted(entity)) && EntityValues.isAuditSupported(entity)) {
            entityAuditInfoProvider.setUpdateInfo(entity, timeSource.currentTimestamp(), auditInfoProvider.getCurrentUser(), false);
            updateStateFields(event.getSession(), entity, event.getState());
        }
        return false;
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }
}

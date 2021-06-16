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

package io.jmix.data.impl;

import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.datastore.DataStoreBeforeEntityCountEvent;
import io.jmix.core.datastore.DataStoreBeforeEntityLoadEvent;
import io.jmix.core.datastore.DataStoreBeforeEntitySaveEvent;
import io.jmix.core.datastore.DataStoreEventListener;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.AccessDeniedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("data_DataStoreCrudListener")
public class DataStoreCrudListener implements DataStoreEventListener {

    private static final Logger log = LoggerFactory.getLogger(DataStoreCrudListener.class);

    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected Metadata metadata;

    public void beforeEntityLoad(DataStoreBeforeEntityLoadEvent event) {
        LoadContext<?> context = event.getLoadContext();

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyConstraints(entityContext, context.getAccessConstraints());

        if (!entityContext.isReadPermitted()) {
            log.debug("Reading entity {} is not permitted by access constraints", metaClass);
            event.setLoadPrevented();
        }
    }

    public void beforeEntityCount(DataStoreBeforeEntityCountEvent event) {
        LoadContext<?> context = event.getLoadContext();

        MetaClass metaClass = extendedEntities.getEffectiveMetaClass(context.getEntityMetaClass());

        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyConstraints(entityContext, context.getAccessConstraints());

        if (!entityContext.isReadPermitted()) {
            log.debug("Reading entity {} is not permitted by access constraints", metaClass);
            event.setCountPrevented();
        }
    }

    @Override
    public void beforeEntitySave(DataStoreBeforeEntitySaveEvent event) {
        SaveContext context = event.getSaveContext();
        Collection<AccessConstraint<?>> accessConstraints = context.getAccessConstraints();

        if (accessConstraints.isEmpty()) {
            return;
        }

        Map<MetaClass, CrudEntityContext> accessCache = new HashMap<>();

        for (Object entity : context.getEntitiesToSave()) {
            if (entity == null) {
                continue;
            }

            MetaClass metaClass = metadata.getClass(entity);
            CrudEntityContext entityContext = accessCache.computeIfAbsent(metaClass,
                    key -> evaluateCrudAccess(key, accessConstraints));

            if (entityStates.isNew(entity)) {
                if (!entityContext.isCreatePermitted()) {
                    throw new AccessDeniedException("entity", metaClass.getName(), "create");
                }
            } else if (!entityContext.isUpdatePermitted()) {
                throw new AccessDeniedException("entity", metaClass.getName(), "update");
            }
        }

        for (Object entity : context.getEntitiesToRemove()) {
            if (entity == null) {
                continue;
            }

            MetaClass metaClass = metadata.getClass(entity);
            CrudEntityContext entityContext = accessCache.computeIfAbsent(metaClass,
                    key -> evaluateCrudAccess(key, accessConstraints));

            if (!entityContext.isDeletePermitted()) {
                throw new AccessDeniedException("entity", metaClass.getName(), "update");
            }
        }

    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE;
    }

    protected CrudEntityContext evaluateCrudAccess(MetaClass metaClass, Collection<AccessConstraint<?>>
            accessConstraints) {
        CrudEntityContext entityContext = new CrudEntityContext(metaClass);
        accessManager.applyConstraints(entityContext, accessConstraints);
        return entityContext;
    }
}

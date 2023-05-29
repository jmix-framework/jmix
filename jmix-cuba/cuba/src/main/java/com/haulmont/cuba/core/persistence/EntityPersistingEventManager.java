/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.core.persistence;

import com.haulmont.cuba.core.app.events.EntityPersistingEvent;
import com.haulmont.cuba.core.global.Events;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.core.metamodel.model.MetaClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component(EntityPersistingEventManager.NAME)
public class EntityPersistingEventManager {

    public static final String NAME = "cuba_EntityPersistingEventManager";

    @Autowired
    protected Events events;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ExtendedEntities extendedEntities;

    @EventListener
    public void handleEvent(EntitySavingEvent<?> entitySavingEvent) {
        if (entitySavingEvent.isNewEntity()) {
            EntityPersistingEvent<Object> event = new EntityPersistingEvent<>(
                    this,
                    entitySavingEvent.getEntity(),
                    getOriginalMetaClass(entitySavingEvent.getEntity())
            );
            events.publish(event);
        }
    }

    private MetaClass getOriginalMetaClass(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        return extendedEntities.getOriginalOrThisMetaClass(metaClass);
    }
}

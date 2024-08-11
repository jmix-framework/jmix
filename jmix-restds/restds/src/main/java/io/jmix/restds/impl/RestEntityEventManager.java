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

package io.jmix.restds.impl;

import io.jmix.core.ExtendedEntities;
import io.jmix.core.Metadata;
import io.jmix.core.event.EntityLoadingEvent;
import io.jmix.core.event.EntitySavingEvent;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.restds.event.RestEntityRemovedEvent;
import io.jmix.restds.event.RestEntitySavedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component("restds_RestEntityEventManager")
public class RestEntityEventManager {

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ExtendedEntities extendedEntities;

    public void publishEntitySavingEvent(Object entity, boolean isNew) {
        var event = new EntitySavingEvent<>(this, getOriginalMetaClass(entity), entity, isNew);
        applicationEventPublisher.publishEvent(event);
    }

    public void publishEntityLoadingEvent(Object entity) {
        var event = new EntityLoadingEvent<>(this, getOriginalMetaClass(entity), entity);
        applicationEventPublisher.publishEvent(event);
    }

    public void publishEntitySavedEvent(Object savedEntity, Object returnedEntity, boolean isNew) {
        var event = new RestEntitySavedEvent<>(this, getOriginalMetaClass(savedEntity), savedEntity, returnedEntity, isNew);
        applicationEventPublisher.publishEvent(event);
    }

    public void publishEntityRemovedEvent(Object entity) {
        var event = new RestEntityRemovedEvent<>(this, getOriginalMetaClass(entity), entity);
        applicationEventPublisher.publishEvent(event);
    }

    private MetaClass getOriginalMetaClass(Object entity) {
        MetaClass metaClass = metadata.getClass(entity);
        return extendedEntities.getOriginalOrThisMetaClass(metaClass);
    }
}

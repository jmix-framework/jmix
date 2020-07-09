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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.cuba.core.app.events.AttributeChanges;
import com.haulmont.cuba.core.app.events.EntityChangedEvent;
import com.haulmont.cuba.core.entity.contracts.Id;
import io.jmix.core.JmixEntity;
import io.jmix.core.Events;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("jmix_CubaEntityChangedEventManager")
public class CubaEntityChangedEventManager {
    @Autowired
    private Events eventPublisher;

    @EventListener
    public void handleEvent(io.jmix.data.event.EntityChangedEvent<? extends JmixEntity> event) {
        io.jmix.core.Id<? extends JmixEntity> entityId = event.getEntityId();
        eventPublisher.publish(new EntityChangedEvent<>(event.getSource(),
                Id.of(entityId.getValue(), entityId.getEntityClass()),
                resolveType(event.getType()),
                new AttributeChanges(event.getChanges())));
    }

    private EntityChangedEvent.Type resolveType(io.jmix.data.event.EntityChangedEvent.Type type) {
        if (type == io.jmix.data.event.EntityChangedEvent.Type.CREATED) {
            return EntityChangedEvent.Type.CREATED;
        } else if (type == io.jmix.data.event.EntityChangedEvent.Type.UPDATED) {
            return EntityChangedEvent.Type.UPDATED;
        } else if (type == io.jmix.data.event.EntityChangedEvent.Type.DELETED) {
            return EntityChangedEvent.Type.DELETED;
        }
        return null;
    }
}

/*
 * Copyright 2022 Haulmont.
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

package io.jmix.securitydata.listener;

import io.jmix.core.event.EntityChangedEvent;
import io.jmix.security.impl.role.event.ResourceRoleModifiedEvent;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener is fired when {@link ResourceRoleEntity} or {@link ResourcePolicyEntity} is changed. Listener job is to send
 * the {@link ResourceRoleModifiedEvent}.
 */
@Component("sec_ResourceRoleEntityChangedEventListener")
public class ResourceRoleEntityChangedEventListener {

    private ApplicationEventPublisher eventPublisher;

    public ResourceRoleEntityChangedEventListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener
    void onResourceRoleEntityChanged(EntityChangedEvent<ResourceRoleEntity> event) {
        publishRoleModifiedEvent();
    }

    @TransactionalEventListener
    void onResourcePolicyEntityChanged(EntityChangedEvent<ResourcePolicyEntity> event) {
        publishRoleModifiedEvent();
    }

    protected void publishRoleModifiedEvent() {
        eventPublisher.publishEvent(new ResourceRoleModifiedEvent(this));
    }
}

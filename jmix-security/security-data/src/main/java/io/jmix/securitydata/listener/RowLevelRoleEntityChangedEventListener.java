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
import io.jmix.security.impl.role.event.RowLevelRoleModifiedEvent;
import io.jmix.securitydata.entity.ResourcePolicyEntity;
import io.jmix.securitydata.entity.ResourceRoleEntity;
import io.jmix.securitydata.entity.RowLevelPolicyEntity;
import io.jmix.securitydata.entity.RowLevelRoleEntity;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener is fired when {@link RowLevelRoleEntity} or {@link RowLevelPolicyEntity} is changed. Listener job is to send
 * the {@link RowLevelRoleModifiedEvent}.
 */
@Component("sec_RowLevelRoleEntityChangedEventListener")
public class RowLevelRoleEntityChangedEventListener {

    private ApplicationEventPublisher eventPublisher;

    public RowLevelRoleEntityChangedEventListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @TransactionalEventListener
    void onRowLevelRoleEntityChanged(EntityChangedEvent<RowLevelRoleEntity> event) {
        publishRoleModifiedEvent();
    }

    @TransactionalEventListener
    void onRowLevelPolicyEntityChanged(EntityChangedEvent<RowLevelPolicyEntity> event) {
        publishRoleModifiedEvent();
    }

    protected void publishRoleModifiedEvent() {
        eventPublisher.publishEvent(new RowLevelRoleModifiedEvent(this));
    }
}

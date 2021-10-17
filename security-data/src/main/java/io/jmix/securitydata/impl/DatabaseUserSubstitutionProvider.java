/*
 * Copyright 2021 Haulmont.
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

package io.jmix.securitydata.impl;

import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.core.usersubstitution.UserSubstitution;
import io.jmix.core.usersubstitution.UserSubstitutionProvider;
import io.jmix.core.usersubstitution.event.UserSubstitutionsChangedEvent;
import io.jmix.securitydata.entity.UserSubstitutionEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A {@link UserSubstitutionProvider} that stores {@link UserSubstitution} in a database.
 */
@Component("sec_DatabaseUserSubstitutionProvider")
public class DatabaseUserSubstitutionProvider implements UserSubstitutionProvider {

    @Autowired
    private UnconstrainedDataManager dataManager;

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    @Override
    public Collection<UserSubstitution> getUserSubstitutions(String username, Date date) {
        List<UserSubstitutionEntity> userSubstitutionEntities = dataManager.load(UserSubstitutionEntity.class)
                .query("e.username = :username " +
                        "and (e.startDate is null or e.startDate <= :date) " +
                        "and (e.endDate is null or e.endDate >= :date)")
                .parameter("date", date)
                .parameter("username", username)
                .list();

        return userSubstitutionEntities.stream()
                .map(entity -> new UserSubstitution(entity.getUsername(),
                        entity.getSubstitutedUsername(),
                        entity.getStartDate(),
                        entity.getEndDate()))
                .collect(Collectors.toList());
    }

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    protected void onUserRemove(UserRemovedEvent event) {
        List<UserSubstitutionEntity> substitutions = dataManager.load(UserSubstitutionEntity.class)
                .query("e.username = :username or e.substitutedUsername = :username")
                .parameter("username", event.getUsername())
                .list();

        if (CollectionUtils.isNotEmpty(substitutions)) {
            dataManager.remove(substitutions.toArray());

            substitutions.stream()
                    .map(UserSubstitutionEntity::getUsername)
                    .distinct()
                    .forEach(this::fireUserSubstitutionsChanged);
        }
    }

    protected void fireUserSubstitutionsChanged(String username) {
        UserSubstitutionsChangedEvent changedEvent = new UserSubstitutionsChangedEvent(username);
        eventPublisher.publishEvent(changedEvent);
    }
}

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

package io.jmix.flowuidata.settings;

import io.jmix.core.annotation.Internal;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.flowuidata.entity.UserSettingsItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Internal
@Component("flowui_UserSettingsRemoveUserListener")
public class UserSettingsRemoveUserListener {

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onUserRemove(UserRemovedEvent event) {
        String username = event.getUsername();

        List<UserSettingsItem> settings = entityManager.createQuery(
                        "select s from flowui_UserSettingsItem s where s.username = ?1", UserSettingsItem.class)
                .setParameter(1, username)
                .getResultList();

        for (UserSettingsItem setting : settings) {
            entityManager.remove(setting);
        }
    }
}

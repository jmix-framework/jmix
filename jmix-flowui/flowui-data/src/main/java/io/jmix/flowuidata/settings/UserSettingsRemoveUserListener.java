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

import io.jmix.core.DataManager;
import io.jmix.core.annotation.Internal;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.flowuidata.entity.UserSettingsItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Internal
@Component("flowui_UserSettingsRemoveUserListener")
public class UserSettingsRemoveUserListener {

    @Autowired
    protected DataManager dataManager;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)
    public void onUserRemove(UserRemovedEvent event) {
        String username = event.getUsername();

        List<UserSettingsItem> settings = dataManager.load(UserSettingsItem.class)
                .query("e.username = ?1", username)
                .list();

        for (UserSettingsItem setting : settings) {
            dataManager.remove(setting);
        }
    }
}

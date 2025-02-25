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

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;
import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowuidata.entity.UserSettingsItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Component("flowui_UserSettingsServiceImpl")
public class UserSettingsServiceImpl implements UserSettingsService {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsServiceImpl.class);

    protected CurrentAuthentication authentication;
    protected Metadata metadata;
    protected AccessManager accessManager;
    protected UnconstrainedDataManager dataManager;
    protected TransactionTemplate transaction;

    public UserSettingsServiceImpl(CurrentAuthentication authentication,
                                   Metadata metadata,
                                   AccessManager accessManager,
                                   UnconstrainedDataManager dataManager,
                                   PlatformTransactionManager transactionManager) {
        this.authentication = authentication;
        this.metadata = metadata;
        this.accessManager = accessManager;
        this.dataManager = dataManager;

        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Optional<String> load(String key) {
        Preconditions.checkNotNullArgument(key);

        // During logout, the authentication object may not be available, so we skip the operation.
        if (notAuthenticated()) {
            return Optional.empty();
        }

        String value = transaction.execute(status -> {
            UserSettingsItem us = findUserSettings(key);
            return us == null ? null : us.getValue();
        });

        log.trace("Loaded key '{}', value '{}'", key, value);

        return Optional.ofNullable(value);
    }

    @Override
    public void save(String key, @Nullable String value) {
        Preconditions.checkNotNullArgument(key);

        // During logout, the authentication object may not be available, so we skip the operation.
        if (notAuthenticated()) {
            return;
        }

        transaction.executeWithoutResult(status -> {
            UserSettingsItem us = findUserSettings(key);
            if (us == null) {
                us = metadata.create(UserSettingsItem.class);
                us.setUsername(authentication.getUser().getUsername());
                us.setKey(key);
                us.setValue(value);
            } else {
                us.setValue(value);
            }
            dataManager.save(us);
        });
    }

    @Override
    public void delete(String key) {
        Preconditions.checkNotNullArgument(key);

        // During logout, the authentication object may not be available, so we skip the operation.
        if (notAuthenticated()) {
            return;
        }

        transaction.executeWithoutResult(status -> {
            UserSettingsItem us = findUserSettings(key);
            if (us != null) {
                dataManager.remove(us);
            }
        });
    }

    @Override
    public void copy(String fromUsername, String toUsername) {
        Preconditions.checkNotNullArgument(fromUsername);
        Preconditions.checkNotNullArgument(toUsername);

        transaction.executeWithoutResult(status -> {
             dataManager.load(UserSettingsItem.class)
                     .query("e.username = ?1", toUsername)
                     .optional()
                     .ifPresent(userSettingsItem -> dataManager.remove(userSettingsItem));
        });

        transaction.executeWithoutResult(status -> {
            List<UserSettingsItem> fromUserSettings = dataManager.load(UserSettingsItem.class)
                    .query("e.username = ?1", fromUsername)
                    .list();

            for (UserSettingsItem currSetting : fromUserSettings) {
                UserSettingsItem newSetting = metadata.create(UserSettingsItem.class);
                newSetting.setUsername(toUsername);
                newSetting.setKey(currSetting.getKey());
                newSetting.setValue(currSetting.getValue());
                dataManager.save(newSetting);
            }
        });
    }

    @Nullable
    protected UserSettingsItem findUserSettings(String key) {
        List<UserSettingsItem> result = dataManager.load(UserSettingsItem.class)
                .query("e.username = ?1 and e.key =?2", authentication.getUser().getUsername(), key)
                .list();
        return result.isEmpty() ? null : result.get(0);
    }

    protected boolean notAuthenticated() {
        VaadinSession session = VaadinSession.getCurrent();
        return (session != null && session.getState() != VaadinSessionState.OPEN)
                || SecurityContextHelper.getAuthentication() == null;
    }
}

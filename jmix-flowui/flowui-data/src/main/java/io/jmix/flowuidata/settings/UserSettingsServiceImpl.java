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

import io.jmix.core.AccessManager;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.event.UserRemovedEvent;
import io.jmix.data.impl.EntityEventManager;
import io.jmix.flowui.settings.UserSettingsService;
import io.jmix.flowuidata.entity.UiSetting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

@Component("ui_UserSettingsServiceImpl")
public class UserSettingsServiceImpl implements UserSettingsService {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsServiceImpl.class);

    protected CurrentAuthentication authentication;
    protected Metadata metadata;
    protected AccessManager accessManager;
    protected EntityEventManager entityEventManager;

    @PersistenceContext
    protected EntityManager entityManager;

    protected TransactionTemplate transaction;

    public UserSettingsServiceImpl(CurrentAuthentication authentication,
                                   Metadata metadata,
                                   AccessManager accessManager,
                                   EntityEventManager entityEventManager,
                                   PlatformTransactionManager transactionManager) {
        this.authentication = authentication;
        this.metadata = metadata;
        this.accessManager = accessManager;
        this.entityEventManager = entityEventManager;

        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public Optional<String> load(String key) {
        Preconditions.checkNotNullArgument(key);

        String value = transaction.execute(status -> {
            UiSetting us = findUserSettings(key);
            return us == null ? null : us.getValue();
        });

        log.trace("Loaded key '{}', value '{}'", key, value);

        return Optional.ofNullable(value);
    }

    @Override
    public void save(String key, @Nullable String value) {
        Preconditions.checkNotNullArgument(key);

        transaction.executeWithoutResult(status -> {
            UiSetting us = findUserSettings(key);
            if (us == null) {
                us = metadata.create(UiSetting.class);
                us.setUsername(authentication.getUser().getUsername());
                us.setKey(key);
                us.setValue(value);
                entityEventManager.publishEntitySavingEvent(us, true); //workaround for jmix-framework/jmix#1069
                entityManager.persist(us);
            } else {
                us.setValue(value);
            }
        });
    }

    @Override
    public void delete(String key) {
        Preconditions.checkNotNullArgument(key);

        transaction.executeWithoutResult(status -> {
            UiSetting us = findUserSettings(key);
            if (us != null) {
                entityManager.remove(us);
            }
        });
    }

    @Override
    public void copy(String fromUsername, String toUsername) {
        Preconditions.checkNotNullArgument(fromUsername);
        Preconditions.checkNotNullArgument(toUsername);

        transaction.executeWithoutResult(status ->
                entityManager.createQuery("delete from ui_UiSetting s where s.username = ?1")
                        .setParameter(1, toUsername)
                        .executeUpdate());

        transaction.executeWithoutResult(status -> {
            List<UiSetting> fromUserSettings =
                    entityManager.createQuery("select s from ui_UiSetting s where s.username = ?1", UiSetting.class)
                            .setParameter(1, fromUsername)
                            .getResultList();

            for (UiSetting currSetting : fromUserSettings) {
                UiSetting newSetting = metadata.create(UiSetting.class);
                newSetting.setUsername(toUsername);
                newSetting.setKey(currSetting.getKey());
                newSetting.setValue(currSetting.getValue());

                entityEventManager.publishEntitySavingEvent(newSetting, true); //workaround for jmix-framework/jmix#1069
                entityManager.persist(newSetting);
            }
        });
    }

    @Nullable
    protected UiSetting findUserSettings(String key) {
        List<UiSetting> result = entityManager.createQuery(
                        "select s from ui_UiSetting s where s.username = ?1 and s.key =?2",
                        UiSetting.class)
                .setParameter(1, authentication.getUser().getUsername())
                .setParameter(2, key)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }
}

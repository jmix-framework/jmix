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

package io.jmix.email.impl;

import io.jmix.core.FetchPlanRepository;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageLocator;
import io.jmix.core.TimeSource;
import io.jmix.data.PersistenceHints;
import io.jmix.email.EmailCleaner;
import io.jmix.email.EmailerProperties;
import io.jmix.email.entity.SendingMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@Component("email_EmailCleaner")
public class EmailCleanerImpl implements EmailCleaner {

    private static final Logger log = LoggerFactory.getLogger(EmailCleanerImpl.class);

    @Autowired
    private EmailerProperties emailerProperties;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TimeSource timeSource;

    @Autowired
    private FetchPlanRepository fetchPlanRepository;

    @Autowired
    private FileStorageLocator fileStorageLocator;

    @Transactional
    @Override
    public Integer deleteOldEmails() {
        log.trace("Start deletion of old emails...");
        int maxAgeOfImportantMessages = emailerProperties.getMaxAgeOfImportantMessages();
        int maxAgeOfNonImportantMessages = emailerProperties.getMaxAgeOfNonImportantMessages();
        entityManager.setProperty(PersistenceHints.SOFT_DELETION, false);

        int result = 0;
        if (maxAgeOfNonImportantMessages != 0) {
            result += deleteMessages(maxAgeOfNonImportantMessages, false);
        }

        if (maxAgeOfImportantMessages != 0) {
            result += deleteMessages(maxAgeOfImportantMessages, true);
        }

        log.trace("{} emails was deleted", result);
        return result;
    }

    private int deleteMessages(int ageOfMessage, boolean important) {
        FileStorage fileStorage = fileStorageLocator.getDefault();
        List<SendingMessage> messagesToDelete = entityManager.createQuery("select msg from email_SendingMessage msg" +
                " where msg.important = :important and msg.createTs < :date", SendingMessage.class)
                .setParameter("important", important)
                .setParameter("date", Date.from(timeSource.now().minusDays(ageOfMessage).toInstant()))
                .setHint(PersistenceHints.FETCH_PLAN,
                        fetchPlanRepository.getFetchPlan(SendingMessage.class, "sendingMessage.deleteFile"))
                .getResultList();
        if (emailerProperties.getCleanFileStorage()) {
            messagesToDelete.forEach(msg -> {
                msg.getAttachments().stream()
                        .filter(attachment -> attachment.getContentFile() != null)
                        .forEach(attachment -> fileStorage.removeFile(attachment.getContentFile()));
                if (msg.getContentTextFile() != null) {
                    fileStorage.removeFile(msg.getContentTextFile());
                }
            });
        }

        if (!messagesToDelete.isEmpty()) {
            return entityManager.createQuery("delete from email_SendingMessage msg where msg.id in :ids", Integer.class)
                    .setParameter("ids", messagesToDelete.stream().map(SendingMessage::getId).collect(Collectors.toList()))
                    .executeUpdate();
        } else {
            return 0;
        }
    }
}

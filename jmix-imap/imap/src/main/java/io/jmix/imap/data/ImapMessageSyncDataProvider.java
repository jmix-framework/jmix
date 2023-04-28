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

package io.jmix.imap.data;

import io.jmix.core.DataManager;
import io.jmix.core.FluentLoader;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.imap.ImapProperties;
import io.jmix.imap.entity.ImapFolder;
import io.jmix.imap.entity.ImapMessage;
import io.jmix.imap.entity.ImapMessageSync;
import io.jmix.imap.entity.ImapSyncStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;

import jakarta.mail.Flags;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component("imap_ImapMessageSyncDataProvider")
public class ImapMessageSyncDataProvider {

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected ImapProperties imapProperties;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected DataManager dataManager;

    protected TransactionTemplate transaction;

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public List<ImapMessage> findMessagesWithSyncStatus(ImapFolder imapFolder, ImapSyncStatus status, Integer maxSize) {
        FluentLoader.ByQuery<ImapMessage> query = dataManager.load(ImapMessage.class)
                .query("select m from imap_Message m where m.id in " +
                        "(select ms.message.id from imap_MessageSync ms where ms.folder.id = :folder and ms.status = :status)")
                .parameter("folder", imapFolder.getId())
                .parameter("status", status)
                .fetchPlan("imap-msg-full");
        if (maxSize != null) {
            query = query.maxResults(maxSize);
        }
        return query.list();
    }

    public List<ImapMessage> findMessagesWithSyncStatus(ImapFolder folder, ImapSyncStatus status, Date minUpdateDate, Date maxUpdateDate) {
        return dataManager.load(ImapMessage.class)
                .query("select m from imap_Message m where m.id in " +
                        "(select ms.message.id from imap_MessageSync ms where ms.folder.id = :folder and ms.status = :status" +
                        " and ms.updateTs >= :minUpdateDate and ms.updateTs < :maxUpdateDate)")
                .parameter("folder", folder.getId())
                .parameter("status", status)
                .parameter("minUpdateDate", minUpdateDate)
                .parameter("maxUpdateDate", maxUpdateDate)
                .fetchPlan("imap-msg-full")
                .list();
    }

    public Collection<ImapMessageSync> findMessagesSyncs(ImapFolder folder, ImapSyncStatus status, Integer maxSize) {
        FluentLoader.ByQuery<ImapMessageSync> query = dataManager.load(ImapMessageSync.class)
                .query("select ms from imap_MessageSync ms where ms.folder.id = :folder and ms.status = :status")
                .parameter("folder", folder.getId())
                .parameter("status", status)
                .fetchPlan("imap-msg-sync-with-message");
        if (maxSize != null) {
            query = query.maxResults(maxSize);
        }
        return query.list();
    }

    public ImapMessageSync findSync(ImapMessage message) {
        return dataManager.load(ImapMessageSync.class)
                .query("select ms from imap_MessageSync ms where ms.message.id = :msgId")
                .parameter("msgId", message.getId())
                .optional()
                .orElse(null);
    }

    public void saveSync(ImapMessageSync messageSync) {
        dataManager.save(messageSync);
    }

    public Collection<ImapMessage> findMessagesForSync(ImapFolder imapFolder) {
        return dataManager.load(ImapMessage.class)
                .query("select m from imap_Message m where m.folder.id = :folder and m.id not in " +
                        "(select ms.message.id from imap_MessageSync ms) order by m.updateTs asc")
                .parameter("folder", imapFolder.getId())
                .fetchPlan("imap-msg-full")
                .maxResults(imapProperties.getUpdateBatchSize())
                .list();
    }

    public void createSyncForMessages(Collection<ImapMessage> messages, ImapSyncStatus syncStatus) {
        transaction.executeWithoutResult(transactionStatus -> {
            messages.forEach(message -> {
                ImapMessageSync sync = findMessageSync(message);
                if (sync == null) {
                    ImapMessageSync messageSync = metadata.create(ImapMessageSync.class);
                    messageSync.setMessage(message);
                    messageSync.setStatus(syncStatus);
                    messageSync.setFolder(message.getFolder());
                    entityManager.persist(messageSync);
                    message.setUpdateTs(timeSource.currentTimestamp());
                    entityManager.merge(message);
                }
            });
        });
    }

    public void updateSyncStatus(ImapMessage message,
                                 ImapSyncStatus syncStatus,
                                 ImapSyncStatus oldStatus,
                                 Flags flags,
                                 ImapFolder oldFolder) {

        transaction.execute(transactionStatus -> {
            ImapMessageSync messageSync = oldStatus == null ? findMessageSync(message) : findMessageSync(message, oldStatus);
            if (messageSync != null) {
                messageSync.setStatus(syncStatus);
                if (flags != null) {
                    messageSync.setImapFlags(flags);
                }
                if (oldFolder != null) {
                    messageSync.setOldFolder(oldFolder);
                }
                entityManager.persist(messageSync);
            }
            return messageSync;
        });
    }

    public ImapMessageSync findMessageSync(ImapMessage message) {
        List<ImapMessageSync> resultList = entityManager.createQuery(
                "select ms from imap_MessageSync ms where ms.message.id = :msgId", ImapMessageSync.class)
                .setParameter("msgId", message.getId())
                .setMaxResults(1)
                .getResultList();
        return CollectionUtils.isEmpty(resultList) ? null : resultList.get(0);
    }

    protected ImapMessageSync findMessageSync(ImapMessage message, ImapSyncStatus status) {
        List<ImapMessageSync> resultList = entityManager.createQuery(
                "select ms from imap_MessageSync ms where ms.message.id = :msgId and ms.status = :oldStatus", ImapMessageSync.class)
                .setParameter("msgId", message.getId())
                .setParameter("oldStatus", status.getId())
                .setMaxResults(1)
                .getResultList();
        return CollectionUtils.isEmpty(resultList) ? null : resultList.get(0);
    }

    public void removeMessagesSyncs(Collection<UUID> messageIds) {
        transaction.executeWithoutResult(transactionStatus -> {
            final AtomicInteger counter = new AtomicInteger(0);
            Collection<List<UUID>> partitions = messageIds.stream()
                    .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / 1000))
                    .values();
            for (List<UUID> messageIdsPartition : partitions) {
                entityManager.createQuery("delete from imap_MessageSync ms where ms.message.id in :msgIds")
                        .setParameter("msgIds", messageIdsPartition)
                        .executeUpdate();
            }
        });
    }

    public void removeOldSyncs(ImapFolder imapFolder, Date minUpdateDate) {
        transaction.executeWithoutResult(transactionStatus -> {
            entityManager.createQuery("delete from imap_MessageSync ms where ms.folder.id = :folderId and ms.updateTs < :minUpdateDate")
                    .setParameter("folderId", imapFolder.getId())
                    .setParameter("minUpdateDate", minUpdateDate)
                    .executeUpdate();
        });
    }
}

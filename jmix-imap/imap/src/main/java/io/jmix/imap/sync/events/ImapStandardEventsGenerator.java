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

package io.jmix.imap.sync.events;

import io.jmix.core.TimeSource;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.imap.ImapProperties;
import io.jmix.imap.data.ImapMessageSyncDataProvider;
import io.jmix.imap.entity.*;
import io.jmix.imap.events.*;
import io.jmix.imap.flags.ImapFlag;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.mail.Flags;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.*;
import java.util.stream.Collectors;

@Component("imap_StandardEventsGenerator")
public class ImapStandardEventsGenerator extends ImapEventsBatchedGenerator {

    private final static Logger log = LoggerFactory.getLogger(ImapStandardEventsGenerator.class);

    @Autowired
    protected ImapMessageSyncDataProvider imapMessageSyncDataProvider;

    @Autowired
    protected SystemAuthenticator authentication;

    @PersistenceContext
    protected EntityManager entityManager;

    protected TransactionTemplate transaction;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected ImapProperties imapProperties;

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public void init(ImapMailBox imapMailBox) {

    }

    @Override
    public void shutdown(ImapMailBox imapMailBox) {

    }

    @Override
    protected int getBatchSize() {
        return imapProperties.getEventsBatchSize();
    }

    @Override
    public Collection<? extends BaseImapEvent> generateForNewMessages(ImapFolder folder, int batchSize) {
        authentication.begin();
        try {
            Collection<ImapMessage> newMessages = imapMessageSyncDataProvider.findMessagesWithSyncStatus(folder, ImapSyncStatus.ADDED, batchSize);

            Collection<BaseImapEvent> newMessageEvents = newMessages.stream()
                    .map(NewEmailImapEvent::new)
                    .collect(Collectors.toList());

            imapMessageSyncDataProvider.removeMessagesSyncs(newMessages.stream().map(ImapMessage::getId).collect(Collectors.toList()));

            return newMessageEvents;
        } catch (Exception e) {
            log.error("New messages events for " + folder.getName() + " failure", e);
            return Collections.emptyList();
        } finally {
            authentication.end();
        }
    }

    @Override
    public Collection<? extends BaseImapEvent> generateForChangedMessages(ImapFolder folder, int batchSize) {
        authentication.begin();
        int i = 0;
        try {
            Collection<BaseImapEvent> updateMessageEvents = new ArrayList<>(batchSize);
            while (i < batchSize) {
                Collection<ImapMessageSync> remainMessageSyncs = imapMessageSyncDataProvider.findMessagesSyncs(folder, ImapSyncStatus.REMAIN, batchSize);

                if (CollectionUtils.isEmpty(remainMessageSyncs)) {
                    break;
                }

                for (ImapMessageSync messageSync : remainMessageSyncs) {
                    List<BaseImapEvent> events = generateUpdateEvents(messageSync);
                    if (CollectionUtils.isNotEmpty(events)) {
                        updateMessageEvents.addAll(events);
                        i++;
                    }
                }

                imapMessageSyncDataProvider.removeMessagesSyncs(remainMessageSyncs.stream()
                        .map(ms -> ms.getMessage().getId())
                        .distinct()
                        .collect(Collectors.toList()));
            }

            return updateMessageEvents;
        } catch (Exception e) {
            log.error("Changed messages events for " + folder.getName() + " failure", e);
            return Collections.emptyList();
        } finally {
            authentication.end();
        }

    }

    protected List<BaseImapEvent> generateUpdateEvents(ImapMessageSync messageSync) {
        Flags newFlags = messageSync.getImapFlags();
        ImapMessage msg = messageSync.getMessage();
        Flags oldFlags = msg.getImapFlags();

        List<BaseImapEvent> modificationEvents = new ArrayList<>(3);
        if (!Objects.equals(newFlags, oldFlags)) {
            log.trace("Update message {}. Old flags: {}, new flags: {}", msg, oldFlags, newFlags);

            HashMap<ImapFlag, Boolean> changedFlagsWithNewValue = new HashMap<>();
            if (isSeen(newFlags, oldFlags)) {
                modificationEvents.add(new EmailSeenImapEvent(msg));
            }

            if (isAnswered(newFlags, oldFlags)) {
                modificationEvents.add(new EmailAnsweredImapEvent(msg));
            }

            for (String userFlag : oldFlags.getUserFlags()) {
                if (!newFlags.contains(userFlag)) {
                    changedFlagsWithNewValue.put(new ImapFlag(userFlag), false);
                }
            }

            for (Flags.Flag systemFlag : oldFlags.getSystemFlags()) {
                if (!newFlags.contains(systemFlag)) {
                    changedFlagsWithNewValue.put(new ImapFlag(ImapFlag.SystemFlag.valueOf(systemFlag)), false);
                }
            }

            for (String userFlag : newFlags.getUserFlags()) {
                if (!oldFlags.contains(userFlag)) {
                    changedFlagsWithNewValue.put(new ImapFlag(userFlag), true);
                }
            }

            for (Flags.Flag systemFlag : newFlags.getSystemFlags()) {
                if (!oldFlags.contains(systemFlag)) {
                    changedFlagsWithNewValue.put(new ImapFlag(ImapFlag.SystemFlag.valueOf(systemFlag)), true);
                }
            }

            modificationEvents.add(new EmailFlagChangedImapEvent(msg, changedFlagsWithNewValue));
            msg.setImapFlags(newFlags);
            msg.setUpdateTs(timeSource.currentTimestamp());
            authentication.begin();
            try {
                transaction.executeWithoutResult(transactionStatus -> entityManager.merge(msg));
            } finally {
                authentication.end();
            }

        }
        return modificationEvents;
    }

    protected boolean isSeen(Flags newFlags, Flags oldFlags) {
        return !oldFlags.contains(Flags.Flag.SEEN)
                && newFlags.contains(Flags.Flag.SEEN);
    }

    protected boolean isAnswered(Flags newFlags, Flags oldFlags) {
        return !oldFlags.contains(Flags.Flag.ANSWERED)
                && newFlags.contains(Flags.Flag.ANSWERED);
    }

    @Override
    @Transactional
    public Collection<? extends BaseImapEvent> generateForMissedMessages(ImapFolder jmixFolder, int batchSize) {
        authentication.begin();
        try {
            return transaction.execute(transactionStatus -> {
                Collection<ImapMessage> removed = imapMessageSyncDataProvider.findMessagesWithSyncStatus(
                        jmixFolder, ImapSyncStatus.REMOVED, batchSize);
                Collection<ImapMessageSync> moved = imapMessageSyncDataProvider.findMessagesSyncs(
                        jmixFolder, ImapSyncStatus.MOVED, batchSize);

                Collection<BaseImapEvent> missedMessageEvents = new ArrayList<>(removed.size() + moved.size());
                List<Integer> missedMessageNums = new ArrayList<>(removed.size() + moved.size());
                removed.forEach(imapMessage -> {
                    missedMessageEvents.add(new EmailDeletedImapEvent(imapMessage));
                    missedMessageNums.add(imapMessage.getMsgNum());
                    entityManager.remove(imapMessage);
                });

                moved.forEach(imapMessageSync -> {
                    ImapMessage message = imapMessageSync.getMessage();
                    ImapFolder oldFolder = imapMessageSync.getOldFolder();

                    imapMessageSync.setStatus(ImapSyncStatus.REMAIN);
                    entityManager.merge(imapMessageSync);
                    missedMessageEvents.add(new EmailMovedImapEvent(message, oldFolder));
                    missedMessageNums.add(message.getMsgNum());
                    recalculateMessageNumbers(jmixFolder, missedMessageNums);
                });


                return missedMessageEvents;
            });

        } catch (Exception e) {
            log.error("Missed messages events for " + jmixFolder.getName() + " failure", e);
            return Collections.emptyList();
        } finally {
            authentication.end();
        }
    }

    protected void recalculateMessageNumbers(ImapFolder jmixFolder, List<Integer> messageNumbers) {
        messageNumbers.sort(Comparator.naturalOrder());
        authentication.begin();
        try  {
            transaction.executeWithoutResult(transactionStatus -> {
                for (int i = 0; i < messageNumbers.size(); i++) {
                    String queryString = "update imap_Message m set m.msgNum = m.msgNum-" + (i + 1) +
                            " where m.folder.id = :mailFolderId and m.msgNum > :msgNum";
                    if (i < messageNumbers.size() - 1) {
                        queryString += " and m.msgNum < :topMsgNum";
                    }
                    Query query = entityManager.createQuery(queryString)
                            .setParameter("mailFolderId", jmixFolder.getId())
                            .setParameter("msgNum", messageNumbers.get(i));
                    if (i < messageNumbers.size() - 1) {
                        query.setParameter("topMsgNum", messageNumbers.get(i + 1));
                    }
                    query.executeUpdate();
                }
            });
        } finally {
            authentication.end();
        }
    }
}

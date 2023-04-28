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

package io.jmix.imap.sync;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPMessage;
import com.sun.mail.imap.IMAPStore;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.imap.ImapProperties;
import io.jmix.imap.data.ImapDataProvider;
import io.jmix.imap.data.ImapMessageSyncDataProvider;
import io.jmix.imap.entity.*;
import io.jmix.imap.exception.ImapException;
import io.jmix.imap.flags.ImapFlag;
import io.jmix.imap.impl.ImapHelper;
import io.jmix.imap.impl.ImapOperations;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.search.FlagTerm;
import jakarta.mail.search.MessageIDTerm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.*;

@Component("imap_Synchronizer")
@Primary
public class ImapSynchronizer {

    private final static Logger log = LoggerFactory.getLogger(ImapSynchronizer.class);

    @Autowired
    protected ImapHelper imapHelper;

    @Autowired
    protected ImapOperations imapOperations;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ImapProperties imapProperties;

    @Autowired
    protected SystemAuthenticator authenticator;

    @Autowired
    protected ImapDataProvider imapDataProvider;

    @Autowired
    protected ImapMessageSyncDataProvider imapMessageSyncDataProvider;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected TimeSource timeSource;

    protected TransactionTemplate transaction;

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void synchronize(ImapMailBox imapMailBox) {
        authenticator.begin();
        try {
            log.trace("Start synchronization of mailbox (id={})", imapMailBox.getId());
            ImapMailBox mailBox = imapDataProvider.findMailBox(imapMailBox.getId());

            if (mailBox == null) {
                log.trace("Mailbox not found by id={}", imapMailBox.getId());
                return;
            }

            IMAPStore store = imapHelper.getStore(mailBox);
            try {
                List<ImapMessage> checkAnswers = new ArrayList<>();
                List<ImapMessage> missedMessages = new ArrayList<>();

                for (ImapFolder jmixFolder : mailBox.getProcessableFolders()) {
                    IMAPFolder imapFolder = null;
                    try {
                        log.trace("Synchronize folder '{}' of mailbox '{}'", jmixFolder.getName(), mailBox);
                        Date tenMinutesAgo = DateUtils.addMinutes(timeSource.currentTimestamp(), -10);
                        imapMessageSyncDataProvider.removeOldSyncs(jmixFolder, tenMinutesAgo);

                        imapFolder = (IMAPFolder) store.getFolder(jmixFolder.getName());
                        imapFolder.open(Folder.READ_WRITE);

                        //existing
                        handleExistingMessages(checkAnswers, missedMessages, jmixFolder, imapFolder);

                        //new
                        handleNewMessages(checkAnswers, missedMessages, jmixFolder, imapFolder);
                    } catch (MessagingException e) {
                        log.warn("synchronization of folder " + jmixFolder.getName() + " of mailbox " + mailBox + " failed", e);
                    } finally {
                        close(mailBox, imapFolder);

                    }
                }

                // answers
                setAnswersFlag(mailBox, store, checkAnswers);

                // missed
                handleMissedMessages(mailBox, store, missedMessages);

            } finally {
                store.close();
            }
        } catch (MessagingException e) {
            throw new ImapException(String.format("synchronization of mailBox#%s failed", imapMailBox.getId()), e);
        } catch (Exception e) {
            log.error("Synchronization failed", e);
        } finally {
            authenticator.end();
        }
    }

    protected void handleExistingMessages(List<ImapMessage> checkAnswers,
                                        List<ImapMessage> missedMessages,
                                        ImapFolder folder,
                                        IMAPFolder imapFolder) throws MessagingException {

        log.trace("Handle existing messages for folder '{}'", folder.getName());
        Date tenMinutesAgo = DateUtils.addMinutes(timeSource.currentTimestamp(), -10);
        Date threeMinutesAgo = DateUtils.addMinutes(tenMinutesAgo, 7);

        Collection<ImapMessage> messagesForSync = new ArrayList<>(imapMessageSyncDataProvider.findMessagesForSync(folder));
        log.trace("Found {} messages for synchronization", messagesForSync.size());

        imapMessageSyncDataProvider.createSyncForMessages(messagesForSync, ImapSyncStatus.IN_SYNC);

        Collection<ImapMessage> oldInSync = imapMessageSyncDataProvider.findMessagesWithSyncStatus(
                folder, ImapSyncStatus.IN_SYNC, tenMinutesAgo, threeMinutesAgo);
        messagesForSync.addAll(oldInSync);

        for (ImapMessage message : messagesForSync) {
            Message imapMessage = imapFolder.getMessageByUID(message.getMsgUid());
            if (imapMessage != null) {
                imapMessageSyncDataProvider.updateSyncStatus(message,
                        ImapSyncStatus.REMAIN, ImapSyncStatus.IN_SYNC,
                        imapMessage.getFlags(), null);
                if (message.getReferenceId() != null) {
                    checkAnswers.add(message);
                }
            } else {
                missedMessages.add(message);
                imapMessageSyncDataProvider.updateSyncStatus(
                        message,
                        ImapSyncStatus.MISSED, ImapSyncStatus.IN_SYNC,
                        null, null);
            }
        }


        Collection<ImapMessage> missed = new ArrayList<>(imapMessageSyncDataProvider.findMessagesWithSyncStatus(folder, ImapSyncStatus.MISSED,
                tenMinutesAgo, threeMinutesAgo));
        missedMessages.addAll(missed);
    }

    protected void handleNewMessages(List<ImapMessage> checkAnswers,
                                     List<ImapMessage> missedMessages,
                                     ImapFolder jmixFolder,
                                     IMAPFolder imapFolder) throws MessagingException {
        log.trace("Handle new messages for folder '{}'", jmixFolder.getName());
        ImapMailBox mailBox = jmixFolder.getMailBox();
        List<IMAPMessage> imapMessages = imapOperations.search(
                imapFolder,
                new FlagTerm(imapHelper.jmixFlags(mailBox), false),
                mailBox
        );
        if (CollectionUtils.isNotEmpty(imapMessages)) {
            for (IMAPMessage imapMessage : imapMessages) {
                if (Boolean.TRUE.equals(imapProperties.isClearCustomFlags())) {
                    log.trace("[{}]clear custom flags for message with uid {}",
                            jmixFolder, imapFolder.getUID(imapMessage));
                    unsetCustomFlags(imapMessage);
                }
                imapMessage.setFlags(imapHelper.jmixFlags(mailBox), true);
                log.debug("[{}]insert message with uid {} to db after changing flags on server",
                        jmixFolder, imapFolder.getUID(imapMessage));
                ImapMessage jmixMessage = createMessage(imapMessage, jmixFolder);
                if (jmixMessage != null && jmixMessage.getReferenceId() != null) {
                    checkAnswers.add(jmixMessage);
                }
            }
        }
    }

    protected void handleMissedMessages(ImapMailBox mailBox, IMAPStore store, List<ImapMessage> missedMessages) throws MessagingException {
        List<ImapMessage> foundMessages = new ArrayList<>();
        for (ImapFolder jmixFolder : mailBox.getProcessableFolders()) {
            log.trace("Handle missed messages for folder '{}'", jmixFolder.getName());
            IMAPFolder imapFolder = null;
            try {
                imapFolder = (IMAPFolder) store.getFolder(jmixFolder.getName());
                imapFolder.open(Folder.READ_ONLY);

                for (ImapMessage jmixMessage : missedMessages) {
                    if (foundMessages.contains(jmixMessage)) {
                        continue;
                    }

                    if (jmixMessage.getMessageId() == null) {
                        foundMessages.add(jmixMessage);
                        imapMessageSyncDataProvider.updateSyncStatus(jmixMessage,
                                ImapSyncStatus.REMOVED, ImapSyncStatus.MISSED,
                                null, null);
                        continue;
                    }

                    List<IMAPMessage> imapMessages = imapOperations.searchMessageIds(
                            imapFolder,
                            new MessageIDTerm(jmixMessage.getMessageId())
                    );
                    if (CollectionUtils.isNotEmpty(imapMessages)) {
                        foundMessages.add(jmixMessage);
                        ImapFolder oldFolder = jmixMessage.getFolder();
                        updateJmixMessage(jmixMessage, imapMessages.get(0), jmixFolder);
                        if (StringUtils.equals(jmixFolder.getName(), mailBox.getTrashFolderName())) {
                            imapMessageSyncDataProvider.updateSyncStatus(jmixMessage,
                                    ImapSyncStatus.REMOVED, ImapSyncStatus.MISSED,
                                    null, null);
                        } else {
                            imapMessageSyncDataProvider.updateSyncStatus(jmixMessage,
                                    ImapSyncStatus.MOVED, ImapSyncStatus.MISSED,
                                    null, oldFolder);
                        }
                    }
                }

            } finally {
                close(mailBox, imapFolder);
            }
        }
        missedMessages.removeAll(foundMessages);
        missedMessages.forEach(jmixMessage -> imapMessageSyncDataProvider.updateSyncStatus(jmixMessage, ImapSyncStatus.REMOVED, ImapSyncStatus.MISSED,
                null, null));
    }

    private void updateJmixMessage(ImapMessage jmixMessage, IMAPMessage imapMessage, ImapFolder jmixFolder) {
        transaction.executeWithoutResult(transactionStatus -> {
            ImapMessage found = entityManager.find(ImapMessage.class, jmixMessage.getId());
            try {
                found = imapOperations.map(found, imapMessage, jmixFolder);
            } catch (MessagingException e) {
                throw new ImapException(e);
            }
            entityManager.merge(found);
        });
    }

    protected void setAnswersFlag(ImapMailBox mailBox, IMAPStore store, List<ImapMessage> checkAnswers) throws MessagingException {
        Map<String, List<ImapMessage>> folderWithMessagesToAnswer = new HashMap<>();
        checkAnswers.forEach(jmixMessage -> {
            ImapMessage parentMessage = imapDataProvider.findMessageByImapMessageId(jmixMessage);

            if (parentMessage != null && !parentMessage.getImapFlags().contains(ImapFlag.ANSWERED.imapFlags())) {
                String folderName = parentMessage.getFolder().getName();
                if (folderWithMessagesToAnswer.containsKey(folderName)) {
                    folderWithMessagesToAnswer.get(folderName).add(parentMessage);
                } else {
                    ArrayList<ImapMessage> messages = new ArrayList<>();
                    messages.add(parentMessage);
                    folderWithMessagesToAnswer.putIfAbsent(folderName, messages);
                }
            }
        });

        for (Map.Entry<String, List<ImapMessage>> folderWithMessages : folderWithMessagesToAnswer.entrySet()) {
            IMAPFolder imapFolder = null;
            try {
                imapFolder = (IMAPFolder) store.getFolder(folderWithMessages.getKey());
                imapFolder.open(Folder.READ_WRITE);

                for (ImapMessage msg : folderWithMessages.getValue()) {
                    Message imapMessage = imapFolder.getMessageByUID(msg.getMsgUid());
                    imapMessage.setFlag(Flags.Flag.ANSWERED, true);

                    ImapMessageSync sync = imapMessageSyncDataProvider.findSync(msg);
                    if (sync != null && sync.getStatus() == ImapSyncStatus.REMAIN) {
                        Flags imapFlags = sync.getImapFlags();
                        imapFlags.add(Flags.Flag.ANSWERED);
                        sync.setImapFlags(imapFlags);
                        imapMessageSyncDataProvider.saveSync(sync);
                    } else {
                        Flags imapFlags = msg.getImapFlags();
                        imapFlags.add(Flags.Flag.ANSWERED);
                        msg.setImapFlags(imapFlags);

                        imapDataProvider.saveMessage(msg);
                    }
                }
            } finally {
                close(mailBox, imapFolder);
            }
        }
    }

    protected void close(ImapMailBox mailBox, IMAPFolder imapFolder) {
        if (imapFolder != null) {
            try {
                imapFolder.close(false);
            } catch (MessagingException | IllegalStateException e) {
                log.warn("can't close folder " + imapFolder.getFullName() + " of mailbox " + mailBox, e);
            }
        }
    }

    protected ImapMessage createMessage(IMAPMessage msg,
                                        ImapFolder jmixFolder) throws MessagingException {

        long uid = ((IMAPFolder) msg.getFolder()).getUID(msg);
        return transaction.execute(status -> {
            int sameUIDs = entityManager.createQuery(
                    "select m from imap_Message m where m.msgUid = :uid and m.folder.id = :mailFolderId")
                    .setParameter("uid", uid)
                    .setParameter("mailFolderId", jmixFolder.getId())
                    .setMaxResults(1)
                    .getResultList()
                    .size();
            if (sameUIDs == 0) {
                log.trace("Save new message {}", msg);
                ImapMessage entity = metadata.create(ImapMessage.class);
                try {
                    imapOperations.map(entity, msg, jmixFolder);
                } catch (MessagingException e) {
                    throw new ImapException(e);
                }
                entityManager.persist(entity);

                ImapMessageSync messageSync = metadata.create(ImapMessageSync.class);
                messageSync.setMessage(entity);
                messageSync.setStatus(ImapSyncStatus.ADDED);
                messageSync.setFolder(jmixFolder);
                entityManager.persist(messageSync);

                return entity;
            }

            return null;
        });
    }

    protected void unsetCustomFlags(Message msg) throws MessagingException {
        Flags flags = new Flags();
        String[] userFlags = msg.getFlags().getUserFlags();
        for (String flag : userFlags) {
            flags.add(flag);
        }
        if (userFlags.length > 0) {
            msg.setFlags(flags, false);
        }
    }

}

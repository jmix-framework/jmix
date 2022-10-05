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

package io.jmix.email.impl;

import io.jmix.core.*;
import io.jmix.data.PersistenceHints;
import io.jmix.data.impl.EntityEventManager;
import io.jmix.email.EmailDataProvider;
import io.jmix.email.EmailerProperties;
import io.jmix.email.SendingStatus;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Component("email_EmailDataProvider")
public class EmailDataProviderImpl implements EmailDataProvider {

    protected static final String BODY_FILE_EXTENSION = "txt";

    private static final Logger log = LoggerFactory.getLogger(EmailDataProviderImpl.class);

    @Autowired
    protected EmailerProperties emailerProperties;

    @Autowired
    protected TimeSource timeSource;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    protected TransactionTemplate transaction;

    protected FileStorage fileStorage;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    @Autowired
    protected EntityEventManager entityEventManager;

    @Autowired
    protected void setTransactionManager(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Override
    public List<SendingMessage> loadEmailsToSend() {
        Date sendTimeoutTime = DateUtils.addSeconds(timeSource.currentTimestamp(), -emailerProperties.getSendingTimeoutSec());

        List<SendingMessage> emailsToSend = new ArrayList<>();
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(SendingMessage.class, "sendingMessage.loadFromQueue");
        transaction.executeWithoutResult(status -> {
            List<SendingMessage> resList = entityManager.createQuery(
                    "select sm from email_SendingMessage sm" +
                            " where sm.status = :statusQueue or (sm.status = :statusSending and sm.updateTs < :time)" +
                            " order by sm.createTs",
                    SendingMessage.class)
                    .setParameter("statusQueue", SendingStatus.QUEUE.getId())
                    .setParameter("time", sendTimeoutTime)
                    .setParameter("statusSending", SendingStatus.SENDING.getId())
                    .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                    .setMaxResults(emailerProperties.getMessageQueueCapacity())
                    .getResultList();

            resList.forEach(msg -> {
                    msg.setStatus(SendingStatus.SENDING);
                    emailsToSend.add(msg);
            });
        });

        emailsToSend.forEach(this::loadBodyAndAttachments);

        return emailsToSend;
    }

    @Override
    public void updateStatus(SendingMessage sendingMessage, SendingStatus status) {
        try {
            transaction.executeWithoutResult(transactionStatus -> {
                SendingMessage msg = entityManager.merge(sendingMessage);

                msg.setAttemptsMade(msg.getAttemptsMade() + 1);
                msg.setStatus(status);

                if(status == SendingStatus.QUEUE && shouldMarkNotSent(msg)) {
                    msg.setStatus(SendingStatus.NOT_SENT);
                }
                if (status == SendingStatus.SENT) {
                    msg.setDateSent(timeSource.currentTimestamp());
                }
                if (emailerProperties.isUseFileStorage()) {
                    msg.setContentText(null);
                }
            });
        } catch (Exception e) {
            log.error(buildErrorMessage(status), sendingMessage.getAddress(), e);
        }
    }

    @Override
    public String loadContentText(SendingMessage sendingMessage) {
        SendingMessage msg = dataManager.load(SendingMessage.class)
                .id(sendingMessage.getId())
                .fetchPlan("sendingMessage.loadContentText")
                .optional()
                .orElse(null);

        Objects.requireNonNull(msg, "Sending message not found: " + sendingMessage.getId());

        if (msg.getContentTextFile() != null) {
            byte[] bodyContent;
            try {
                bodyContent = IOUtils.toByteArray(getFileStorage().openStream(msg.getContentTextFile()));
            } catch (IOException e) {
                throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, "Unable to load file from file storage", e);
            }
            //noinspection UnnecessaryLocalVariable
            String res = bodyTextFromByteArray(bodyContent);
            return res;
        } else {
            return msg.getContentText();
        }
    }

    @Override
    public void persistMessage(SendingMessage sendingMessage, SendingStatus status) {
        MessagePersistingContext context = new MessagePersistingContext();
        try {
            transaction.executeWithoutResult(transactionStatus -> {
                if (sendingMessage.getAttemptsLimit() == null) {
                    sendingMessage.setAttemptsLimit(emailerProperties.getDefaultSendingAttemptsLimit());
                }
                sendingMessage.setStatus(status);
                persistSendingMessage(sendingMessage, context);
            });
            context.finished();
        } finally {
            removeOrphanFiles(context);
        }
    }

    @Override
    public void migrateEmailsToFileStorage(List<SendingMessage> messages) {
        transaction.executeWithoutResult(transactionStatus -> messages.forEach(this::migrateMessage));
    }

    @Override
    public void migrateAttachmentsToFileStorage(List<SendingAttachment> attachments) {
        transaction.executeWithoutResult(transactionStatus -> attachments.forEach(this::migrateAttachment));
    }

    protected String buildErrorMessage(SendingStatus status) {
        switch (status) {
            case SENT:
                return "Error marking message to '{}' as sent";
            case QUEUE:
                return "Error returning message to '{}' to the queue";
            case NOT_SENT:
                return "Error marking message to '{}' as not sent";
        }
        return "Error updating status of message for '{}'";
    }

    protected void migrateMessage(SendingMessage msg) {
        msg = entityManager.merge(msg);
        byte[] bodyBytes = bodyTextToBytes(msg);
        String fileName = getFileName(msg);
        FileRef contentTextFile = createContentFile(null, bodyBytes, fileName);
        msg.setContentTextFile(contentTextFile);
        msg.setContentText(null);
    }

    protected void migrateAttachment(SendingAttachment attachment) {
        attachment = entityManager.merge(attachment);
        FileRef contentFile = createContentFile(null, attachment.getContent(), attachment.getName());
        attachment.setContentFile(contentFile);
        attachment.setContent(null);
    }

    protected boolean shouldMarkNotSent(SendingMessage sendingMessage) {
        Date deadline = sendingMessage.getDeadline();
        if (deadline != null && deadline.before(timeSource.currentTimestamp())) {
            return true;
        }

        Integer messageAttemptsLimit = sendingMessage.getAttemptsLimit();
        int defaultLimit = emailerProperties.getDefaultSendingAttemptsLimit();
        int attemptsLimit = messageAttemptsLimit != null ? messageAttemptsLimit : defaultLimit;
        //noinspection UnnecessaryLocalVariable
        boolean res = sendingMessage.getAttemptsMade() != null && sendingMessage.getAttemptsMade() >= attemptsLimit;
        return res;
    }

    protected void loadBodyAndAttachments(SendingMessage message) {
        try {
            if (message.getContentTextFile() != null) {
                byte[] bodyContent = IOUtils.toByteArray(getFileStorage().openStream(message.getContentTextFile()));
                String body = bodyTextFromByteArray(bodyContent);
                message.setContentText(body);
            }

            for (SendingAttachment attachment : message.getAttachments()) {
                if (attachment.getContentFile() != null) {
                    byte[] content = IOUtils.toByteArray(getFileStorage().openStream(attachment.getContentFile()));
                    attachment.setContent(content);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load body or attachments for {}", message);
        }
    }

    protected void persistSendingMessage(SendingMessage message, MessagePersistingContext context) {
        boolean useFileStorage = emailerProperties.isUseFileStorage();

        if (useFileStorage) {
            byte[] bodyBytes = bodyTextToBytes(message);

            String fileName = getFileName(message);
            FileRef contentTextFile = createContentFile(context, bodyBytes, fileName);
            message.setContentTextFile(contentTextFile);
            message.setContentText(null);
        }

        entityEventManager.publishEntitySavingEvent(message, true);//workaround for jmix-framework/jmix#1069
        entityManager.persist(message);

        message.getAttachments().forEach(attachment -> {
            if (useFileStorage) {
                FileRef contentFile = createContentFile(context, attachment.getContent(), attachment.getName());
                attachment.setContentFile(contentFile);
                attachment.setContent(null);
            }

            entityEventManager.publishEntitySavingEvent(attachment, true);//workaround for jmix-framework/jmix#1069
            entityManager.persist(attachment);
        });
    }

    protected FileRef createContentFile(@Nullable MessagePersistingContext context, byte[] bodyBytes, String fileName) {
        FileRef contentTextFile = getFileStorage().saveStream(fileName, new ByteArrayInputStream(bodyBytes));
        if (context != null) {
            context.files.add(contentTextFile);
        }
        return contentTextFile;
    }

    protected String getFileName(SendingMessage msg) {
        return String.format("Email_%s.%s", msg.getId(), BODY_FILE_EXTENSION);
    }

    protected String bodyTextFromByteArray(byte[] bodyContent) {
        return new String(bodyContent, StandardCharsets.UTF_8);
    }

    protected byte[] bodyTextToBytes(SendingMessage message) {
        return message.getContentText().getBytes(StandardCharsets.UTF_8);
    }

    protected static class MessagePersistingContext {
        public final List<FileRef> files = new ArrayList<>();

        public void finished() {
            files.clear();
        }
    }

    protected void removeOrphanFiles(MessagePersistingContext context) {
        context.files.forEach(file -> {
            try {
                getFileStorage().removeFile(file);
            } catch (Exception e) {
                log.error("Failed to remove file {}", file);
            }
        });
    }

    protected FileStorage getFileStorage() {
        if (fileStorage == null) {
            fileStorage = fileStorageLocator.getDefault();
        }
        return fileStorage;
    }
}
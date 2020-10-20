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

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import io.jmix.core.*;
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

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
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

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected Persistence persistence;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    protected FileStorage<URI, String> fileStorage;

    @Autowired
    public void setFileStorage() {
        fileStorage = fileStorageLocator.getDefault();
    }

    @Override
    public List<SendingMessage> loadEmailsToSend() {
        Date sendTimeoutTime = DateUtils.addSeconds(timeSource.currentTimestamp(), -emailerProperties.getSendingTimeoutSec());

        List<SendingMessage> emailsToSend = new ArrayList<>();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<SendingMessage> query = em.createQuery(
                    "select sm from email_SendingMessage sm" +
                            " where sm.status = :statusQueue or (sm.status = :statusSending and sm.updateTs < :time)" +
                            " order by sm.createTs",
                    SendingMessage.class
            );
            query.setParameter("statusQueue", SendingStatus.QUEUE.getId());
            query.setParameter("time", sendTimeoutTime);
            query.setParameter("statusSending", SendingStatus.SENDING.getId());

            FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(SendingMessage.class, "sendingMessage.loadFromQueue");
            query.setView(fetchPlan);

            query.setMaxResults(emailerProperties.getMessageQueueCapacity());

            List<SendingMessage> resList = query.getResultList();

            resList.forEach(msg -> {
                if (shouldMarkNotSent(msg)) {
                    msg.setStatus(SendingStatus.NOTSENT);
                } else {
                    msg.setStatus(SendingStatus.SENDING);
                    emailsToSend.add(msg);
                }
            });
            tx.commit();
        }

        emailsToSend.forEach(this::loadBodyAndAttachments);

        return emailsToSend;
    }

    @Override
    public void updateStatus(SendingMessage sendingMessage, SendingStatus status) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            SendingMessage msg = em.merge(sendingMessage);

            msg.setStatus(status);
            msg.setAttemptsMade(msg.getAttemptsMade() + 1);
            if (status == SendingStatus.SENT) {
                msg.setDateSent(timeSource.currentTimestamp());
            }
            if (emailerProperties.isFileStorageUsed()) {
                msg.setContentText(null);
            }

            tx.commit();
        } catch (Exception e) {
            log.error(buildErrorMessage(status), sendingMessage.getAddress(), e);
        }
    }

    @Override
    public String loadContentText(SendingMessage sendingMessage) {
        SendingMessage msg;
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            msg = em.reload(sendingMessage, "sendingMessage.loadContentText");
            tx.commit();
        }
        Objects.requireNonNull(msg, "Sending message not found: " + sendingMessage.getId());

        if (msg.getContentTextFile() != null) {
            byte[] bodyContent;
            try {
                bodyContent = IOUtils.toByteArray(fileStorage.openStream(msg.getContentTextFile()));
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
    public void persistMessages(List<SendingMessage> sendingMessageList, SendingStatus status) {
        MessagePersistingContext context = new MessagePersistingContext();
        try {
            try (Transaction tx = persistence.createTransaction()) {
                EntityManager em = persistence.getEntityManager();
                sendingMessageList.forEach(message -> {
                    message.setStatus(status);
                    persistSendingMessage(em, message, context);
                });
                tx.commit();
            }
            context.finished();
        } finally {
            removeOrphanFiles(context);
        }
    }

    @Override
    public void migrateEmailsToFileStorage(List<SendingMessage> messages) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            messages.forEach(msg -> migrateMessage(em, msg));
            tx.commit();
        }
    }

    @Override
    public void migrateAttachmentsToFileStorage(List<SendingAttachment> attachments) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            attachments.forEach(attachment -> migrateAttachment(em, attachment));
            tx.commit();
        }
    }

    protected String buildErrorMessage(SendingStatus status) {
        switch (status) {
            case SENT:
                return "Error marking message to '{}' as sent";
            case QUEUE:
                return "Error returning message to '{}' to the queue";
            case NOTSENT:
                return "Error marking message to '{}' as not sent";
        }
        return "Error updating status of message for '{}'";
    }

    protected void migrateMessage(EntityManager em, SendingMessage msg) {
        msg = em.merge(msg);
        byte[] bodyBytes = bodyTextToBytes(msg);
        String fileName = getFileName(msg);
        URI contentTextFile = createContentFile(null, bodyBytes, fileName);
        msg.setContentTextFile(contentTextFile);
        msg.setContentText(null);
    }

    protected void migrateAttachment(EntityManager em, SendingAttachment attachment) {
        attachment = em.merge(attachment);
        URI contentFile = createContentFile(null, attachment.getContent(), attachment.getName());
        attachment.setContentFile(contentFile);
        attachment.setContent(null);
    }

    protected boolean shouldMarkNotSent(SendingMessage sendingMessage) {
        Date deadline = sendingMessage.getDeadline();
        if (deadline != null && deadline.before(timeSource.currentTimestamp())) {
            return true;
        }

        Integer messageAttemptsLimit = sendingMessage.getAttemptsCount();
        int defaultLimit = emailerProperties.getDefaultSendingAttemptsCount();
        int attemptsLimit = messageAttemptsLimit != null ? messageAttemptsLimit : defaultLimit;
        //noinspection UnnecessaryLocalVariable
        boolean res = sendingMessage.getAttemptsMade() != null && sendingMessage.getAttemptsMade() >= attemptsLimit;
        return res;
    }

    protected void loadBodyAndAttachments(SendingMessage message) {
        try {
            if (message.getContentTextFile() != null) {
                byte[] bodyContent = IOUtils.toByteArray(fileStorage.openStream(message.getContentTextFile()));
                String body = bodyTextFromByteArray(bodyContent);
                message.setContentText(body);
            }

            for (SendingAttachment attachment : message.getAttachments()) {
                if (attachment.getContentFile() != null) {
                    byte[] content = IOUtils.toByteArray(fileStorage.openStream(attachment.getContentFile()));
                    attachment.setContent(content);
                }
            }
        } catch (IOException e) {
            log.error("Failed to load body or attachments for {}", message);
        }
    }

    protected void persistSendingMessage(EntityManager em, SendingMessage message, MessagePersistingContext context) {
        boolean useFileStorage = emailerProperties.isFileStorageUsed();

        if (useFileStorage) {
            byte[] bodyBytes = bodyTextToBytes(message);

            String fileName = getFileName(message);
            URI contentTextFile = createContentFile(context, bodyBytes, fileName);
            message.setContentTextFile(contentTextFile);
            message.setContentText(null);
        }

        em.persist(message);

        message.getAttachments().forEach(attachment -> {
            if (useFileStorage) {
                URI contentFile = createContentFile(context, attachment.getContent(), attachment.getName());
                attachment.setContentFile(contentFile);
                attachment.setContent(null);
            }

            em.persist(attachment);
        });
    }

    protected URI createContentFile(@Nullable MessagePersistingContext context, byte[] bodyBytes, String fileName) {
        URI contentTextFile = fileStorage.createReference(fileName);
        fileStorage.saveStream(contentTextFile, new ByteArrayInputStream(bodyBytes));
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
        public final List<URI> files = new ArrayList<>();

        public void finished() {
            files.clear();
        }
    }

    protected void removeOrphanFiles(MessagePersistingContext context) {
        context.files.forEach(file -> {
            try {
                fileStorage.removeFile(file);
            } catch (Exception e) {
                log.error("Failed to remove file {}", file);
            }
        });
    }
}
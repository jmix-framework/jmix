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

import com.google.common.base.Strings;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.TemplateHelper;
import com.haulmont.cuba.core.sys.AppContext;
import com.sun.mail.smtp.SMTPAddressFailedException;
import io.jmix.core.*;
import io.jmix.core.security.Authenticator;
import io.jmix.email.*;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

@Component(Emailer.NAME)
public class EmailerImpl implements Emailer {

    protected static final String BODY_FILE_EXTENSION = "txt";

    private static final Logger log = LoggerFactory.getLogger(EmailerImpl.class);

    @Autowired
    protected EmailerProperties emailerProperties;

    protected AtomicInteger callCount = new AtomicInteger(0);

    @Resource(name = "mailSendTaskExecutor")
    protected TaskExecutor mailSendTaskExecutor;

    @Autowired
    protected TimeSource timeSource;

    @Autowired
    protected Persistence persistence;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected FetchPlanRepository repository;

    @Autowired
    protected Authenticator authenticator;

    @Autowired
    protected EmailSender emailSender;

    @Autowired
    protected Resources resources;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected FileStorageLocator fileStorageLocator;

    protected FileStorage<URI, String> fileStorage;

    @Autowired
    public void setFileStorage() {
        fileStorage = fileStorageLocator.getDefault();
    }

    @Override
    public void sendEmail(String address, String caption, String body, String bodyContentType,
                          EmailAttachment... attachment) throws EmailException {
        EmailInfo emailInfo = EmailInfoBuilder.create(address, caption, body)
                .setBodyContentType(bodyContentType)
                .setAttachments(attachment)
                .build();
        sendEmail(emailInfo);
    }

    @Override
    public void sendEmail(EmailInfo info) throws EmailException {
        prepareEmailInfo(info);
        persistAndSendEmail(info);
    }

    @Override
    public List<SendingMessage> sendEmailAsync(EmailInfo info) {
        //noinspection UnnecessaryLocalVariable
        List<SendingMessage> result = sendEmailAsync(info, null, null);
        return result;
    }

    @Override
    public List<SendingMessage> sendEmailAsync(EmailInfo info, Integer attemptsCount, Date deadline) {
        prepareEmailInfo(info);
        List<SendingMessage> messages = splitEmail(info, attemptsCount, deadline);
        persistMessages(messages, SendingStatus.QUEUE);
        return messages;
    }

    protected void prepareEmailInfo(EmailInfo emailInfo) {
        processBodyTemplate(emailInfo);

        if (emailInfo.getFrom() == null) {
            String defaultFromAddress = emailerProperties.getFromAddress();
            if (defaultFromAddress == null) {
                throw new IllegalStateException("jmix.email.fromAddress not set in the system");
            }
            emailInfo.setFrom(defaultFromAddress);
        }
    }

    protected void processBodyTemplate(EmailInfo info) {
        String templatePath = info.getTemplatePath();
        if (templatePath == null) {
            return;
        }

        Map<String, Serializable> params = info.getTemplateParameters() == null
                ? Collections.emptyMap()
                : info.getTemplateParameters();
        String templateContents = resources.getResourceAsString(templatePath);
        if (templateContents == null) {
            throw new IllegalArgumentException("Could not find template by path: " + templatePath);
        }
        //todo: template helper is not implemented yet
        String body = TemplateHelper.processTemplate(templateContents, params);
        info.setBody(body);
    }

    protected List<SendingMessage> splitEmail(EmailInfo info, @Nullable Integer attemptsCount, @Nullable Date deadline) {
        List<SendingMessage> sendingMessageList = new ArrayList<>();
        if (info.isSendInOneMessage()) {
            if (StringUtils.isNotBlank(info.getAddresses())) {
                SendingMessage sendingMessage = convertToSendingMessage(info.getAddresses(), info.getFrom(), info.getCc(),
                        info.getBcc(), info.getCaption(), info.getBody(), info.getBodyContentType(), info.getHeaders(),
                        info.getAttachments(), attemptsCount, deadline);

                sendingMessageList.add(sendingMessage);
            }
        } else {
            String[] splitAddresses = info.getAddresses().split("[,;]");
            for (String address : splitAddresses) {
                address = address.trim();
                if (StringUtils.isNotBlank(address)) {
                    SendingMessage sendingMessage = convertToSendingMessage(address, info.getFrom(), null,
                            null, info.getCaption(), info.getBody(), info.getBodyContentType(), info.getHeaders(),
                            info.getAttachments(), attemptsCount, deadline);

                    sendingMessageList.add(sendingMessage);
                }
            }
        }
        return sendingMessageList;
    }

    protected void sendSendingMessage(SendingMessage sendingMessage) {
        Objects.requireNonNull(sendingMessage, "sendingMessage is null");
        Objects.requireNonNull(sendingMessage.getAddress(), "sendingMessage.address is null");
        Objects.requireNonNull(sendingMessage.getCaption(), "sendingMessage.caption is null");
        Objects.requireNonNull(sendingMessage.getContentText(), "sendingMessage.contentText is null");
        Objects.requireNonNull(sendingMessage.getFrom(), "sendingMessage.from is null");
        try {
            emailSender.sendEmail(sendingMessage);
            markAsSent(sendingMessage);
        } catch (Exception e) {
            log.warn("Unable to send email to '" + sendingMessage.getAddress() + "'", e);
            if (isNeedToRetry(e)) {
                returnToQueue(sendingMessage);
            } else {
                markAsNonSent(sendingMessage);
            }
        }
    }

    protected void persistAndSendEmail(EmailInfo emailInfo) throws EmailException {
        Objects.requireNonNull(emailInfo.getAddresses(), "addresses are null");
        Objects.requireNonNull(emailInfo.getCaption(), "caption is null");
        Objects.requireNonNull(emailInfo.getBody(), "body is null");
        Objects.requireNonNull(emailInfo.getFrom(), "from is null");

        List<SendingMessage> messages = splitEmail(emailInfo, null, null);

        List<String> failedAddresses = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (SendingMessage sendingMessage : messages) {
            SendingMessage persistedMessage = persistMessageIfPossible(sendingMessage);

            try {
                emailSender.sendEmail(sendingMessage);
                if (persistedMessage != null) {
                    markAsSent(persistedMessage);
                }
            } catch (Exception e) {
                log.warn("Unable to send email to '" + sendingMessage.getAddress() + "'", e);
                failedAddresses.add(sendingMessage.getAddress());
                errorMessages.add(e.getMessage());
                if (persistedMessage != null) {
                    markAsNonSent(persistedMessage);
                }
            }
        }

        if (!failedAddresses.isEmpty()) {
            throw new EmailException(failedAddresses, errorMessages);
        }
    }

    /*
     * Try to persist message and catch all errors to allow actual delivery
     * in case of database or file storage failure.
     */
    @Nullable
    protected SendingMessage persistMessageIfPossible(SendingMessage sendingMessage) {
        // A copy of sendingMessage is created
        // to avoid additional overhead to load body and attachments back from FS
        try {
            SendingMessage clonedMessage = createClone(sendingMessage);
            persistMessages(Collections.singletonList(clonedMessage), SendingStatus.SENDING);
            return clonedMessage;
        } catch (Exception e) {
            log.error("Failed to persist message " + sendingMessage.getCaption(), e);
            return null;
        }
    }

    protected SendingMessage createClone(SendingMessage srcMessage) {
        SendingMessage clonedMessage = metadataTools.copy(srcMessage);
        List<SendingAttachment> clonedList = new ArrayList<>();
        for (SendingAttachment srcAttach : srcMessage.getAttachments()) {
            SendingAttachment clonedAttach = metadataTools.copy(srcAttach);
            clonedAttach.setMessage(null);
            clonedAttach.setMessage(clonedMessage);
            clonedList.add(clonedAttach);
        }
        clonedMessage.setAttachments(clonedList);
        return clonedMessage;
    }

    @Override
    public String processQueuedEmails() {
        if (applicationNotStartedYet()) {
            return null;
        }

        int callsToSkip = emailerProperties.getDelayCallCount();
        int count = callCount.getAndAdd(1);
        if (count < callsToSkip) {
            return null;
        }

        String resultMessage;
        try {
            authenticator.begin(getEmailerLogin());
            try {
                resultMessage = sendQueuedEmails();
            } finally {
                authenticator.end();
            }
        } catch (Throwable e) {
            log.error("Error", e);
            resultMessage = e.getMessage();
        }
        return resultMessage;
    }

    protected boolean applicationNotStartedYet() {
        return !AppContext.isStarted();
    }

    protected String sendQueuedEmails() {
        List<SendingMessage> messagesToSend = loadEmailsToSend();

        for (SendingMessage msg : messagesToSend) {
            submitExecutorTask(msg);
        }

        if (messagesToSend.isEmpty()) {
            return "";
        }

        return String.format("Processed %d emails", messagesToSend.size());
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

    protected void submitExecutorTask(SendingMessage msg) {
        try {
            Runnable mailSendTask = new EmailSendTask(msg, authenticator);
            mailSendTaskExecutor.execute(mailSendTask);
        } catch (RejectedExecutionException e) {
            returnToQueue(msg);
        } catch (Exception e) {
            log.error("Exception while sending email: ", e);
            if (isNeedToRetry(e)) {
                returnToQueue(msg);
            } else {
                markAsNonSent(msg);
            }
        }
    }

    protected List<SendingMessage> loadEmailsToSend() {
        Date sendTimeoutTime = DateUtils.addSeconds(timeSource.currentTimestamp(), -emailerProperties.getSendingTimeoutSec());

        List<SendingMessage> emailsToSend = new ArrayList<>();

        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<SendingMessage> query = em.createQuery(
                    "select sm from sys_SendingMessage sm" +
                            " where sm.status = :statusQueue or (sm.status = :statusSending and sm.updateTs < :time)" +
                            " order by sm.createTs",
                    SendingMessage.class
            );
            query.setParameter("statusQueue", SendingStatus.QUEUE.getId());
            query.setParameter("time", sendTimeoutTime);
            query.setParameter("statusSending", SendingStatus.SENDING.getId());

            FetchPlan fetchPlan = repository.getFetchPlan(SendingMessage.class, "sendingMessage.loadFromQueue");
            query.setView(fetchPlan);

            query.setMaxResults(emailerProperties.getMessageQueueCapacity());

            List<SendingMessage> resList = query.getResultList();

            for (SendingMessage msg : resList) {
                if (shouldMarkNotSent(msg)) {
                    msg.setStatus(SendingStatus.NOTSENT);
                } else {
                    msg.setStatus(SendingStatus.SENDING);
                    emailsToSend.add(msg);
                }
            }
            tx.commit();
        }

        for (SendingMessage message : emailsToSend) {
            loadBodyAndAttachments(message);
        }
        return emailsToSend;
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
    public void updateSession() {
        emailSender.updateSession();
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
            log.error("Failed to load body or attachments for " + message);
        }
    }

    protected void persistMessages(List<SendingMessage> sendingMessageList, SendingStatus status) {
        MessagePersistingContext context = new MessagePersistingContext();

        try {
            try (Transaction tx = persistence.createTransaction()) {
                EntityManager em = persistence.getEntityManager();
                for (SendingMessage message : sendingMessageList) {
                    message.setStatus(status);
                    persistSendingMessage(em, message, context);

                }
                tx.commit();
            }
            context.finished();
        } finally {
            removeOrphanFiles(context);
        }
    }

    protected void removeOrphanFiles(MessagePersistingContext context) {
        for (URI file : context.files) {
            try {
                fileStorage.removeFile(file);
            } catch (Exception e) {
                log.error("Failed to remove file " + file);
            }
        }
    }

    protected void persistSendingMessage(EntityManager em, SendingMessage message,
                                         MessagePersistingContext context) {
        boolean useFileStorage = emailerProperties.isFileStorageUsed();

        if (useFileStorage) {
            byte[] bodyBytes = bodyTextToBytes(message);

            String fileName = "Email_" + message.getId() + "." + BODY_FILE_EXTENSION;
            URI contentTextFile = fileStorage.createReference(fileName);

            fileStorage.saveStream(contentTextFile, new ByteArrayInputStream(bodyBytes));
            context.files.add(contentTextFile);
            message.setContentTextFile(contentTextFile);
            message.setContentText(null);
        }

        em.persist(message);

        for (SendingAttachment attachment : message.getAttachments()) {
            if (useFileStorage) {
                URI contentFile = fileStorage.createReference(attachment.getName());
                fileStorage.saveStream(contentFile, new ByteArrayInputStream(attachment.getContent()));
                context.files.add(contentFile);
                attachment.setContentFile(contentFile);
                attachment.setContent(null);
            }

            em.persist(attachment);
        }
    }

    protected void returnToQueue(SendingMessage sendingMessage) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            SendingMessage msg = em.merge(sendingMessage);

            msg.setAttemptsMade(msg.getAttemptsMade() + 1);
            msg.setStatus(SendingStatus.QUEUE);
            if (emailerProperties.isFileStorageUsed()) {
                msg.setContentText(null);
            }

            tx.commit();
        } catch (Exception e) {
            log.error("Error returning message to '{}' to the queue", sendingMessage.getAddress(), e);
        }
    }

    protected void markAsSent(SendingMessage sendingMessage) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            SendingMessage msg = em.merge(sendingMessage);

            msg.setStatus(SendingStatus.SENT);
            msg.setAttemptsMade(msg.getAttemptsMade() + 1);
            msg.setDateSent(timeSource.currentTimestamp());
            if (emailerProperties.isFileStorageUsed()) {
                msg.setContentText(null);
            }

            tx.commit();
        } catch (Exception e) {
            log.error("Error marking message to '{}' as sent", sendingMessage.getAddress(), e);
        }
    }

    protected void markAsNonSent(SendingMessage sendingMessage) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();
            SendingMessage msg = em.merge(sendingMessage);

            msg.setStatus(SendingStatus.NOTSENT);
            msg.setAttemptsMade(msg.getAttemptsMade() + 1);
            if (emailerProperties.isFileStorageUsed()) {
                msg.setContentText(null);
            }


            tx.commit();
        } catch (Exception e) {
            log.error("Error marking message to '{}' as not sent", sendingMessage.getAddress(), e);
        }
    }

    protected SendingMessage convertToSendingMessage(String address, String from, String cc, String bcc, String caption, String body,
                                                     String bodyContentType,
                                                     @Nullable List<EmailHeader> headers,
                                                     @Nullable EmailAttachment[] attachments,
                                                     @Nullable Integer attemptsCount, @Nullable Date deadline) {
        SendingMessage sendingMessage = metadata.create(SendingMessage.class);

        sendingMessage.setAddress(address);
        sendingMessage.setCc(cc);
        sendingMessage.setBcc(bcc);
        sendingMessage.setFrom(from);
        sendingMessage.setContentText(body);
        sendingMessage.setCaption(caption);
        sendingMessage.setAttemptsCount(attemptsCount);
        sendingMessage.setDeadline(deadline);
        sendingMessage.setAttemptsMade(0);

        if (Strings.isNullOrEmpty(bodyContentType)) {
            bodyContentType = getContentBodyType(sendingMessage);
            sendingMessage.setBodyContentType(bodyContentType);
        } else {
            sendingMessage.setBodyContentType(bodyContentType);
        }

        if (attachments != null && attachments.length > 0) {
            StringBuilder attachmentsName = new StringBuilder();
            List<SendingAttachment> sendingAttachments = new ArrayList<>(attachments.length);
            for (EmailAttachment ea : attachments) {
                attachmentsName.append(ea.getName()).append(";");

                SendingAttachment sendingAttachment = toSendingAttachment(ea);
                sendingAttachment.setMessage(sendingMessage);
                sendingAttachments.add(sendingAttachment);
            }
            sendingMessage.setAttachments(sendingAttachments);
            sendingMessage.setAttachmentsName(attachmentsName.toString());
        } else {
            sendingMessage.setAttachments(Collections.emptyList());
        }

        if (headers != null && !headers.isEmpty()) {
            StringBuilder headersLine = new StringBuilder();
            for (EmailHeader header : headers) {
                headersLine.append(header.toString()).append(SendingMessage.HEADERS_SEPARATOR);
            }
            sendingMessage.setHeaders(headersLine.toString());
        } else {
            sendingMessage.setHeaders(null);
        }

        replaceRecipientIfNecessary(sendingMessage);

        return sendingMessage;
    }

    protected String getContentBodyType(SendingMessage sendingMessage) {
        String bodyContentType;
        String text = sendingMessage.getContentText();
        if (text.trim().startsWith("<html>")) {
            bodyContentType = "text/html; charset=UTF-8";
        } else {
            bodyContentType = "text/plain; charset=UTF-8";
        }
        log.debug("Content body type is not set for email '{}' with addresses: {}. Will be used '{}'.",
                sendingMessage.getCaption(), sendingMessage.getAddress(), bodyContentType);
        return bodyContentType;
    }

    protected void replaceRecipientIfNecessary(SendingMessage msg) {
        if (emailerProperties.isSendAllToAdmin()) {
            String adminAddress = emailerProperties.getAdminAddress();
            log.warn(String.format(
                    "Replacing actual email recipient '%s' by admin address '%s'", msg.getAddress(), adminAddress
            ));
            msg.setAddress(adminAddress);
        }
    }

    protected SendingAttachment toSendingAttachment(EmailAttachment ea) {
        SendingAttachment sendingAttachment = metadata.create(SendingAttachment.class);
        sendingAttachment.setContent(ea.getData());
        sendingAttachment.setContentId(ea.getContentId());
        sendingAttachment.setName(ea.getName());
        sendingAttachment.setEncoding(ea.getEncoding());
        sendingAttachment.setDisposition(ea.getDisposition());
        return sendingAttachment;
    }

    protected byte[] bodyTextToBytes(SendingMessage message) {
        byte[] bodyBytes = message.getContentText().getBytes(StandardCharsets.UTF_8);
        return bodyBytes;
    }

    protected String bodyTextFromByteArray(byte[] bodyContent) {
        return new String(bodyContent, StandardCharsets.UTF_8);
    }

    protected boolean isNeedToRetry(Exception e) {
        if (e instanceof MailSendException) {
            if (e.getCause() instanceof SMTPAddressFailedException) {
                return false;
            }
        } else if (e instanceof AddressException) {
            return false;
        }
        return true;
    }

    @Override
    public void migrateEmailsToFileStorage(List<SendingMessage> messages) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            for (SendingMessage msg : messages) {
                migrateMessage(em, msg);
            }
            tx.commit();
        }
    }

    @Override
    public void migrateAttachmentsToFileStorage(List<SendingAttachment> attachments) {
        try (Transaction tx = persistence.createTransaction()) {
            EntityManager em = persistence.getEntityManager();

            for (SendingAttachment attachment : attachments) {
                migrateAttachment(em, attachment);
            }

            tx.commit();
        }
    }

    @Override
    public boolean isFileStorageUsed() {
        return emailerProperties.isFileStorageUsed();
    }

    protected void migrateMessage(EntityManager em, SendingMessage msg) {
        msg = em.merge(msg);
        byte[] bodyBytes = bodyTextToBytes(msg);
        String fileName = "Email_" + msg.getId() + "." + BODY_FILE_EXTENSION;
        URI contentTextFile = fileStorage.createReference(fileName);
        fileStorage.saveStream(contentTextFile, new ByteArrayInputStream(bodyBytes));
        msg.setContentTextFile(contentTextFile);
        msg.setContentText(null);
    }

    protected void migrateAttachment(EntityManager em, SendingAttachment attachment) {
        attachment = em.merge(attachment);
        URI contentFile = fileStorage.createReference(attachment.getName());
        fileStorage.saveStream(contentFile, new ByteArrayInputStream(attachment.getContent()));
        attachment.setContentFile(contentFile);
        attachment.setContent(null);
    }

    protected static class EmailSendTask implements Runnable {

        private SendingMessage sendingMessage;
        private static final Logger log = LoggerFactory.getLogger(EmailSendTask.class);
        private Authenticator authenticator;

        public EmailSendTask(SendingMessage message, Authenticator authenticator) {
            this.sendingMessage = message;
            this.authenticator = authenticator;
        }

        @Override
        public void run() {
            try {
                EmailerImpl emailer = AppBeans.get(Emailer.NAME);
                authenticator.begin(emailer.getEmailerLogin());
                try {
                    emailer.sendSendingMessage(sendingMessage);
                } finally {
                    authenticator.end();
                }
            } catch (Exception e) {
                log.error("Exception while sending email: ", e);
            }
        }
    }

    protected static class MessagePersistingContext {
        public final List<URI> files = new ArrayList<>();

        public void finished() {
            files.clear();
        }
    }

    protected String getEmailerLogin() {
        return emailerProperties.getUserLogin();
    }
}
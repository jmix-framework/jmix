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
import com.sun.mail.smtp.SMTPAddressFailedException;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.email.*;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component("email_Emailer")
public class EmailerImpl implements Emailer {

    private static final Logger log = LoggerFactory.getLogger(EmailerImpl.class);

    @Autowired
    protected EmailerProperties emailerProperties;

    protected AtomicInteger callCount = new AtomicInteger(0);

    @Resource(name = "mailSendTaskExecutor")
    protected TaskExecutor mailSendTaskExecutor;

    @Autowired
    protected EmailDataProvider emailDataProvider;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected SystemAuthenticator authenticator;

    @Autowired
    protected EmailSender emailSender;

    @Autowired
    protected MetadataTools metadataTools;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected EmailCleaner emailCleaner;

    @Override
    public void sendEmail(String address, String subject, String body, String bodyContentType, Boolean important,
                          EmailAttachment... attachment) throws EmailException {
        EmailInfo emailInfo = EmailInfoBuilder.create(address, subject, body)
                .setBodyContentType(bodyContentType)
                .setImportant(important)
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
    public SendingMessage sendEmailAsync(EmailInfo info) {
        //noinspection UnnecessaryLocalVariable
        SendingMessage result = sendEmailAsync(info, null, null);
        return result;
    }

    @Override
    public SendingMessage sendEmailAsync(EmailInfo info, @Nullable Integer attemptsLimit, @Nullable Date deadline) {
        prepareEmailInfo(info);
        SendingMessage message = convertToSendingMessage(info, attemptsLimit, deadline);
        emailDataProvider.persistMessage(message, SendingStatus.QUEUE);
        return message;
    }

    protected void prepareEmailInfo(EmailInfo emailInfo) {
        if (StringUtils.isBlank(emailInfo.getAddresses())) {
            throw new IllegalArgumentException("Addresses are not specified");
        }

        if (emailInfo.getFrom() == null) {
            String defaultFromAddress = emailerProperties.getFromAddress();
            if (defaultFromAddress == null) {
                throw new IllegalStateException("jmix.email.fromAddress not set in the system");
            }
            emailInfo.setFrom(defaultFromAddress);
        }
    }

    protected SendingMessage convertToSendingMessage(EmailInfo info, @Nullable Integer attemptsLimit, @Nullable Date deadline) {
        SendingMessage sendingMessage = metadata.create(SendingMessage.class);

        sendingMessage.setAddress(info.getAddresses());
        sendingMessage.setCc(info.getCc());
        sendingMessage.setBcc(info.getBcc());
        sendingMessage.setFrom(info.getFrom());
        sendingMessage.setContentText(info.getBody());
        sendingMessage.setSubject(info.getSubject());
        sendingMessage.setAttemptsLimit(attemptsLimit);
        sendingMessage.setDeadline(deadline);
        sendingMessage.setAttemptsMade(0);
        sendingMessage.setImportant(info.getImportant());

        String bodyContentType = info.getBodyContentType();
        if (Strings.isNullOrEmpty(bodyContentType)) {
            bodyContentType = getContentBodyType(sendingMessage);
        }
        sendingMessage.setBodyContentType(bodyContentType);

        List<EmailAttachment> attachments = info.getAttachments();
        if (CollectionUtils.isNotEmpty(attachments)) {
            StringBuilder attachmentsName = new StringBuilder();
            List<SendingAttachment> sendingAttachments = new ArrayList<>(attachments.size());

            attachments.forEach(ea -> {
                attachmentsName.append(ea.getName()).append(";");

                SendingAttachment sendingAttachment = toSendingAttachment(ea);
                sendingAttachment.setMessage(sendingMessage);
                sendingAttachments.add(sendingAttachment);
            });

            sendingMessage.setAttachments(sendingAttachments);
            sendingMessage.setAttachmentsName(attachmentsName.toString());
        } else {
            sendingMessage.setAttachments(Collections.emptyList());
        }

        String headersLine = null;
        if (CollectionUtils.isNotEmpty(info.getHeaders())) {
            headersLine = info.getHeaders().stream()
                    .map(EmailHeader::toString)
                    .collect(Collectors.joining(SendingMessage.HEADERS_SEPARATOR));
        }
        sendingMessage.setHeaders(headersLine);

        replaceRecipientIfNecessary(sendingMessage);

        return sendingMessage;
    }

    protected void sendSendingMessage(SendingMessage sendingMessage) {
        Objects.requireNonNull(sendingMessage, "sendingMessage is null");
        Objects.requireNonNull(sendingMessage.getAddress(), "sendingMessage.address is null");
        Objects.requireNonNull(sendingMessage.getSubject(), "sendingMessage.subject is null");
        Objects.requireNonNull(sendingMessage.getContentText(), "sendingMessage.contentText is null");
        Objects.requireNonNull(sendingMessage.getFrom(), "sendingMessage.from is null");
        try {
            emailSender.sendEmail(sendingMessage);
            emailDataProvider.updateStatus(sendingMessage, SendingStatus.SENT);
        } catch (Exception e) {
            log.warn("Unable to send email to '{}'", sendingMessage.getAddress(), e);
            SendingStatus newStatus = isNeedToRetry(e) ? SendingStatus.QUEUE : SendingStatus.NOT_SENT;
            emailDataProvider.updateStatus(sendingMessage, newStatus);
        }
    }

    protected void persistAndSendEmail(EmailInfo emailInfo) throws EmailException {
        Objects.requireNonNull(emailInfo.getAddresses(), "addresses are null");
        Objects.requireNonNull(emailInfo.getSubject(), "subject is null");
        Objects.requireNonNull(emailInfo.getBody(), "body is null");
        Objects.requireNonNull(emailInfo.getFrom(), "from is null");

        SendingMessage sendingMessage = convertToSendingMessage(emailInfo, null, null);

        List<String> failedAddresses = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        SendingMessage persistedMessage = persistMessageIfPossible(sendingMessage);

        try {
            emailSender.sendEmail(sendingMessage);
            if (persistedMessage != null) {
                emailDataProvider.updateStatus(persistedMessage, SendingStatus.SENT);
            }
        } catch (Exception e) {
            log.warn("Unable to send email to '{}'", sendingMessage.getAddress(), e);
            failedAddresses.add(sendingMessage.getAddress());
            errorMessages.add(e.getMessage());
            if (persistedMessage != null) {
                emailDataProvider.updateStatus(persistedMessage, SendingStatus.NOT_SENT);
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
            emailDataProvider.persistMessage(clonedMessage, SendingStatus.SENDING);
            return clonedMessage;
        } catch (Exception e) {
            log.error("Failed to persist message '{}'", sendingMessage.getSubject(), e);
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
        int callsToSkip = emailerProperties.getScheduledSendingDelayCallCount();
        int count = callCount.getAndAdd(1);
        if (count < callsToSkip) {
            return "";
        }

        String resultMessage;
        try {
            authenticator.begin(emailerProperties.getAsyncSendingUsername());
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

    protected String sendQueuedEmails() {
        List<SendingMessage> messagesToSend = emailDataProvider.loadEmailsToSend();

        messagesToSend.forEach(this::submitExecutorTask);

        if (messagesToSend.isEmpty()) {
            return "";
        }

        return String.format("Processed %d emails", messagesToSend.size());
    }

    protected void submitExecutorTask(SendingMessage msg) {
        try {
            Runnable mailSendTask = applicationContext.getBean(EmailSendTask.class, msg);
            mailSendTaskExecutor.execute(mailSendTask);
        } catch (RejectedExecutionException e) {
            emailDataProvider.updateStatus(msg, SendingStatus.QUEUE);
        } catch (Exception e) {
            log.error("Exception while sending email to '{}': ", msg.getAddress(), e);

            SendingStatus newStatus = isNeedToRetry(e) ? SendingStatus.QUEUE : SendingStatus.NOT_SENT;
            emailDataProvider.updateStatus(msg, newStatus);
        }
    }

    protected String getContentBodyType(SendingMessage sendingMessage) {
        String bodyContentType;
        String text = sendingMessage.getContentText();
        bodyContentType = text.trim().startsWith("<html>") ? "text/html; charset=UTF-8" : "text/plain; charset=UTF-8";
        log.debug("Content body type is not set for email '{}' with addresses: {}. Will be used '{}'.",
                sendingMessage.getSubject(), sendingMessage.getAddress(), bodyContentType);
        return bodyContentType;
    }

    protected void replaceRecipientIfNecessary(SendingMessage msg) {
        if (emailerProperties.isSendAllToAdmin()) {
            String adminAddress = emailerProperties.getAdminAddress();
            log.warn("Replacing actual email recipient '{}' by admin address '{}'", msg.getAddress(), adminAddress);
            msg.setAddress(adminAddress);
            if (msg.getCc() != null) {
                msg.setCc(adminAddress);
            }
            if (msg.getBcc() != null) {
                msg.setBcc(adminAddress);
            }
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
}
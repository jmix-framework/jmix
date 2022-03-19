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

package io.jmix.email;

import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.security.Authenticated;
import io.jmix.data.PersistenceHints;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.*;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

@ManagedResource(objectName = "jmix.email:type=Emailer", description = "Manages email messages")
@Component("email_EmailerManagementFacade")
public class EmailerManagementFacade {

    @Autowired
    protected Emailer emailer;

    protected TransactionTemplate transaction;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected FetchPlanRepository fetchPlanRepository;

    @Autowired
    protected EmailerProperties emailerProperties;

    @Autowired
    protected JavaMailSenderImpl javaMailSender;

    protected Properties javaMailProperties;

    @Autowired
    protected EmailDataProvider emailDataProvider;

    @Autowired
    protected void setJavaMailProperties() {
        javaMailProperties = javaMailSender.getJavaMailProperties();
    }

    @Autowired
    protected void setTransaction(PlatformTransactionManager transactionManager) {
        transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @ManagedAttribute(description = "Default \"from\" address")
    public String getFromAddress() {
        return emailerProperties.getFromAddress();
    }

    @ManagedAttribute(description = "SMTP server address")
    @Nullable
    public String getSmtpHost() {
        return javaMailSender.getHost();
    }

    @ManagedAttribute(description = "SMTP server port")
    public int getSmtpPort() {
        return javaMailSender.getPort();
    }

    @ManagedAttribute(description = "User name for the SMTP server authentication")
    @Nullable
    public String getSmtpUser() {
        return javaMailSender.getUsername();
    }

    @ManagedAttribute(description = "Whether to authenticate on SMTP server")
    public String getSmtpAuthRequired() {
        return javaMailProperties.getProperty("mail.smtp.auth");
    }

    @ManagedAttribute(description = "Whether to use STARTTLS command during the SMTP server authentication")
    public String getStarttlsEnable() {
        return  javaMailProperties.getProperty("mail.smtp.starttls.enable");
    }

    @ManagedAttribute(description = "If set to true, use SSL to connect")
    public String getSmtpSslEnabled() {
        return javaMailProperties.getProperty("mail.smtp.ssl.enable");
    }

    @ManagedAttribute(description = "SMTP I/O timeout value in seconds")
    public String getSmtpTimeoutSec() {
        return javaMailProperties.getProperty("mail.smtp.timeout");
    }

    @ManagedAttribute(description = "SMTP connection timeout value in seconds")
    public String getSmtpConnectionTimeoutSec() {
        return javaMailProperties.getProperty("mail.smtp.connectiontimeout");
    }

    @Authenticated
    @ManagedOperation(description = "Send a test email to the specified addresses")
    @ManagedOperationParameters({@ManagedOperationParameter(name = "addresses", description = "")})
    public String sendTestEmail(String addresses) {
        try {
            String att = "<html><body><h1>Test attachment</h1></body></html>";
            EmailAttachment emailAtt = EmailAttachment.createTextAttachment(att, StandardCharsets.UTF_8.name(), "test attachment.html");
            EmailInfo emailInfo = EmailInfoBuilder.create(addresses, "Test email", "<html><body><h1>Test email</h1></body></html>")
                    .setAttachments(emailAtt)
                    .build();
            emailer.sendEmail(emailInfo);
            return "Email to '" + addresses + "' sent successfully";
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Authenticated
    @ManagedOperation(description = "Migrate existing email history to use file storage")
    public String migrateEmailsToFileStorage(String password) {
        if (!"do migration".equals(password)) {
            return "Wrong password";
        }

        int processed;
        do {
            try {
                processed = migrateMessagesBatch();
            } catch (Exception e) {
                throw new RuntimeException("Failed to migrate batch", e);
            }
        } while (processed > 0);
        do {
            try {
                processed = migrateAttachmentsBatch();
            } catch (Exception e) {
                throw new RuntimeException("Failed to migrate batch", e);
            }
        } while (processed > 0);

        return "Finished";
    }

    protected int migrateMessagesBatch() {
        List<SendingMessage> resultList = transaction.execute(status -> loadMessagesBatch());

        if (CollectionUtils.isNotEmpty(resultList)) {
            emailDataProvider.migrateEmailsToFileStorage(resultList);
        }

        return resultList != null ? resultList.size() : 0;
    }

    protected int migrateAttachmentsBatch() {
        List<SendingAttachment> resultList = transaction.execute(status -> loadAttachmentsBatch());

        if (CollectionUtils.isNotEmpty(resultList)) {
            emailDataProvider.migrateAttachmentsToFileStorage(resultList);
        }

        return resultList != null ? resultList.size() : 0;
    }

    protected List<SendingMessage> loadMessagesBatch() {
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(SendingMessage.class, FetchPlan.INSTANCE_NAME);
        return entityManager.createQuery("select m from email_SendingMessage m where m.contentText is not null",
                SendingMessage.class)
                .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                .setMaxResults(50)
                .getResultList();
    }

    protected List<SendingAttachment> loadAttachmentsBatch() {
        FetchPlan fetchPlan = fetchPlanRepository.getFetchPlan(SendingAttachment.class, FetchPlan.INSTANCE_NAME);
        return entityManager.createQuery("select a from email_SendingAttachment a where a.content is not null",
                SendingAttachment.class)
                .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                .setMaxResults(50)
                .getResultList();
    }
}
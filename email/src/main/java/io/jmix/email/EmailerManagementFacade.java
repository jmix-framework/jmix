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

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.TypedQuery;
import io.jmix.core.FetchPlan;
import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import io.jmix.email.impl.EmailerImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.*;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@ManagedResource(objectName = "jmix.email:type=Emailer", description = "Manages email messages")
@Component("email_EmailerManagementFacade")
public class EmailerManagementFacade {

    @Autowired
    protected Emailer emailer;

    @Autowired
    protected Persistence persistence;

    @Autowired
    protected EmailerProperties emailerProperties;
    
    @Autowired
    protected EmailSmtpProperties smtpProperties;
    
    @Autowired
    protected EmailDataProvider emailDataProvider;

    private static final Logger log = LoggerFactory.getLogger(EmailerImpl.class);

    @ManagedAttribute(description = "Default \"from\" address")
    public String getFromAddress() {
        return emailerProperties.getFromAddress();
    }

    @ManagedAttribute(description = "SMTP server address")
    public String getSmtpHost() {
        return smtpProperties.getHost();
    }

    @ManagedAttribute(description = "SMTP server port")
    public int getSmtpPort() {
        return smtpProperties.getPort();
    }

    @ManagedAttribute(description = "User name for the SMTP server authentication")
    public String getSmtpUser() {
        return smtpProperties.getUser();
    }

    @ManagedAttribute(description = "Whether to authenticate on SMTP server")
    public boolean getSmtpAuthRequired() {
        return smtpProperties.isAuthRequired();
    }

    @ManagedAttribute(description = "Whether to use STARTTLS command during the SMTP server authentication")
    public boolean getStarttlsEnable() {
        return smtpProperties.isStartTlsEnabled();
    }

    @ManagedAttribute(description = "If set to true, use SSL to connect")
    public boolean getSmtpSslEnabled() {
        return smtpProperties.isSslEnabled();
    }

    @ManagedAttribute(description = "SMTP I/O timeout value in seconds")
    public int getSmtpTimeoutSec() {
        return smtpProperties.getTimeoutSec();
    }

    @ManagedAttribute(description = "SMTP connection timeout value in seconds")
    public int getSmtpConnectionTimeoutSec() {
        return smtpProperties.getConnectionTimeoutSec();
    }

    //todo: @Authenticated
    @ManagedOperation(description = "Update properties for JavaMail session")
    public void updateSession() {
        emailer.updateSession();
    }

    //todo: @Authenticated
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

    //todo: @Authenticated
   // todo: @JmxRunAsync
    @ManagedOperation(description = "Migrate existing email history to use file storage")
    public String migrateEmailsToFileStorage(String password) {
        if (!"do migration".equals(password)) {
            return "Wrong password";
        }

        int processed;
        do {
            try {
                processed = migrateMessagesBatch();
                log.info(String.format("Migrated %d emails", processed));
            } catch (Exception e) {
                throw new RuntimeException("Failed to migrate batch", e);
            }
        } while (processed > 0);
        log.info("Finished migrating emails");
        do {
            try {
                processed = migrateAttachmentsBatch();
                log.info(String.format("Migrated %d attachments", processed));
            } catch (Exception e) {
                throw new RuntimeException("Failed to migrate batch", e);
            }
        } while (processed > 0);
        log.info("Finished migrating attachments");

        return "Finished";
    }

    protected int migrateMessagesBatch() {
        List<SendingMessage> resultList;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            String qstr = "select m from sys_SendingMessage m where m.contentText is not null";
            TypedQuery<SendingMessage> query = em.createQuery(qstr, SendingMessage.class);
            query.setMaxResults(50);
            query.setViewName(FetchPlan.INSTANCE_NAME);

            resultList = query.getResultList();
            tx.commit();
        } finally {
            tx.end();
        }

        if (CollectionUtils.isNotEmpty(resultList)) {
            emailDataProvider.migrateEmailsToFileStorage(resultList);
        }

        return resultList.size();
    }

    protected int migrateAttachmentsBatch() {
        List<SendingAttachment> resultList;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            String qstr = "select a from sys_SendingAttachment a where a.content is not null";
            TypedQuery<SendingAttachment> query = em.createQuery(qstr, SendingAttachment.class);
            query.setMaxResults(50);
            query.setViewName(FetchPlan.INSTANCE_NAME);

            resultList = query.getResultList();

            tx.commit();
        } finally {
            tx.end();
        }

        if (CollectionUtils.isNotEmpty(resultList)) {
            emailDataProvider.migrateAttachmentsToFileStorage(resultList);
        }

        return resultList.size();
    }
}

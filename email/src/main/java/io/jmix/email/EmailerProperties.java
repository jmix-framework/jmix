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

import io.jmix.email.entity.SendingAttachment;
import io.jmix.email.entity.SendingMessage;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.PositiveOrZero;

@Validated
@ConfigurationProperties(prefix = "jmix.email")
@ConstructorBinding
public class EmailerProperties {

    /**
     * Default "from" address
     */
    String fromAddress;

    /**
     * How many scheduler ticks to skip after server startup. Actual sending will start with the next call. This reduces
     * the server load on startup.
     */
    int scheduledSendingDelayCallCount;

    /**
     * Number of queued messages per every scheduler tick. Scheduler will process no more than given number of queued
     * messages per every scheduler tick.
     */
    int messageQueueCapacity;

    /**
     * Max number of attempts to send a message, after which the message's status is set to NOT_SENT.
     */
    int defaultSendingAttemptsLimit;

    /**
     * Timeout in seconds for message in {@link SendingStatus#SENDING} status to be successfully sent or failed. After
     * this time passes, emailer will try to resend email again.
     */
    int sendingTimeoutSec;

    /**
     * All emails go to this address if {@link #sendAllToAdmin} is enabled, regardless of actual recipient.
     */
    String adminAddress;

    /**
     * Whether all email messages go to {@link #adminAddress}.
     */
    boolean sendAllToAdmin;

    /**
     * Whether email body text and attachments are stored in file storage instead of BLOB columns in database. Should be
     * used if application stores lots of emails and/or email attachments.
     *
     * @see SendingMessage#getContentText()
     * @see SendingAttachment#getContentFile()
     */
    boolean useFileStorage;

    /**
     * Username used by asynchronous sending mechanism to be able to store information in the database.
     */
    String asyncSendingUsername;

    /**
     * Whether the default Email Sending quartz scheduling configuration is used.
     */
    boolean useDefaultQuartzConfiguration;

    /**
     * CRON expression that is used by default Email Sending quartz scheduling configuration.
     */
    String emailSendingCron;

    /**
     * Whether the default Email Cleaning quartz scheduling configuration is used.
     */
    boolean useDefaultEmailCleaningQuartzConfiguration;

    /**
     * Maximum age (in days) of important messages after which they must be deleted. Zero value (0) means that messages
     * won't be removed.
     */
    @PositiveOrZero
    int maxAgeOfImportantMessages;

    /**
     * Maximum age (in days) of messages after which they must be deleted. Zero value (0) means that messages won't be
     * removed.
     */
    @PositiveOrZero
    int maxAgeOfNonImportantMessages;

    /**
     * CRON expression that is used by default Email Cleaning quartz scheduling configuration.
     */
    String emailCleaningCron;

    /**
     * Whether the file storage cleaning should be performed while the cleaning scheduler is working.
     */
    boolean cleanFileStorage;

    public EmailerProperties(@DefaultValue("DoNotReply@localhost") String fromAddress,
                             @DefaultValue("2") int scheduledSendingDelayCallCount,
                             @DefaultValue("100") int messageQueueCapacity,
                             @DefaultValue("10") int defaultSendingAttemptsLimit,
                             @DefaultValue("240") int sendingTimeoutSec,
                             @DefaultValue("admin@localhost") String adminAddress,
                             @DefaultValue("false") boolean sendAllToAdmin,
                             @DefaultValue("false") boolean useFileStorage,
                             @DefaultValue("admin") String asyncSendingUsername,
                             @DefaultValue("0 * * * * ?") String emailSendingCron,
                             @DefaultValue("0") int maxAgeOfImportantMessages,
                             @DefaultValue("0") int maxAgeOfNonImportantMessages,
                             @DefaultValue("0 0 0 * * ?") String emailCleaningCron,
                             @DefaultValue("false") boolean cleanFileStorage) {
        this.fromAddress = fromAddress;
        this.scheduledSendingDelayCallCount = scheduledSendingDelayCallCount;
        this.messageQueueCapacity = messageQueueCapacity;
        this.defaultSendingAttemptsLimit = defaultSendingAttemptsLimit;
        this.sendingTimeoutSec = sendingTimeoutSec;
        this.adminAddress = adminAddress;
        this.sendAllToAdmin = sendAllToAdmin;
        this.useFileStorage = useFileStorage;
        this.asyncSendingUsername = asyncSendingUsername;
        this.emailSendingCron = emailSendingCron;
        this.maxAgeOfImportantMessages = maxAgeOfImportantMessages;
        this.maxAgeOfNonImportantMessages = maxAgeOfNonImportantMessages;
        this.emailCleaningCron = emailCleaningCron;
        this.cleanFileStorage = cleanFileStorage;
    }

    /**
     * @see #fromAddress
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * @see #scheduledSendingDelayCallCount
     */
    public int getScheduledSendingDelayCallCount() {
        return scheduledSendingDelayCallCount;
    }

    /**
     * @see #messageQueueCapacity
     */
    public int getMessageQueueCapacity() {
        return messageQueueCapacity;
    }

    /**
     * @see #defaultSendingAttemptsLimit
     */
    public int getDefaultSendingAttemptsLimit() {
        return defaultSendingAttemptsLimit;
    }

    /**
     * @see #sendingTimeoutSec
     */
    public int getSendingTimeoutSec() {
        return sendingTimeoutSec;
    }

    /**
     * @see #adminAddress
     */
    public String getAdminAddress() {
        return adminAddress;
    }

    /**
     * @see #sendAllToAdmin
     */
    public boolean isSendAllToAdmin() {
        return sendAllToAdmin;
    }

    /**
     * @see #useFileStorage
     */
    public boolean isUseFileStorage() {
        return useFileStorage;
    }

    /**
     * @see #asyncSendingUsername
     */
    public String getAsyncSendingUsername() {
        return asyncSendingUsername;
    }

    /**
     * @see #useDefaultQuartzConfiguration
     */
    public boolean getUseDefaultQuartzConfiguration() {
        return useDefaultQuartzConfiguration;
    }

    /**
     * @see #emailSendingCron
     */
    public String getEmailSendingCron() {
        return emailSendingCron;
    }

    /**
     * @see #useDefaultEmailCleaningQuartzConfiguration
     */
    public boolean getUseDefaultEmailCleaningQuartzConfiguration() {
        return useDefaultEmailCleaningQuartzConfiguration;
    }

    /**
     * @see #maxAgeOfImportantMessages
     */
    public int getMaxAgeOfImportantMessages() {
        return maxAgeOfImportantMessages;
    }

    /**
     * @see #maxAgeOfNonImportantMessages
     */
    public int getMaxAgeOfNonImportantMessages() {
        return maxAgeOfNonImportantMessages;
    }

    /**
     * @see #emailCleaningCron
     */
    public String getEmailCleaningCron() {
        return emailCleaningCron;
    }

    /**
     * @see #cleanFileStorage
     */
    public boolean getCleanFileStorage() {
        return cleanFileStorage;
    }
}

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

    String fromAddress;
    int scheduledSendingDelayCallCount;
    int messageQueueCapacity;
    int defaultSendingAttemptsLimit;
    int sendingTimeoutSec;
    String adminAddress;
    boolean sendAllToAdmin;
    boolean useFileStorage;
    String asyncSendingUsername;
    boolean useDefaultQuartzConfiguration;
    String emailSendingCron;
    boolean useDefaultEmailCleaningQuartzConfiguration;
    @PositiveOrZero
    int maxAgeOfImportantMessages;
    @PositiveOrZero
    int maxAgeOfNonImportantMessages;
    String emailCleaningCron;
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
     * @return Default "from" address
     */
    public String getFromAddress() {
        return fromAddress;
    }

    /**
     * How many scheduler ticks to skip after server startup.
     * Actual sending will start with the next call.
     * <br> This reduces the server load on startup.
     *
     * @return Number of scheduler ticks to skip after server startup
     */
    public int getScheduledSendingDelayCallCount() {
        return scheduledSendingDelayCallCount;
    }

    /**
     * Scheduler will process no more than given number of queued messages per every scheduler tick.
     *
     * @return Number of queued messages per every scheduler tick
     */
    public int getMessageQueueCapacity() {
        return messageQueueCapacity;
    }

    /**
     * @return Max number of attempts to send a message, after which the message's status is set to NOT_SENT.
     */
    public int getDefaultSendingAttemptsLimit() {
        return defaultSendingAttemptsLimit;
    }

    /**
     * Timeout in seconds for message in {@link SendingStatus#SENDING} status
     * to be successfully sent or failed. After this time passes, emailer will try to resend email again.
     */
    public int getSendingTimeoutSec() {
        return sendingTimeoutSec;
    }

    /**
     * All emails go to this address if {@link #isSendAllToAdmin()} ()} is enabled, regardless of actual recipient.
     */
    public String getAdminAddress() {
        return adminAddress;
    }

    /**
     * @return true if all email messages go to {@link #getAdminAddress()}.
     */
    public boolean isSendAllToAdmin() {
        return sendAllToAdmin;
    }

    /**
     * When turned on, email body text and attachments will be stored in file storage
     * instead of BLOB columns in database.
     * Should be used if application stores lots of emails and/or email attachments.
     *
     * @return true if email body text and attachments are stored in file storage instead of BLOB columns in database.
     * @see SendingMessage#getContentText()
     * @see SendingAttachment#getContentFile()
     */
    public boolean isUseFileStorage() {
        return useFileStorage;
    }

    /**
     * @return Username used by asynchronous sending mechanism to be able to store information in the database.
     */
    public String getAsyncSendingUsername() {
        return asyncSendingUsername;
    }

    /**
     * @return true if default Email Sending quartz scheduling configuration is used. False otherwise
     */
    public boolean getUseDefaultQuartzConfiguration() {
        return useDefaultQuartzConfiguration;
    }

    /**
     * @return CRON expression that is used by default Email Sending quartz scheduling configuration
     */
    public String getEmailSendingCron() {
        return emailSendingCron;
    }

    /**
     * @return true if default Email Cleaning quartz scheduling configuration is used. False otherwise
     */
    public boolean getUseDefaultEmailCleaningQuartzConfiguration() {
        return useDefaultEmailCleaningQuartzConfiguration;
    }

    /**
     * @return the maximum age (in days) of important messages after which they must be deleted.
     * Zero value (0) means that messages won't be removed
     */
    public int getMaxAgeOfImportantMessages() {
        return maxAgeOfImportantMessages;
    }

    /**
     * @return the maximum age (in days) of messages after which they must be deleted.
     * Zero value (0) means that messages won't be removed
     */
    public int getMaxAgeOfNonImportantMessages() {
        return maxAgeOfNonImportantMessages;
    }

    /**
     * @return CRON expression that is used by default Email Cleaning quartz scheduling configuration
     */
    public String getEmailCleaningCron() {
        return emailCleaningCron;
    }

    /**
     * @return true if deleted from file storage is performed while the cleaning scheduler is working
     */
    public boolean getCleanFileStorage() {
        return cleanFileStorage;
    }
}

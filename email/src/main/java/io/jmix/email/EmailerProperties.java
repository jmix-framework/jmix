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

@ConfigurationProperties(prefix = "jmix.email")
@ConstructorBinding
public class EmailerProperties {

    String fromAddress;
    int scheduledSendingDelayCallCount;
    int messageQueueCapacity;
    int defaultSendingAttemptsCount;
    int sendingTimeoutSec;
    String adminAddress;
    boolean sendAllToAdmin;
    boolean isFileStorageUsed;
    String asyncSendingUsername;

    public EmailerProperties(@DefaultValue("DoNotReply@localhost") String fromAddress,
                             @DefaultValue("2") int scheduledSendingDelayCallCount,
                             @DefaultValue("100") int messageQueueCapacity,
                             @DefaultValue("10") int defaultSendingAttemptsCount,
                             @DefaultValue("240") int sendingTimeoutSec,
                             @DefaultValue("admin@localhost") String adminAddress,
                             @DefaultValue("false") boolean sendAllToAdmin,
                             @DefaultValue("false") boolean isFileStorageUsed,
                             @DefaultValue("admin") String asyncSendingUsername) {
        this.fromAddress = fromAddress;
        this.scheduledSendingDelayCallCount = scheduledSendingDelayCallCount;
        this.messageQueueCapacity = messageQueueCapacity;
        this.defaultSendingAttemptsCount = defaultSendingAttemptsCount;
        this.sendingTimeoutSec = sendingTimeoutSec;
        this.adminAddress = adminAddress;
        this.sendAllToAdmin = sendAllToAdmin;
        this.isFileStorageUsed = isFileStorageUsed;
        this.asyncSendingUsername = asyncSendingUsername;
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
    public int getDefaultSendingAttemptsCount() {
        return defaultSendingAttemptsCount;
    }

    /**
     * Timeout in seconds for message in {@link SendingStatus#SENDING} status
     * to be successfully sent or failed. After this time passes, emailer will try to resend email again.
     *
     */
    public int getSendingTimeoutSec() {
        return sendingTimeoutSec;
    }

    /**
     * All emails go to this address if {@link #isSendAllToAdmin()} ()} is enabled, regardless of actual recipient.
     *
     */
    public String getAdminAddress() {
        return adminAddress;
    }

    /**
     * 
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
     * @see SendingMessage#getContentText()
     * @see SendingAttachment#getContentFile()
     * 
     * @return true if email body text and attachments are stored in file storage instead of BLOB columns in database.
     * 
     */
    public boolean isFileStorageUsed() {
        return isFileStorageUsed;
    }

    /**
     * 
     * @return Username used by asynchronous sending mechanism to be able to store information in the database.
     */
    public String getAsyncSendingUsername() {
        return asyncSendingUsername;
    }
}

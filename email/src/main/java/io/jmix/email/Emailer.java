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

import io.jmix.email.entity.SendingMessage;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;


/**
 * Provides application with emailing functionality.
 * <br>
 * Sending email can be synchronous (caller's thread is blocked until email is delivered to SMTP server)
 * or asynchronous (email is persisted in a DB queue and sent later by scheduled task).
 * <br>
 * In order to send emails asynchronously, you should register a scheduled task that periodically invokes
 * {@link #processQueuedEmails()} method.
 */
public interface Emailer {
    /**
     * Send email synchronously.
     *
     * @param address         comma or semicolon separated list of addresses
     * @param caption         email subject
     * @param body            email body
     * @param bodyContentType email body like "text/plain; charset=UTF-8" or "text/html; charset=UTF-8", etc
     * @param attachment      email attachments
     * @throws EmailException
     *          in case of any errors
     */
    void sendEmail(String address, String caption, String body, String bodyContentType, EmailAttachment... attachment)
            throws EmailException;

    /**
     * Send email synchronously.
     *
     * @param info email details
     * @throws EmailException in case of any errors
     */
    void sendEmail(EmailInfo info) throws EmailException;

    /**
     * Send email asynchronously, with limited number of attempts.
     *
     * @param info          email details
     * @param attemptsCount count of attempts to send (1 attempt per scheduler tick). If not specified,
     *                      {@link EmailerProperties#getDefaultSendingAttemptsCount()} is used
     * @param deadline      Emailer tries to send message till deadline.
     *                      If deadline has come and message has not been sent, status of this message is changed to
     *                      {@link SendingStatus#NOTSENT}
     * @return list of created {@link SendingMessage}s
     */
    List<SendingMessage> sendEmailAsync(EmailInfo info, @Nullable Integer attemptsCount, @Nullable Date deadline);

    /**
     * Send email asynchronously.
     * <p>
     * This method creates a list of {@link SendingMessage} instances, saves it to the database and returns immediately.
     * The actual sending is performed by the {@link #processQueuedEmails()} method which should be invoked by a
     * scheduled task.
     *
     * @param info email details
     * @return list of created {@link SendingMessage}s
     */
    List<SendingMessage> sendEmailAsync(EmailInfo info);

    /**
     * Send emails added to the queue.
     * <p>
     * This method should be called periodically from a scheduled task.
     *
     * @return short message describing how many emails were sent, or error message
     */
    String processQueuedEmails();

    /**
     * Updates {@code Properties} for JavaMail {@code Session}
     */
    void updateSession();
}
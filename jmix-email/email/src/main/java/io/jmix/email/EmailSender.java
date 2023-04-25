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

import jakarta.mail.MessagingException;
/**
 * Adapter to javax.mail email sending API.
 * <br>
 * Should not be used from application code, use {@link Emailer}.
 */
public interface EmailSender {
    /**
     * Sends email with help of {@link org.springframework.mail.javamail.JavaMailSender}.
     * Message body and attachments' content must be loaded from file storage.
     * <br>
     * Use {@link Emailer} instead if you need email to be delivered reliably and stored to email history.
     *
     * @throws MessagingException if delivery fails
     */
    void sendEmail(SendingMessage sendingMessage) throws MessagingException;
}
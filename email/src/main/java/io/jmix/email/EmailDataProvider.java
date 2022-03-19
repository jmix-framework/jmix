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

import java.util.List;

/**
 * Interface provides methods to load, store and update email message and attachment entities
 */
public interface EmailDataProvider {
    /**
     * Loads email messages to be sent.
     */
    List<SendingMessage> loadEmailsToSend();

    /**
     * Update status for specified message.
     *
     * @param sendingMessage message
     * @param status new status
     */
    void updateStatus(SendingMessage sendingMessage, SendingStatus status);

    /**
     * Loads content text for given message.
     *
     * @return email content text
     */
    String loadContentText(SendingMessage sendingMessage);

    /**
     * Stores given message with specified status.
     */
    void persistMessage(SendingMessage sendingMessage, SendingStatus status);

    /**
     * Migrate list of existing messages to be stored in file storage, in a single transaction.
     */
    void migrateEmailsToFileStorage(List<SendingMessage> messages);

    /**
     * Migrate list of existing email attachments to be stored in file storage, in a single transaction.
     */
    void migrateAttachmentsToFileStorage(List<SendingAttachment> attachments);
}

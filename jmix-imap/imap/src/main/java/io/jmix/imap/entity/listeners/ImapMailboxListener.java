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

package io.jmix.imap.entity.listeners;

import io.jmix.data.AttributeChangesProvider;
import io.jmix.data.listener.*;
import io.jmix.imap.crypto.Encryptor;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.sync.ImapMailboxSyncActivationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component("imap_MailboxListener")
public class ImapMailboxListener implements BeforeInsertEntityListener<ImapMailBox>,
        BeforeUpdateEntityListener<ImapMailBox>,
        AfterInsertEntityListener<ImapMailBox>,
        AfterUpdateEntityListener<ImapMailBox>,
        BeforeDeleteEntityListener<ImapMailBox> {

    private final static Logger log = LoggerFactory.getLogger(ImapMailboxListener.class);

    @Autowired
    protected Encryptor encryptor;

    @Autowired
    protected AttributeChangesProvider attributeChangesProvider;

    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void onBeforeInsert(ImapMailBox entity) {
        setEncryptedPassword(entity);
    }

    @Override
    public void onBeforeUpdate(ImapMailBox entity) {
        if (attributeChangesProvider.isChanged(entity.getAuthentication(), "password")) {
            setEncryptedPassword(entity);
        }
        applicationEventPublisher.publishEvent(createDeactivationEvent(entity));
    }

    protected void setEncryptedPassword(ImapMailBox entity) {
        log.debug("Encrypt password for {}", entity);
        String encryptedPassword = encryptor.getEncryptedPassword(entity);
        entity.getAuthentication().setPassword(encryptedPassword);
    }

    @Override
    public void onBeforeDelete(ImapMailBox entity) {
        applicationEventPublisher.publishEvent(createDeactivationEvent(entity));
    }

    @Override
    public void onAfterInsert(ImapMailBox entity) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            public void afterCommit() {
                applicationEventPublisher.publishEvent(createActivationEvent(entity));
            }
        });
    }

    @Override
    public void onAfterUpdate(ImapMailBox entity) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            public void afterCommit() {
                applicationEventPublisher.publishEvent(createActivationEvent(entity));
            }
        });
    }

    protected ImapMailboxSyncActivationEvent createActivationEvent(ImapMailBox entity) {
        return new ImapMailboxSyncActivationEvent(entity, ImapMailboxSyncActivationEvent.Type.ACTIVATE);
    }

    protected ImapMailboxSyncActivationEvent createDeactivationEvent(ImapMailBox entity) {
        return new ImapMailboxSyncActivationEvent(entity, ImapMailboxSyncActivationEvent.Type.DEACTIVATE);
    }
}

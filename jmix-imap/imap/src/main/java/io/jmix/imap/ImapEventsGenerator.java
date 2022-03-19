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

package io.jmix.imap;

import io.jmix.imap.entity.ImapFolder;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.events.BaseImapEvent;
import io.jmix.imap.events.NewEmailImapEvent;

import java.util.Collection;

/**
 * An extension point for IMAP events. A bean implementing this interface can be specified in an IMAP configuration.
 * Such beans can be useful for applying IMAP extensions and custom communication mechanisms specific
 * for particular mailboxes
 */
public interface ImapEventsGenerator {

    /**
     * Performs bootstrap logic for mailbox synchronization, e.g. attaching listeners or schedule background tasks
     *
     * @param mailBox IMAP mailbox
     */
    void init(ImapMailBox mailBox);
    /**
     * Releases resources used for synchronization, e.g. detaching listeners or cancelling scheduled background tasks
     *
     * @param mailBox IMAP mailbox
     */
    void shutdown(ImapMailBox mailBox);
    /**
     * Emits events for new messages in a mailbox folder accumulated since the previous call of this method for the folder
     *
     * @param folder IMAP mailbox folder
     * @return       events related to new messages in the folder,
     * can emit not only instances of {@link NewEmailImapEvent}
     *
     */
    Collection<? extends BaseImapEvent> generateForNewMessages(ImapFolder folder);
    /**
     * Emits events for modified messages in a mailbox folder accumulated since the previous call of this method
     * for the folder
     *
     * @param folder IMAP mailbox folder
     * @return       events related to modified messages in the folder
     *
     */
    Collection<? extends BaseImapEvent> generateForChangedMessages(ImapFolder folder);
    /**
     * Emits events for missed (moved to other folder or deleted) messages in a mailbox folder accumulated since the
     * previous call of this method for the folder
     *
     * @param folder IMAP mailbox folder
     * @return       events related to missed messages in the folder
     *
     */
    Collection<? extends BaseImapEvent> generateForMissedMessages(ImapFolder folder);
}

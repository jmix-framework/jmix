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

package io.jmix.imap.sync;

import io.jmix.imap.entity.ImapMailBox;
import org.springframework.context.ApplicationEvent;

public class ImapMailboxSyncActivationEvent extends ApplicationEvent {

    protected final ImapMailBox mailBox;
    protected final Type type;

    public ImapMailboxSyncActivationEvent(ImapMailBox mailBox, Type type) {
        super(mailBox);
        this.mailBox = mailBox;
        this.type = type;
    }

    public ImapMailBox getMailBox() {
        return mailBox;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        ACTIVATE, DEACTIVATE
    }
}
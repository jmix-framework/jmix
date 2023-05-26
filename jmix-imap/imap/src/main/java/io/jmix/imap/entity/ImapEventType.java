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

package io.jmix.imap.entity;

import io.jmix.core.metamodel.datatype.impl.EnumClass;
import io.jmix.imap.events.*;

import org.springframework.lang.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public enum ImapEventType implements EnumClass<String> {

    /**
     * Event type to capture new message in folder
     */
    NEW_EMAIL("new_email", NewEmailImapEvent.class),
    /**
     * Event type to capture mark message as read
     */
    EMAIL_SEEN("seen", EmailSeenImapEvent.class),
    /**
     * Event type to capture new reply for message
     */
    NEW_ANSWER("new_answer", EmailAnsweredImapEvent.class),
    /**
     * Event type to capture move message to different folder
     */
    EMAIL_MOVED("moved", EmailMovedImapEvent.class),
    /**
     * Event type to capture any change in IMAP flags of message
     */
    FLAGS_UPDATED("flags_updated", EmailFlagChangedImapEvent.class),
    /**
     * Event type to capture message removal
     */
    EMAIL_DELETED("deleted", EmailDeletedImapEvent.class),
    /**
     * Event type to capture new message thread in folder
     */
    NEW_THREAD("new_thread", NewThreadImapEvent.class);

    private final String id;
    private final Class<? extends BaseImapEvent> eventClass;

    ImapEventType(String id, Class<? extends BaseImapEvent> eventClass) {
        this.id = id;
        this.eventClass = eventClass;
    }

    public String getId() {
        return id;
    }

    public Class<? extends BaseImapEvent> getEventClass() {
        return eventClass;
    }

    @Nullable
    public static ImapEventType fromId(String id) {
        for (ImapEventType at : ImapEventType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    public static Collection<ImapEventType> getByEventType(Class<? extends BaseImapEvent> eventClass) {
        return Arrays.stream(ImapEventType.values())
                .filter(event -> eventClass.isAssignableFrom(event.getEventClass()))
                .collect(Collectors.toList());
    }
}
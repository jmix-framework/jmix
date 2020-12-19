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

package io.jmix.imap.events;

import io.jmix.imap.entity.ImapMessage;
import io.jmix.imap.flags.ImapFlag;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Map;

/**
 * Event triggered when any IMAP flag of message was changed,
 * {@link #getChangedFlagsWithNewValue()} specifies only modified flags with actual values
 */
public class EmailFlagChangedImapEvent extends BaseImapEvent {

    protected final Map<ImapFlag, Boolean> changedFlagsWithNewValue;

    @SuppressWarnings("WeakerAccess")
    public EmailFlagChangedImapEvent(ImapMessage message, Map<ImapFlag, Boolean> changedFlagsWithNewValue) {
        super(message);

        this.changedFlagsWithNewValue = changedFlagsWithNewValue;
    }

    @SuppressWarnings("unused")
    public Map<ImapFlag, Boolean> getChangedFlagsWithNewValue() {
        return changedFlagsWithNewValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("changedFlagsWithNewValue", changedFlagsWithNewValue).
                append("message", message).
                toString();
    }
}

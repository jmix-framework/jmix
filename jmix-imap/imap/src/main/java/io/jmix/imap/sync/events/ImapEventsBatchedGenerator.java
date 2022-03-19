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

package io.jmix.imap.sync.events;

import io.jmix.imap.ImapEventsGenerator;
import io.jmix.imap.entity.ImapFolder;
import io.jmix.imap.events.BaseImapEvent;

import java.util.Collection;

public abstract class ImapEventsBatchedGenerator implements ImapEventsGenerator {

    @Override
    public final Collection<? extends BaseImapEvent> generateForNewMessages(ImapFolder jmixFolder) {
        return generateForNewMessages(jmixFolder, getBatchSize());
    }

    @Override
    public final Collection<? extends BaseImapEvent> generateForChangedMessages(ImapFolder jmixFolder) {
        return generateForChangedMessages(jmixFolder, getBatchSize());
    }

    @Override
    public final Collection<? extends BaseImapEvent> generateForMissedMessages(ImapFolder jmixFolder) {
        return generateForMissedMessages(jmixFolder, getBatchSize());
    }

    protected abstract int getBatchSize();
    protected abstract Collection<? extends BaseImapEvent> generateForNewMessages(ImapFolder jmixFolder, int batchSize);
    protected abstract Collection<? extends BaseImapEvent> generateForChangedMessages(ImapFolder jmixFolder, int batchSize);
    protected abstract Collection<? extends BaseImapEvent> generateForMissedMessages(ImapFolder jmixFolder, int batchSize);
}

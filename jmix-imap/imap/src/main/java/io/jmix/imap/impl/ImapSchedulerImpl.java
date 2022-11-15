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

package io.jmix.imap.impl;

import io.jmix.core.security.SystemAuthenticator;
import io.jmix.imap.ImapScheduler;
import io.jmix.imap.data.ImapDataProvider;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.sync.ImapFlaglessSynchronizer;
import io.jmix.imap.sync.ImapSynchronizer;
import io.jmix.imap.sync.events.ImapEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Component("imap_ImapScheduler")
public class ImapSchedulerImpl implements ImapScheduler {

    private final static Logger log = LoggerFactory.getLogger(ImapSchedulerImpl.class);

    @Autowired
    protected ImapDataProvider imapDataProvider;

    @Autowired
    protected ImapEvents imapEvents;

    @Autowired
    protected ImapFlaglessSynchronizer imapFlaglessSynchronizer;

    @Autowired
    protected ImapSynchronizer imapSynchronizer;

    @Autowired
    protected SystemAuthenticator authenticator;

    protected final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(@Nonnull Runnable r) {
            Thread thread = new Thread(
                    r, "ImapMailBoxSync-" + threadNumber.getAndIncrement()
            );
            thread.setDaemon(true);
            return thread;
        }
    });

    @Override
    public void syncImap() {
        List<ImapMailBox> mailBoxes = imapDataProvider.findMailBoxes();
        log.trace("IMAP synchronization: found {} mailboxes", mailBoxes.size());

        Map<ImapMailBox, Future> tasks = new HashMap<>(mailBoxes.size());
        mailBoxes.forEach(mailBox -> {
            tasks.put(mailBox, executor.submit(() -> {
                authenticator.begin();
                try {
                    syncMailBox(mailBox);
                } finally {
                    authenticator.end();
                }
            }));
        });

        tasks.keySet().forEach(mailBox -> {
            try {
                tasks.get(mailBox).get();
            } catch (Exception e) {
                log.error(String.format("Error on %s[%s] mailbox sync",
                        mailBox.getName(), mailBox.getId()), e);
            }
        });

    }

    protected void syncMailBox(ImapMailBox mailBox) {
        getImapSynchronizer(mailBox).synchronize(mailBox);
        handleFolderMessages(mailBox);
    }

    protected ImapSynchronizer getImapSynchronizer(ImapMailBox mailBox) {
        return Boolean.TRUE.equals(mailBox.getFlagsSupported()) ? imapSynchronizer : imapFlaglessSynchronizer;
    }

    protected void handleFolderMessages(ImapMailBox imapMailBox) {
        log.trace("Handle folder messages of mailbox with id={}", imapMailBox.getId());
        ImapMailBox mailBox = imapDataProvider.findMailBox(imapMailBox.getId());
        if (mailBox == null) {
            log.trace("Mailbox with id={} not found", imapMailBox.getId());
            return;
        }
        mailBox.getProcessableFolders()
                .forEach(folder -> {
                            imapEvents.handleNewMessages(folder);
                            imapEvents.handleMissedMessages(folder);
                            imapEvents.handleChangedMessages(folder);
                        }
                );
    }
}

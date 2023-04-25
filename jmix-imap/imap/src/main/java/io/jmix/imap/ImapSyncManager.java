/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.imap;


import io.jmix.core.security.SystemAuthenticator;
import io.jmix.imap.data.ImapDataProvider;
import io.jmix.imap.entity.ImapMailBox;
import io.jmix.imap.sync.events.ImapEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nonnull;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component("imap_SyncManager")
public class ImapSyncManager {

    private final static Logger log = LoggerFactory.getLogger(ImapSyncManager.class);

    @Autowired
    protected ImapDataProvider imapDataProvider;
    @Autowired
    protected ImapEvents imapEvents;
    @Autowired
    protected SystemAuthenticator authentication;

    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
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


    @EventListener
    public void applicationStarted(ContextStartedEvent event) {
        authentication.begin();
        try {
            for (ImapMailBox mailBox : imapDataProvider.findMailBoxes()) {
                log.debug("{}: synchronizing", mailBox);
                CompletableFuture.runAsync(() -> imapEvents.init(mailBox), executor);
            }
        } finally {
            authentication.end();
        }
    }

    @EventListener
    public void applicationStopped(ContextStoppedEvent contextStoppedEvent) {
        try {
            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Exception while shutting down executor", e);
        }

        authentication.begin();
        try {
            for (ImapMailBox mailBox : imapDataProvider.findMailBoxes()) {
                try {
                    imapEvents.shutdown(mailBox);
                } catch (Exception e) {
                    log.warn("Exception while shutting down imapEvents for mailbox " + mailBox, e);
                }
            }
        } finally {
            authentication.end();
        }
    }
}

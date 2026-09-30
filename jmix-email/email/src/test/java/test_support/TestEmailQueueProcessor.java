/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.core.DataManager;
import io.jmix.email.Emailer;
import io.jmix.email.SendingStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

/**
 * Processes the email queue in tests and waits until the emails picked from it are sent or failed, since the
 * emailer sends them asynchronously on its own executor.
 */
public class TestEmailQueueProcessor {

    private static final long TIMEOUT_MS = 10_000;

    @Autowired
    protected Emailer emailer;

    @Autowired
    protected DataManager dataManager;

    /**
     * Calls {@link Emailer#processQueuedEmails()} and waits until none of the emails queued before the call
     * remains in the {@link SendingStatus#SENDING} status.
     *
     * @throws IllegalStateException if the emails are still being sent after the timeout
     */
    public void processQueuedEmailsAndWait() {
        List<UUID> queuedIds = dataManager.loadValues("select e.id from email_SendingMessage e where e.status = :status")
                .properties("id")
                .parameter("status", SendingStatus.QUEUE.getId())
                .list()
                .stream()
                .map(value -> value.<UUID>getValue("id"))
                .toList();

        emailer.processQueuedEmails();

        if (!queuedIds.isEmpty()) {
            awaitNotSending(queuedIds);
        }
    }

    private void awaitNotSending(List<UUID> ids) {
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (countSending(ids) > 0) {
            if (System.currentTimeMillis() > deadline) {
                throw new IllegalStateException("Queued emails are still being sent after " + TIMEOUT_MS + " ms");
            }
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for queued emails to be sent", e);
            }
        }
    }

    private long countSending(List<UUID> ids) {
        return dataManager.loadValue(
                        "select count(e) from email_SendingMessage e where e.id in :ids and e.status = :status",
                        Long.class)
                .parameter("ids", ids)
                .parameter("status", SendingStatus.SENDING.getId())
                .one();
    }
}

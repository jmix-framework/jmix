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

import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import io.jmix.multitenancy.event.TenantEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Records the tenant announcements the search module relies on.
 * <p>
 * Subscribes to the cluster channel rather than listening for a Spring event: the publisher forwards the message
 * back into the context only after {@code ApplicationStartedEvent}, which a plain test context never fires.
 */
public class TestTenantEventTracker {

    protected final List<TenantEvent> events = new CopyOnWriteArrayList<>();

    public TestTenantEventTracker(ClusterApplicationEventChannelSupplier channelSupplier) {
        channelSupplier.get().subscribe(message -> {
            if (message.getPayload() instanceof TenantEvent tenantEvent) {
                events.add(tenantEvent);
            }
        });
    }

    /**
     * Waits for an announcement to arrive: publishing goes through a channel and is not guaranteed to be
     * synchronous.
     */
    public List<String> awaitAnnouncedTenants(TenantEvent.Type type, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline && announced(type).isEmpty()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return announced(type);
    }

    protected List<String> announced(TenantEvent.Type type) {
        return events.stream()
                .filter(event -> event.getType() == type)
                .map(TenantEvent::getTenantId)
                .distinct()
                .toList();
    }

    public void clear() {
        events.clear();
    }
}

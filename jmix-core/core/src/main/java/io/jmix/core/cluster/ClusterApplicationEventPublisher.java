/*
 * Copyright 2023 Haulmont.
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

package io.jmix.core.cluster;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * A component that allows to publish application events to application instances of a cluster
 */
@Component("core_ClusterApplicationEventPublisher")
public class ClusterApplicationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ClusterApplicationEventPublisher.class);

    protected ApplicationEventPublisher applicationEventPublisher;
    protected ClusterApplicationEventChannelSupplier appEventChannelSupplier;

    public ClusterApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher,
                                            ClusterApplicationEventChannelSupplier appEventChannelSupplier) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.appEventChannelSupplier = appEventChannelSupplier;
    }

    @EventListener
    public void onApplicationStarted(ApplicationStartedEvent event) {
        appEventChannelSupplier.get().subscribe(this::onAppEventMessage);
    }

    protected void onAppEventMessage(Message<?> message) {
        ApplicationEvent event = (ApplicationEvent) message.getPayload();

        log.debug("Receiving event {}", event);

        applicationEventPublisher.publishEvent(event);
    }

    /**
     * Publishes an event cluster-wide. Note that, in general case, publishing will be asynchronous.
     * Also keep in mind the event will be published also in publishing app instance itself.
     *
     * @param event an event to publish
     */
    public void publish(ClusterApplicationEvent event) {
        if (appEventChannelSupplier != null) {
            Message<?> message = MessageBuilder.withPayload(event).build();

            log.debug("Publishing event {}", event);

            appEventChannelSupplier.get().send(message);
        }
    }
}

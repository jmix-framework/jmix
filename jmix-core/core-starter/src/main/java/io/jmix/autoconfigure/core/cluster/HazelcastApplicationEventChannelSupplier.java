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

package io.jmix.autoconfigure.core.cluster;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import io.jmix.core.cluster.ClusterApplicationEventChannelSupplier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

/**
 * Provides a channel that publishes messages to all application instances in a cluster using Hazelcast topics.
 */
public class HazelcastApplicationEventChannelSupplier implements ClusterApplicationEventChannelSupplier {

    protected static final String TOPIC_NAME = "jmix-cluster-application-event-topic";

    protected final SubscribableChannel messageChannel;

    public HazelcastApplicationEventChannelSupplier(HazelcastInstance hazelcastInstance) {
        ITopic<Message<?>> topic = hazelcastInstance.getTopic(TOPIC_NAME);
        this.messageChannel = new HazelcastMessageChannel(topic);
    }

    @Override
    public SubscribableChannel get() {
        return messageChannel;
    }

    protected static class HazelcastMessageChannel implements SubscribableChannel {

        protected ITopic<Message<?>> topic;

        public HazelcastMessageChannel(ITopic<Message<?>> topic) {
            this.topic = topic;
        }

        @Override
        public boolean subscribe(MessageHandler handler) {
            topic.addMessageListener(message -> handler.handleMessage(message.getMessageObject()));
            return true;
        }

        @Override
        public boolean unsubscribe(MessageHandler handler) {
            return false;
        }

        @Override
        public boolean send(Message<?> message) {
            topic.publish(message);
            return true;
        }

        @Override
        public boolean send(Message<?> message, long timeout) {
            topic.publish(message);
            return true;
        }
    }
}

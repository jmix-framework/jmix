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

package io.jmix.autoconfigure.flowui.cluster;

import com.hazelcast.cluster.Member;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.topic.ITopic;
import io.jmix.flowui.sys.cluster.AppEventSubscribableChannelSupplier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

public class HazelcastAppEventSubscribableChannelSupplier implements AppEventSubscribableChannelSupplier {

    protected final SubscribableChannel messageChannel;

    protected static class HazelcastMessageChannel implements SubscribableChannel {

        protected ITopic<Message<?>> topic;

        public HazelcastMessageChannel(ITopic<Message<?>> topic) {
            this.topic = topic;
        }

        @Override
        public boolean subscribe(MessageHandler handler) {
            topic.addMessageListener(message -> {
                Member publishingMember = message.getPublishingMember();
                //avoid to process own messages
                if (publishingMember == null || !publishingMember.localMember()) {
                    handler.handleMessage(message.getMessageObject());
                }
            });
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

    public HazelcastAppEventSubscribableChannelSupplier(HazelcastInstance hazelcastInstance) {
        ITopic<Message<?>> topic = hazelcastInstance.getTopic("jmix-app-event-topic");
        this.messageChannel = new HazelcastMessageChannel(topic);
    }

    @Override
    public SubscribableChannel get() {
        return messageChannel;
    }
}

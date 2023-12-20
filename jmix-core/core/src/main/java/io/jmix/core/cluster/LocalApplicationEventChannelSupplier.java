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

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a channel that publishes messages only to owner application instance
 * which is used in single-instance applications.
 */
public class LocalApplicationEventChannelSupplier implements ClusterApplicationEventChannelSupplier {

    protected static SubscribableChannel LOCAL_CHANNEL = new SubscribableChannel() {

        private List<MessageHandler> handlers;

        @Override
        public boolean subscribe(MessageHandler handler) {
            if (handlers == null) {
                handlers = new ArrayList<>();
            }
            return handlers.add(handler);
        }

        @Override
        public boolean unsubscribe(MessageHandler handler) {
            return handlers != null && handlers.remove(handler);
        }

        @Override
        public boolean send(Message<?> message, long timeout) {
            if (handlers != null) {
                handlers.forEach(handler -> handler.handleMessage(message));
            }
            return true;
        }
    };

    @Override
    public SubscribableChannel get() {
        return LOCAL_CHANNEL;
    }
}

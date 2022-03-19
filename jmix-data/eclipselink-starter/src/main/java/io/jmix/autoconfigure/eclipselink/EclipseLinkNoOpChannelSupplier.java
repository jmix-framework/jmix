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

package io.jmix.autoconfigure.eclipselink;

import io.jmix.eclipselink.impl.support.EclipseLinkChannelSupplier;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.SubscribableChannel;

public class EclipseLinkNoOpChannelSupplier implements EclipseLinkChannelSupplier {

    private static final SubscribableChannel NO_OP_CHANNEL = new SubscribableChannel() {
        @Override
        public boolean subscribe(MessageHandler handler) {
            return true;
        }

        @Override
        public boolean unsubscribe(MessageHandler handler) {
            return true;
        }

        @Override
        public boolean send(Message<?> message, long timeout) {
            return true;
        }
    };

    @Override
    public SubscribableChannel get() {
        return NO_OP_CHANNEL;
    }
}

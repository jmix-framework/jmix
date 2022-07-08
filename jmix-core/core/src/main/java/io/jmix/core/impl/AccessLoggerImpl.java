/*
 * Copyright 2022 Haulmont.
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

package io.jmix.core.impl;

import io.jmix.core.accesscontext.AccessContext;
import io.jmix.core.constraint.AccessConstraint;
import io.jmix.core.AccessLogger;
import io.jmix.core.constraint.RowLevelConstraint;
import io.jmix.core.security.CurrentAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("core_AccessLogger")
public class AccessLoggerImpl implements AccessLogger {

    private static final Logger log = LoggerFactory.getLogger(AccessLogger.class);

    @Autowired
    private CurrentAuthentication currentAuthentication;

    private ThreadLocal<LastMessage> lastMessageThreadLocal = new ThreadLocal<>();

    @Override
    public <T extends AccessContext> void log(AccessConstraint<T> constraint, T accessContext) {
        if (!log.isDebugEnabled()) {
            return;
        }
        String explanation = accessContext.explainConstraints();
        if (explanation != null) {
            String prefix = constraint instanceof RowLevelConstraint ? "Applied row-level constraint" : "Denied access";
            String message = prefix + " to [" + explanation + "]"
                    + " for user [" + currentAuthentication.getUser().getUsername() + "]"
                    + " by " + constraint.getClass().getName();

            LastMessage lastMessage = lastMessageThreadLocal.get();
            if (lastMessage == null) {
                lastMessage = new LastMessage();
                lastMessageThreadLocal.set(lastMessage);
            }
            if (!message.equals(lastMessage.message) || someTimePassed()) {
                log.debug(message);
                lastMessage.message = message;
                lastMessage.timestamp = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void reset() {
        lastMessageThreadLocal.remove();
    }

    private boolean someTimePassed() {
        return (System.currentTimeMillis() - lastMessageThreadLocal.get().timestamp) > 10;
    }

    private static class LastMessage {
        String message;
        long timestamp;
    }
}

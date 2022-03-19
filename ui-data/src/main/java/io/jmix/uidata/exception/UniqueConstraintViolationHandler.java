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

package io.jmix.uidata.exception;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.data.exception.UniqueConstraintViolationException;
import io.jmix.ui.Notifications;
import io.jmix.ui.exception.UiExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Handles database unique constraint violations. Determines the exception type by searching a special marker string
 * in the messages of all exceptions in the chain.
 */
@Component("ui_UniqueConstraintViolationHandler")
public class UniqueConstraintViolationHandler implements UiExceptionHandler, Ordered {

    private static final Logger log = LoggerFactory.getLogger(UniqueConstraintViolationHandler.class);

    protected static final String DEFAULT_MESSAGE_PROPERTY = "uniqueConstraintViolation.message";
    protected static final String MESSAGE_PREFIX = "databaseUniqueConstraintViolation.";

    @Autowired
    protected Messages messages;

    @Override
    public boolean handle(Throwable exception, UiContext context) {
        Throwable throwable = exception;
        try {
            while (throwable != null) {
                if (throwable instanceof UniqueConstraintViolationException) {
                    return doHandle((UniqueConstraintViolationException) throwable, context);
                }
                throwable = throwable.getCause();
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean doHandle(UniqueConstraintViolationException exception, UiContext context) {
        String constraintName = exception.getConstraintName();
        log.debug("handle: UniqueConstraintViolationException with constraintName={}", constraintName);
        String message = getMessage(constraintName);
        context.getNotifications()
                .create(Notifications.NotificationType.ERROR)
                .withDescription(message)
                .show();
        return true;
    }

    protected String getMessage(String constraintName) {
        String messageKey = MESSAGE_PREFIX + constraintName;
        return Optional.ofNullable(messages.findMessage(messageKey, null))
                .orElseGet(() -> getDefaultMessage(constraintName));
    }

    protected String getDefaultMessage(String constraintName) {
        String msg = messages.getMessage(DEFAULT_MESSAGE_PROPERTY);
        if (StringUtils.isNotBlank(constraintName)) {
            msg = msg + " (" + constraintName + ")";
        }
        return msg;
    }

    @Override
    public int getOrder() {
        return JmixOrder.HIGHEST_PRECEDENCE + 60;
    }
}

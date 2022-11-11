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

package io.jmix.flowuidata.exception;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.exception.AbstractUiExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("flowui_OptimisticLockExceptionHandler")
@Order(JmixOrder.HIGHEST_PRECEDENCE + 50)
public class OptimisticLockExceptionHandler extends AbstractUiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(OptimisticLockExceptionHandler.class);

    Messages messages;
    Notifications notifications;

    public OptimisticLockExceptionHandler(Messages messages, Notifications notifications) {
        super(org.eclipse.persistence.exceptions.OptimisticLockException.class.getName(),
                javax.persistence.OptimisticLockException.class.getName());
        this.messages = messages;
        this.notifications = notifications;
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable) {
        log.debug("Optimistic lock: {}", throwable != null ? throwable.toString() : message);

        Pattern pattern = Pattern.compile("Class> (.+)");
        Matcher matcher = pattern.matcher(message);
        String entityClassName = "";
        if (matcher.find()) {
            entityClassName = matcher.group(1);
        }

        String exceptionTitle;

        if (entityClassName.contains(".")) {
            String entityName = entityClassName.substring(entityClassName.lastIndexOf(".") + 1);
            String packageName = entityClassName.substring(0, entityClassName.lastIndexOf("."));
            String localizedEntityName = messages.getMessage(packageName, entityName);

            exceptionTitle = messages.formatMessage(getClass(), "optimisticLock.exceptionTitle", localizedEntityName);
        } else {
            exceptionTitle = messages.getMessage(getClass(), "optimisticLockUnknownObject.message");
        }
        String exceptionMessage = messages.getMessage(getClass(), "optimisticLock.exceptionMessage");

        notifications.create(exceptionTitle, exceptionMessage)
                .withType(Notifications.Type.ERROR)
                .show();
    }
}

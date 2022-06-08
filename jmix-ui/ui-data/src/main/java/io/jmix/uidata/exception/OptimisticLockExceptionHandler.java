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

package io.jmix.uidata.exception;

import io.jmix.core.JmixOrder;
import io.jmix.core.Messages;
import io.jmix.ui.Notifications;
import io.jmix.ui.exception.AbstractUiExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("ui_OptimisticLockExceptionHandler")
@Order(JmixOrder.HIGHEST_PRECEDENCE + 50)
public class OptimisticLockExceptionHandler extends AbstractUiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(OptimisticLockExceptionHandler.class);

    @Autowired
    protected Messages messages;

    public OptimisticLockExceptionHandler() {
        super(org.eclipse.persistence.exceptions.OptimisticLockException.class.getName(),
                javax.persistence.OptimisticLockException.class.getName());
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable, UiContext context) {
        log.debug("Optimistic lock: {}", throwable != null ? throwable.toString() : message);

        Pattern pattern = Pattern.compile("Class> (.+)");
        Matcher matcher = pattern.matcher(message);
        String entityClassName = "";
        if (matcher.find()) {
            entityClassName = matcher.group(1);
        }

        String msg;

        if (entityClassName.contains(".")) {
            String entityName = entityClassName.substring(entityClassName.lastIndexOf(".") + 1);
            String packageName = entityClassName.substring(0, entityClassName.lastIndexOf("."));
            String localizedEntityName = messages.getMessage(packageName, entityName);

            msg = messages.formatMessage(getClass(), "optimisticLock.message", localizedEntityName);
        } else {
            msg = messages.getMessage(getClass(), "optimisticLockUnknownObject.message");
        }
        String description = messages.getMessage(getClass(), "optimisticLock.description");

        context.getNotifications().create(Notifications.NotificationType.ERROR)
                .withCaption(msg)
                .withDescription(description)
                .show();
    }
}

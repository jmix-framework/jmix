/*
 * Copyright 2021 Haulmont.
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

package io.jmix.securityui.impl.handler;

import io.jmix.core.Messages;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.ui.Notifications;
import io.jmix.ui.exception.AbstractUiExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("sec_AccessDeniedExceptionHandler")
public class AccessDeniedExceptionHandler extends AbstractUiExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(AccessDeniedExceptionHandler.class);

    private final Messages messages;

    public AccessDeniedExceptionHandler(Messages messages) {
        super(AccessDeniedException.class.getName());
        this.messages = messages;
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable, UiContext context) {
        context.getNotifications().create(Notifications.NotificationType.ERROR)
                .withCaption(messages.getMessage("accessDenied.message"))
                .show();
        LOG.error(message);
    }
}

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

package io.jmix.simplesecurityflowui.exception;

import io.jmix.core.Messages;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.exception.AbstractUiExceptionHandler;
import org.springframework.stereotype.Component;

//todo MG move to some shared security module?
/**
 * Handles AccessDeniedExceptionHandler that can be thrown by checking access to the dialog.
 * Displays violation messages as on-screen notifications.
 */
public class AccessDeniedExceptionHandler extends AbstractUiExceptionHandler {

    protected final Notifications notifications;
    protected final Messages messages;

    public AccessDeniedExceptionHandler(Notifications notifications,
                                        Messages messages) {
        super(AccessDeniedException.class.getName());

        this.notifications = notifications;
        this.messages = messages;
    }

    @Override
    protected void doHandle(String className, String message, Throwable throwable) {
        notifications.create(messages.getMessage(getClass(), "accessDeniedError.title"))
                .withType(Notifications.Type.ERROR)
                .show();
    }
}

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
package io.jmix.reportsui.exception;

import com.vaadin.spring.annotation.UIScope;
import io.jmix.core.Messages;
import io.jmix.reports.exception.*;
import io.jmix.ui.Notifications;
import io.jmix.ui.exception.AbstractUiExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Handles reporting exceptions.
 */
@UIScope
@Component("report_ReportExceptionHandler")
public class ReportExceptionHandler extends AbstractUiExceptionHandler {

    @Autowired
    protected Messages messages;

    public ReportExceptionHandler() {
        super(
                ReportingException.class.getName(),
                NoOpenOfficeFreePortsException.class.getName(),
                FailedToConnectToOpenOfficeException.class.getName(),
                UnsupportedFormatException.class.getName(),
                FailedToLoadTemplateClassException.class.getName(),
                ValidationException.class.getName()
        );
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable, UiContext context) {
        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            context.getNotifications().create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(getClass(), "reportException.failedConnectToOffice"))
                    .show();
        } else if (NoOpenOfficeFreePortsException.class.getName().equals(className)) {
            context.getNotifications().create(Notifications.NotificationType.ERROR)
                    .withCaption(messages.getMessage(getClass(), "reportException.noOpenOfficeFreePorts"))
                    .show();
        } else if (ValidationException.class.getName().equals(className)) {
            context.getNotifications().create(Notifications.NotificationType.ERROR)
                    .withCaption(message)
                    .show();
        } else {
            context.getDialogs().createExceptionDialog()
                    .withThrowable(throwable)
                    .withCaption(messages.getMessage(getClass(), "reportException.message"))
                    .withMessage(message)
                    .show();
        }
    }
}
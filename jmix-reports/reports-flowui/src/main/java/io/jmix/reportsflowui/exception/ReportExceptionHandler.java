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
package io.jmix.reportsflowui.exception;

import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.exception.AbstractUiExceptionHandler;
import io.jmix.flowui.exception.ExceptionDialog;
import io.jmix.flowui.sys.BeanUtil;
import io.jmix.reports.exception.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 * Handles reporting exceptions.
 */
@Component("report_ReportExceptionHandler")
public class ReportExceptionHandler extends AbstractUiExceptionHandler {

    protected final Messages messages;
    protected final Notifications notifications;
    protected final ApplicationContext applicationContext;

    public ReportExceptionHandler(Messages messages,
                                  Notifications notifications,
                                  ApplicationContext applicationContext) {
        super(
                ReportingException.class.getName(),
                NoOpenOfficeFreePortsException.class.getName(),
                FailedToConnectToOpenOfficeException.class.getName(),
                UnsupportedFormatException.class.getName(),
                FailedToLoadTemplateClassException.class.getName(),
                ValidationException.class.getName()
        );

        this.messages = messages;
        this.notifications = notifications;
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doHandle(String className, String message, @Nullable Throwable throwable) {
        if (FailedToConnectToOpenOfficeException.class.getName().equals(className)) {
            notifications.create(messages.getMessage(getClass(), "reportException.failedConnectToOffice"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        } else if (NoOpenOfficeFreePortsException.class.getName().equals(className)) {
            notifications.create(messages.getMessage(getClass(), "reportException.noOpenOfficeFreePorts"))
                    .withType(Notifications.Type.ERROR)
                    .show();
        } else {
            Throwable rootCause = ExceptionUtils.getRootCause(throwable);
            if (rootCause == null) {
                rootCause = throwable;
            }

            ExceptionDialog exceptionDialog = new ExceptionDialog(rootCause);
            BeanUtil.autowireContext(applicationContext, exceptionDialog);
            exceptionDialog.open();
        }
    }
}
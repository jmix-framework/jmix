/*
 * Copyright (c) 2008-2019 Haulmont.
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
package io.jmix.reportsui.web.exception;

import com.haulmont.cuba.core.global.Messages;
import com.vaadin.ui.Window;
import io.jmix.reports.exception.*;
import io.jmix.ui.AppUI;
import io.jmix.ui.exception.AbstractUiExceptionHandler;
import io.jmix.ui.widget.ExceptionDialog;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;

/**
 * Handles reporting exceptions.
 */
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
            String msg = messages.getMessage(getClass(), "reportException.failedConnectToOffice");
            //TODO show notifications
//            App.getInstance().getWindowManager().showNotification(msg, Frame.NotificationType.ERROR);
        } else if (NoOpenOfficeFreePortsException.class.getName().equals(className)) {
            String msg = messages.getMessage(getClass(), "reportException.noOpenOfficeFreePorts");
            //TODO show notifications
//            app.getWindowManager().showNotification(msg, Frame.NotificationType.ERROR);
        } else if (ValidationException.class.getName().equals(className)) {
            //TODO show notifications
//            app.getWindowManager().showNotification(message, Frame.NotificationType.ERROR);
        } else {
            ExceptionDialog dialog = new ExceptionDialog(
                    throwable,
                    messages.getMessage(getClass(), "reportException.message"),
                    message,
                    //TODO get app context
                    null
            );
            for (Window window : AppUI.getCurrent().getWindows()) {
                if (window.isModal()) {
                    dialog.setModal(true);
                    break;
                }
            }
            AppUI.getCurrent().addWindow(dialog);
            dialog.focus();
        }
    }
}
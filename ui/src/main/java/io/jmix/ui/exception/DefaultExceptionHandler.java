/*
 * Copyright 2019 Haulmont.
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
package io.jmix.ui.exception;

import com.vaadin.server.ErrorEvent;
import com.vaadin.ui.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import io.jmix.core.Messages;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.ui.App;
import io.jmix.ui.AppUI;
import io.jmix.ui.Notifications;
import io.jmix.ui.widget.ExceptionDialog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.net.SocketException;

/**
 * This exception handler comes into play if no other handler in the chain has handled the exception.
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    protected ApplicationContext applicationContext;

    protected Messages messages;

    public DefaultExceptionHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        messages = applicationContext.getBean(Messages.class);
    }

    @Override
    public boolean handle(ErrorEvent event, App app) {
        // Copied from com.vaadin.server.DefaultErrorHandler.doDefault()

        Throwable t = event.getThrowable();
        if (t instanceof SocketException
                || ExceptionUtils.getRootCause(t) instanceof SocketException) {
            // Most likely client browser closed socket
            return true;
        }

        // if it is UberJar or deployed to Jetty
        if (ExceptionUtils.getThrowableList(t).stream()
                .anyMatch(o -> o.getClass().getName().equals("org.eclipse.jetty.io.EofException"))) {
            // Most likely client browser closed socket
            return true;
        }

        // Support Tomcat 8 ClientAbortException
        if (StringUtils.contains(ExceptionUtils.getMessage(t), "ClientAbortException")) {
            // Most likely client browser closed socket
            return true;
        }

        AppUI ui = AppUI.getCurrent();
        if (ui == null) {
            // there is no UI, just add error to log
            return true;
        }

        if (t != null) {
            log.error("Unhandled exception", t);

            //todo MG
            if (SecurityContextHelper.getAuthentication() != null) {
                showDialog(app, ui, t);
            } else {
                showNotification(app, ui, t);
            }
        }

        // default handler always return true
        return true;
    }

    protected void showDialog(App app, AppUI ui, Throwable exception) {
        Throwable rootCause = ExceptionUtils.getRootCause(exception);
        if (rootCause == null) {
            rootCause = exception;
        }
        ExceptionDialog dialog = new ExceptionDialog(rootCause, applicationContext);
        for (Window window : ui.getWindows()) {
            if (window.isModal()) {
                dialog.setModal(true);
                break;
            }
        }
        ui.addWindow(dialog);
        dialog.focus();
    }

    protected void showNotification(App app, AppUI ui, Throwable exception) {
        Throwable rootCause = ExceptionUtils.getRootCause(exception);
        if (rootCause == null) {
            rootCause = exception;
        }

        String message = rootCause.getClass().getSimpleName() +
                (rootCause.getMessage() != null ? "\n" + rootCause.getMessage() : "");

        ui.getNotifications().create(Notifications.NotificationType.ERROR)
                .withCaption(messages.getMessage("exceptionDialog.caption", app.getLocale()))
                .withDescription(message)
                .show();
    }
}
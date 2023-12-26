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

package io.jmix.ui.executor;

import io.jmix.core.Messages;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.Screens;
import io.jmix.ui.screen.Screen;
import io.jmix.ui.screen.ScreenContext;
import io.jmix.ui.screen.UiControllerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class LocalizedTaskWrapper<T, V> extends BackgroundTask<T, V> {

    private static final Logger log = LoggerFactory.getLogger(BackgroundWorker.class);

    protected BackgroundTask<T, V> wrappedTask;
    protected Screen screen;
    protected Messages messages;

    public LocalizedTaskWrapper(BackgroundTask<T, V> wrappedTask, Screen screen, Messages messages) {
        super(wrappedTask.getTimeoutSeconds(), screen);
        this.wrappedTask = wrappedTask;
        this.screen = screen;
        this.messages = messages;

        wrappedTask.getProgressListeners().forEach(this::addProgressListener);
    }

    @Override
    public Map<String, Object> getParams() {
        return wrappedTask.getParams();
    }

    @Override
    public V run(TaskLifeCycle<T> lifeCycle) throws Exception {
        return wrappedTask.run(lifeCycle);
    }

    @Override
    public boolean handleException(Exception ex) {
        boolean handled = wrappedTask.handleException(ex);

        if (handled || wrappedTask.getOwnerScreen() == null) {
            Screens screens = getScreenContext().getScreens();
            screens.remove(screen);
        } else {
            Screens screens = getScreenContext().getScreens();
            screens.remove(screen);

            showExecutionError(ex);

            log.error("Exception occurred in background task", ex);

            handled = true;
        }
        return handled;
    }

    @Override
    public boolean handleTimeoutException() {
        boolean handled = wrappedTask.handleTimeoutException();
        if (handled || wrappedTask.getOwnerScreen() == null) {
            Screens screens = getScreenContext().getScreens();
            screens.remove(screen);
        } else {
            Screens screens = getScreenContext().getScreens();
            screens.remove(screen);

            Notifications notifications = getScreenContext().getNotifications();

            notifications.create(Notifications.NotificationType.WARNING)
                    .withCaption(messages.getMessage("backgroundWorkProgress.timeout"))
                    .withDescription(messages.getMessage("backgroundWorkProgress.timeoutMessage"))
                    .show();

            handled = true;
        }
        return handled;
    }

    @Override
    public void done(V result) {
        Screens screens = getScreenContext().getScreens();
        screens.remove(screen);

        try {
            wrappedTask.done(result);
        } catch (Exception ex) {
            // we should show exception messages immediately
            showExecutionError(ex);
        }
    }

    @Override
    public void canceled() {
        try {
            wrappedTask.canceled();
        } catch (Exception ex) {
            // we should show exception messages immediately
            showExecutionError(ex);
        }
    }

    @Override
    public void progress(List<T> changes) {
        wrappedTask.progress(changes);
    }

    protected ScreenContext getScreenContext() {
        return UiControllerUtils.getScreenContext(screen);
    }

    protected void showExecutionError(Exception ex) {
        Screen ownerFrame = wrappedTask.getOwnerScreen();
        if (ownerFrame != null) {
            Dialogs dialogs = getScreenContext().getDialogs();

            dialogs.createExceptionDialog()
                    .withThrowable(ex)
                    .withCaption(messages.getMessage("backgroundWorkProgress.executionError"))
                    .withMessage(ex.getLocalizedMessage())
                    .show();
        }
    }
}
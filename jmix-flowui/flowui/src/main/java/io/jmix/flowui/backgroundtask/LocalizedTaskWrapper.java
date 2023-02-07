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

package io.jmix.flowui.backgroundtask;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dialog.Dialog;
import io.jmix.core.Messages;
import io.jmix.core.annotation.Internal;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.impl.DialogsImpl;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * INTERNAL!
 * Supposed to use when showing {@link Dialog} or some {@link View}. For instance
 * if we need to show some dialog when task is running (e.g. updating the progress).
 * <p>
 * See example in {@link DialogsImpl.BackgroundTaskDialogBuilderImpl}.
 *
 * @param <T> task progress measurement unit
 * @param <V> result type
 */
@Internal
@Component("flowui_LocalizedTaskWrapper")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class LocalizedTaskWrapper<T, V> extends BackgroundTask<T, V> {

    private static final Logger log = LoggerFactory.getLogger(BackgroundWorker.class);

    protected Messages messages;
    protected Notifications notifications;

    protected BackgroundTask<T, V> wrappedTask;
    protected Consumer<CloseViewContext> closeViewHandler;

    public LocalizedTaskWrapper(BackgroundTask<T, V> wrappedTask) {
        super(wrappedTask.getTimeoutSeconds());
        this.wrappedTask = wrappedTask;
    }

    @Autowired
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Autowired
    public void setNotifications(Notifications notifications) {
        this.notifications = notifications;
    }

    @Nullable
    public Consumer<CloseViewContext> getCloseViewHandler() {
        return closeViewHandler;
    }

    public void setCloseViewHandler(@Nullable Consumer<CloseViewContext> closeViewHandler) {
        this.closeViewHandler = closeViewHandler;
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

        notifyCloseViewHandler();

        if (!handled && wrappedTask.getOwnerView() != null) {
            showExecutionError(ex);

            log.error("Exception occurred in background task", ex);

            handled = true;
        }
        return handled;
    }

    @Override
    public boolean handleTimeoutException() {
        boolean handled = wrappedTask.handleTimeoutException();

        notifyCloseViewHandler();

        if (!handled && wrappedTask.getOwnerView() != null) {
            if (UI.getCurrent() != null) {
                notifications.create(
                                messages.getMessage("localizedTaskWrapper.timeout.notification.title"),
                                messages.getMessage("localizedTaskWrapper.timeout.notification.message"))
                        .withType(Notifications.Type.WARNING)
                        .show();
            }
            handled = true;
        }
        return handled;
    }

    @Override
    public void done(V result) {
        notifyCloseViewHandler();

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

    protected void showExecutionError(Exception ex) {
        View<?> ownerView = wrappedTask.getOwnerView();
        if (ownerView != null && UI.getCurrent() != null) {
            notifications.create(messages.getMessage("localizedTaskWrapper.executionError.message"))
                    .withType(Notifications.Type.ERROR)
                    .show();

            /* todo ExceptionDialog */
            /*Dialogs dialogs = Instantiator.get(UI.getCurrent()).getOrCreate(Dialogs.class);
            dialogs.createExceptionDialog()
                    .withThrowable(ex)
                    .withCaption(messages.getMessage("localizedTaskWrapper.executionError.message"))
                    .withMessage(ex.getLocalizedMessage())
                    .show();*/
        }
    }

    protected void notifyCloseViewHandler() {
        if (closeViewHandler != null) {
            closeViewHandler.accept(new CloseViewContext(this));
        }
    }

    public static class CloseViewContext {

        protected LocalizedTaskWrapper taskWrapper;

        public CloseViewContext(LocalizedTaskWrapper taskWrapper) {
            this.taskWrapper = taskWrapper;
        }

        public <T, V> LocalizedTaskWrapper<T, V> getSource() {
            return taskWrapper;
        }
    }
}
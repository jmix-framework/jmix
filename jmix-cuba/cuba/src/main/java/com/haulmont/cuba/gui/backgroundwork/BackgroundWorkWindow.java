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

package com.haulmont.cuba.gui.backgroundwork;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.WindowManager;

import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.AbstractWindow;

import com.haulmont.cuba.gui.components.Label;

import io.jmix.ui.Screens;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.executor.LocalizedTaskWrapper;
import io.jmix.ui.component.Button;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Modal window wrapping around background task. Displays title, message and optional cancel button.
 * Window lasts until task completes, timeout timer elapses or user presses cancel button.
 * <br>
 * When cancelled by user, does not interrupt task thread.
 * <br>
 * <p>On error:
 * <ul>
 * <li>Executes handle exception in background task</li>
 * <li>Closes background window</li>
 * <li>Shows Warning message if for background task specified owner window</li>
 * </ul>
 * <br>
 *
 * @deprecated use only in legacy CUBA code. In new code, use {@link io.jmix.ui.app.backgroundwork.BackgroundWorkDialog}.
 */
@Deprecated
public class BackgroundWorkWindow<T, V> extends AbstractWindow {

    @Autowired
    protected Label<String> text;
    @Autowired
    protected Button cancelButton;
    @Autowired
    protected BackgroundWorker backgroundWorker;

    protected BackgroundTaskHandler<V> taskHandler;
    protected boolean cancelAllowed = false;

    /**
     * Show modal window with message which will last until task completes.
     * Optionally cancel button can be displayed. By pressing cancel button user can cancel task execution.
     *
     * @param task          background task containing long operation
     * @param title         window title, optional
     * @param message       window message, optional
     * @param cancelAllowed show or not cancel button
     * @param <T>           task progress unit
     * @param <V>           task result type
     */
    public static <T, V> void show(BackgroundTask<T, V> task, @Nullable String title, @Nullable String message,
                                   boolean cancelAllowed) {
        if (task.getOwnerScreen() == null) {
            throw new IllegalArgumentException("Task without owner cannot be run");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("task", task);
        params.put("title", title);
        params.put("message", message);
        params.put("cancelAllowed", cancelAllowed);

        Screens screens = UiControllerUtils.getScreenContext(task.getOwnerScreen()).getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("backgroundWorkWindow");
        ((WindowManager)screens).openWindow(windowInfo, OpenType.DIALOG, params);
    }

    /**
     * Show modal window with message which will last until task completes.
     * Cancel button is not shown.
     *
     * @param task    background task containing long operation
     * @param title   window title, optional
     * @param message window message, optional
     * @param <T>     task progress unit
     * @param <V>     task result type
     */
    public static <T, V> void show(BackgroundTask<T, V> task, String title, String message) {
        show(task, title, message, false);
    }

    /**
     * Show modal window with default title and message which will last until task completes.
     * Cancel button is not shown.
     *
     * @param task          background task containing long operation
     * @param cancelAllowed show or not cancel button
     * @param <T>           task progress unit
     * @param <V>           task result type
     */
    public static <T, V> void show(BackgroundTask<T, V> task, boolean cancelAllowed) {
        show(task, null, null, cancelAllowed);
    }

    /**
     * Show modal window with default title and message which will last until task completes.
     * Cancel button is not shown.
     *
     * @param task background task containing long operation
     * @param <T>  task progress unit
     * @param <V>  task result type
     */
    public static <T, V> void show(BackgroundTask<T, V> task) {
        show(task, null, null, false);
    }

    @Override
    public void init(Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        BackgroundTask<T, V> task = (BackgroundTask<T, V>) params.get("task");
        String title = (String) params.get("title");
        if (title != null) {
            setCaption(title);
        }

        String message = (String) params.get("message");
        if (message != null) {
            text.setValue(message);
        }

        Boolean cancelAllowedNullable = (Boolean) params.get("cancelAllowed");
        cancelAllowed = BooleanUtils.isTrue(cancelAllowedNullable);
        cancelButton.setVisible(cancelAllowed);

        getWindow().setCloseable(cancelAllowed);

        BackgroundTask<T, V> wrapperTask = new LocalizedTaskWrapper<>(task, this, messages);

        taskHandler = backgroundWorker.handle(wrapperTask);
        taskHandler.execute();
    }

    public void cancel() {
        close(WINDOW_CLOSE_ACTION);
    }

    @Override
    protected boolean preClose(String actionId) {
        if (!taskHandler.cancel()) {
            return false;
        }
        return super.preClose(actionId);
    }
}
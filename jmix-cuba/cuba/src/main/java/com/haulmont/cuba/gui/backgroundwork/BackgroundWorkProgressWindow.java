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
import io.jmix.core.Messages;
import io.jmix.ui.Screens;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.WindowInfo;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.ProgressBar;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.executor.LocalizedTaskWrapper;
import io.jmix.ui.screen.UiControllerUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Window that indicates progress of the background task, shows progress bar and processed items' message.
 * <br>
 * Background task should have &lt;T extends Number&gt; as the progress measure unit. Progress measure passed to the publish() method
 * is displayed in processed items'/percents' message. Total number of items should be specified before task execution.
 * <br>
 * <p>On error:
 * <ul>
 * <li>Executes handle exception in background task</li>
 * <li>Closes background window</li>
 * <li>Shows Warning message if for background task specified owner window</li>
 * </ul>
 * <br>
 *
 * @param <T> measure unit which shows progress of task
 * @param <V> result type
 */
public class BackgroundWorkProgressWindow<T extends Number, V> extends AbstractWindow {

    @Autowired
    protected Label<String> text;
    @Autowired
    protected Label<String> progressText;
    @Autowired
    protected Button cancelButton;
    @Autowired
    protected ProgressBar taskProgressBar;

    @Autowired
    protected BackgroundWorker backgroundWorker;

    @Autowired
    protected Messages messages;

    protected BackgroundTaskHandler<V> taskHandler;
    protected boolean cancelAllowed = false;

    protected T totalProgress;
    protected boolean percentProgress;

    /**
     * Show modal window with message which will last until task completes.
     * Optionally cancel button can be displayed. By pressing cancel button user can cancel task execution.
     *
     * @param task            background task containing long operation
     * @param title           window title, optional
     * @param message         window message, optional
     * @param total           total number of items, that will be processed
     * @param cancelAllowed   show or not cancel button
     * @param percentProgress show progress in percents
     * @param <V>             task result type
     */
    public static <T extends Number, V> void show(BackgroundTask<T, V> task, @Nullable String title, @Nullable String message,
                                                  Number total, boolean cancelAllowed, boolean percentProgress) {
        if (task.getOwnerScreen() == null) {
            throw new IllegalArgumentException("Task without owner cannot be run");
        }

        Map<String, Object> params = new HashMap<>();
        params.put("task", task);
        params.put("title", title);
        params.put("message", message);
        params.put("total", total);
        params.put("cancelAllowed", cancelAllowed);
        params.put("percentProgress", percentProgress);

        Screens screens = UiControllerUtils.getScreenContext(task.getOwnerScreen()).getScreens();
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo("backgroundWorkProgressWindow");
        ((WindowManager)screens).openWindow(windowInfo, OpenType.DIALOG, params);
    }

    /**
     * Show modal window with message which will last until task completes.
     * Optionally cancel button can be displayed. By pressing cancel button user can cancel task execution.
     *
     * @param task          background task containing long operation
     * @param title         window title, optional
     * @param message       window message, optional
     * @param total         total number of items, that will be processed
     * @param cancelAllowed show or not cancel button
     * @param <V>           task result type
     */
    public static <T extends Number, V> void show(BackgroundTask<T, V> task, @Nullable String title, @Nullable String message,
                                                  Number total, boolean cancelAllowed) {
        show(task, title, message, total, cancelAllowed, false);
    }

    /**
     * Show modal window with message which will last until task completes.
     * Cancel button is not shown.
     *
     * @param task    background task containing long operation
     * @param title   window title, optional
     * @param message window message, optional
     * @param total   total number of items, that will be processed
     * @param <V>     task result type
     */
    public static <T extends Number, V> void show(BackgroundTask<T, V> task, String title, String message, Number total) {
        show(task, title, message, total, false);
    }

    /**
     * Show modal window with default title and message which will last until task completes.
     * Cancel button is not shown.
     *
     * @param task          background task containing long operation
     * @param total         total number of items, that will be processed
     * @param cancelAllowed show or not cancel button
     * @param <V>           task result type
     */
    public static <T extends Number, V> void show(BackgroundTask<T, V> task, Number total, boolean cancelAllowed) {
        show(task, null, null, total, cancelAllowed);
    }

    /**
     * Show modal window with default title and message which will last until task completes.
     * Cancel button is not shown.
     *
     * @param task  background task containing long operation
     * @param total total number of items, that will be processed
     * @param <V>   task result type
     */
    public static <T extends Number, V> void show(BackgroundTask<T, V> task, Number total) {
        show(task, null, null, total, false);
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

        Boolean percentProgressNullable = (Boolean) params.get("percentProgress");
        this.percentProgress = BooleanUtils.isTrue(percentProgressNullable);

        cancelButton.setVisible(cancelAllowed);
        getWindow().setCloseable(cancelAllowed);

        @SuppressWarnings("unchecked")
        final T total = (T) params.get("total");
        this.totalProgress = total;

        showProgress(0);

        BackgroundTask<T, V> wrapperTask = new LocalizedTaskWrapper<>(task, this, messages);
        wrapperTask.addProgressListener(new BackgroundTask.ProgressListenerAdapter<T, V>() {
            @Override
            public void onProgress(List<T> changes) {
                if (!changes.isEmpty()) {
                    Number lastProcessedValue = changes.get(changes.size() - 1);
                    showProgress(lastProcessedValue);
                }
            }
        });

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

    protected void showProgress(Number processedValue) {
        double value = processedValue.doubleValue() / totalProgress.doubleValue();

        taskProgressBar.setValue(value);

        if (!percentProgress) {
            progressText.setValue(messages.formatMessage(StringUtils.EMPTY, "backgroundWorkProgress.progressTextFormat", processedValue, totalProgress));
        } else {
            int percentValue = (int) Math.ceil(value * 100);
            progressText.setValue(messages.formatMessage(StringUtils.EMPTY, "backgroundWorkProgress.progressPercentFormat", percentValue));
        }
    }
}
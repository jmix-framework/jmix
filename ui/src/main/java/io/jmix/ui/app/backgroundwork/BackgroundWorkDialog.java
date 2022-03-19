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

package io.jmix.ui.app.backgroundwork;

import io.jmix.core.Messages;
import io.jmix.ui.executor.LocalizedTaskWrapper;
import io.jmix.ui.component.Button;
import io.jmix.ui.component.Label;
import io.jmix.ui.component.ProgressBar;
import io.jmix.ui.executor.BackgroundTask;
import io.jmix.ui.executor.BackgroundTaskHandler;
import io.jmix.ui.executor.BackgroundWorker;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Dialog that indicates progress of the background task, shows progress bar and processed items' message.
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
@UiController("backgroundWorkProgressScreen")
@UiDescriptor("background-work-dialog.xml")
@DialogMode(forceDialog = true)
public class BackgroundWorkDialog<T extends Number, V> extends Screen {

    @Autowired
    protected BackgroundWorker backgroundWorker;
    @Autowired
    protected Messages messages;

    @Autowired
    protected Label<String> text;
    @Autowired
    protected Label<String> progressText;
    @Autowired
    protected Button cancelButton;
    @Autowired
    protected ProgressBar taskProgressBar;

    protected BackgroundTask<T, V> task;
    protected boolean cancelAllowed = false;
    protected String message;
    protected Number total;
    protected boolean showProgressInPercentage;

    protected BackgroundTaskHandler<V> taskHandler;
    protected T totalProgress;

    @Subscribe
    public void onAfterShow(AfterShowEvent event) {

        if (message != null) {
            text.setValue(message);
        }

        cancelButton.setVisible(cancelAllowed);
        getWindow().setCloseable(cancelAllowed);

        if (total != null) {
            this.totalProgress = (T) total;
        }
        showProgress(0);

        BackgroundTask<T, V> taskWrapper = new LocalizedTaskWrapper<>(this.task, this, messages);
        taskWrapper.addProgressListener(new BackgroundTask.ProgressListenerAdapter<T, V>() {
            @Override
            public void onProgress(List<T> changes) {
                if (!changes.isEmpty()) {
                    Number lastProcessedValue = changes.get(changes.size() - 1);
                    showProgress(lastProcessedValue);
                }
            }
        });

        taskHandler = backgroundWorker.handle(taskWrapper);
        taskHandler.execute();
    }

    @Subscribe
    public void onBeforeClose(BeforeCloseEvent event) {
        if (!taskHandler.cancel()) {
            event.preventWindowClose();
        }
    }

    @Subscribe("cancelButton")
    public void onCancelButtonClick(Button.ClickEvent event) {
        close(WINDOW_CLOSE_ACTION);
    }

    public BackgroundTask<T, V> getTask() {
        return task;
    }

    public void setTask(BackgroundTask<T, V> task) {
        this.task = task;
    }

    public boolean isCancelAllowed() {
        return cancelAllowed;
    }

    public void setCancelAllowed(boolean cancelAllowed) {
        this.cancelAllowed = cancelAllowed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isShowProgressInPercentage() {
        return showProgressInPercentage;
    }

    public void setShowProgressInPercentage(boolean showProgressInPercentage) {
        this.showProgressInPercentage = showProgressInPercentage;
    }

    public Number getTotal() {
        return total;
    }

    public void setTotal(Number total) {
        this.total = total;
    }

    protected void showProgress(Number processedValue) {
        if (totalProgress != null) {
            updateProgress(processedValue);
        } else {
            taskProgressBar.setIndeterminate(true);
            progressText.setVisible(false);
        }
    }

    private void updateProgress(Number processedValue) {
        double value = processedValue.doubleValue() / totalProgress.doubleValue();
        taskProgressBar.setValue(value);
        if (!showProgressInPercentage) {
            progressText.setValue(messages.formatMessage(StringUtils.EMPTY,"backgroundWorkProgress.progressTextFormat", processedValue, totalProgress));
        } else {
            int percentValue = (int) Math.ceil(value * 100);
            progressText.setValue(messages.formatMessage(StringUtils.EMPTY,"backgroundWorkProgress.progressPercentFormat", percentValue));
        }
    }
}
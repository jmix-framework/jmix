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

package io.jmix.flowui.backgroundtask.impl;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.TimeSource;
import io.jmix.flowui.event.BackgroundTaskTimeoutEvent;
import io.jmix.flowui.backgroundtask.*;
import io.jmix.flowui.backgroundtask.BackgroundTaskWatchDog;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.lang.Nullable;

import static com.google.common.base.Preconditions.checkState;

public class TaskHandlerImpl<T, V> implements BackgroundTaskHandler<V> {

    private static final Logger log = LoggerFactory.getLogger(BackgroundWorker.class);

    private UIAccessor uiAccessor;
    private final TaskExecutor<T, V> taskExecutor;
    private final BackgroundTaskWatchDog backgroundTaskWatchDog;
    private ApplicationEventPublisher applicationEventPublisher;
    private TimeSource timeSource;

    private volatile boolean started = false;
    private volatile boolean timeoutHappens = false;

    private long startTimeStamp;

    private Registration viewDetachRegistration;
    private final UserDetails user;

    public TaskHandlerImpl(UIAccessor uiAccessor,
                           TaskExecutor<T, V> taskExecutor,
                           BackgroundTaskWatchDog backgroundTaskWatchDog,
                           ApplicationEventPublisher applicationEventPublisher,
                           UserDetails user,
                           TimeSource timeSource) {
        this.uiAccessor = uiAccessor;
        this.taskExecutor = taskExecutor;
        this.backgroundTaskWatchDog = backgroundTaskWatchDog;
        this.applicationEventPublisher = applicationEventPublisher;
        this.timeSource = timeSource;

        this.user = user;
        BackgroundTask<T, V> task = taskExecutor.getTask();
        if (task.getOwnerView() != null) {
            View<?> ownerView = task.getOwnerView();

            viewDetachRegistration = ownerView.addDetachListener(e -> onOwnerViewRemoved(e.getSource()));

            // remove close listener on done
            taskExecutor.setFinalizer(() -> {
                log.trace("Start task finalizer. Task: {}", taskExecutor.getTask());

                removeViewDetachListener();

                log.trace("Finish task finalizer. Task: {}", taskExecutor.getTask());
            });
        }
    }

    protected void onOwnerViewRemoved(Component ownerView) {
        if (log.isTraceEnabled()) {
            String viewClass = ownerView.getClass().getCanonicalName();
            log.trace("View removed. User: {}. View: {}", user.getUsername(), viewClass);
        }

        taskExecutor.cancelExecution();
    }

    @Override
    public final void execute() {
        checkState(!started, "Task is already started. Task: " + taskExecutor.getTask());

        this.started = true;

        this.startTimeStamp = timeSource.currentTimestamp().getTime();

        this.backgroundTaskWatchDog.manageTask(this);

        log.trace("Run task: {}. User: {}", taskExecutor.getTask(), user.getUsername());

        taskExecutor.startExecution();
    }

    @Override
    public final boolean cancel() {
        checkState(started, "Task is not running. Task: " + taskExecutor.getTask());

        boolean canceled = taskExecutor.cancelExecution();
        if (canceled) {
            removeViewDetachListener();

            BackgroundTask<T, V> task = taskExecutor.getTask();
            task.canceled();

            // Notify listeners
            for (BackgroundTask.ProgressListener listener : task.getProgressListeners()) {
                listener.onCancel();
            }

            if (log.isTraceEnabled()) {
                View<?> ownerView = getTask().getOwnerView();
                if (ownerView != null) {
                    String viewClass = ownerView.getClass().getCanonicalName();

                    log.trace("Task was cancelled. Task: {}. User: {}. View: {}", taskExecutor.getTask(), user.getUsername(), viewClass);
                } else {
                    log.trace("Task was cancelled. Task: {}. User: {}", taskExecutor.getTask(), user.getUsername());
                }
            }
        } else {
            log.trace("Task wasn't cancelled. Execution is already cancelled. Task: {}", taskExecutor.getTask());
        }

        return canceled;
    }

    protected void removeViewDetachListener() {
        if (viewDetachRegistration != null) {
            viewDetachRegistration.remove();
            viewDetachRegistration = null;
        }
    }

    /**
     * Join task thread to current
     * <br>
     * <b>Caution!</b>
     * Call this method only from synchronous gui action;
     *
     * @return Task result
     */
    @Nullable
    @Override
    public final V getResult() {
        checkState(started, "Task is not running");

        return taskExecutor.getResult();
    }

    /**
     * Cancels without events for tasks. Need to execute {@link #timeoutExceeded} after this method.
     */
    public final void closeByTimeout() {
        timeoutHappens = true;
        kill();
    }

    /**
     * Cancels without events for tasks.
     */
    public final void kill() {
        uiAccessor.access(() -> {
            removeViewDetachListener();

            if (log.isTraceEnabled()) {
                View<?> ownerView = getTask().getOwnerView();
                if (ownerView != null) {
                    String viewClass = ownerView.getClass().getCanonicalName();
                    log.trace("Task killed. Task: {}. User: {}. View: {}", taskExecutor.getTask(), user.getUsername(), viewClass);
                } else {
                    log.trace("Task killed. Task: {}. User: {}", taskExecutor.getTask(), user.getUsername());
                }
            }

            taskExecutor.cancelExecution();
        });
    }

    /**
     * Cancels with timeout exceeded event.
     */
    public final void timeoutExceeded() {
        uiAccessor.access(() -> {
            View<?> ownerView = getTask().getOwnerView();
            if (log.isTraceEnabled()) {
                if (ownerView != null) {
                    String viewClass = ownerView.getClass().getCanonicalName();
                    log.trace("Task timeout exceeded. Task: {}. View: {}", taskExecutor.getTask(), viewClass);
                } else {
                    log.trace("Task timeout exceeded. Task: {}", taskExecutor.getTask());
                }
            }

            checkState(started, "Task is not running");

            boolean canceled = taskExecutor.cancelExecution();
            if (canceled || timeoutHappens) {
                removeViewDetachListener();

                BackgroundTask<T, V> task = taskExecutor.getTask();
                boolean handled = task.handleTimeoutException();
                if (!handled) {
                    log.error("Unhandled timeout exception in background task. Task: " + task);
                    applicationEventPublisher.publishEvent(new BackgroundTaskTimeoutEvent(this, task));
                }
            }

            if (log.isTraceEnabled()) {
                if (ownerView != null) {
                    String viewClass = ownerView.getClass().getCanonicalName();
                    log.trace("Timeout was processed. Task: {}. View: {}", taskExecutor.getTask(), viewClass);
                } else {
                    log.trace("Timeout was processed. Task: {}", taskExecutor.getTask());
                }
            }
        });
    }

    @Override
    public final boolean isDone() {
        return taskExecutor.isDone();
    }

    @Override
    public final boolean isCancelled() {
        return taskExecutor.isCancelled();
    }

    @Override
    public final boolean isAlive() {
        return taskExecutor.inProgress() && started;
    }

    public final BackgroundTask<T, V> getTask() {
        return taskExecutor.getTask();
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public long getTimeoutMs() {
        return taskExecutor.getTask().getTimeoutMilliseconds();
    }
}
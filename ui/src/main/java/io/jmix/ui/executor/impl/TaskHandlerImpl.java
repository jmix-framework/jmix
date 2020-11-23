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

package io.jmix.ui.executor.impl;

import io.jmix.core.TimeSource;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.event.BackgroundTaskTimeoutEvent;
import io.jmix.ui.executor.*;
import io.jmix.ui.screen.FrameOwner;
import io.jmix.ui.screen.Screen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkState;
import static io.jmix.ui.screen.UiControllerUtils.addAfterDetachListener;

public class TaskHandlerImpl<T, V> implements BackgroundTaskHandler<V> {

    private static final Logger log = LoggerFactory.getLogger(BackgroundWorker.class);

    private UIAccessor uiAccessor;
    private final TaskExecutor<T, V> taskExecutor;
    private final WatchDog watchDog;
    private ApplicationEventPublisher applicationEventPublisher;
    private TimeSource timeSource;

    private volatile boolean started = false;
    private volatile boolean timeoutHappens = false;

    private long startTimeStamp;

    private Subscription afterDetachSubscription;
    private final UserDetails user;

    public TaskHandlerImpl(UIAccessor uiAccessor, TaskExecutor<T, V> taskExecutor, WatchDog watchDog, ApplicationEventPublisher applicationEventPublisher,
                           UserDetails user, TimeSource timeSource) {
        this.uiAccessor = uiAccessor;
        this.taskExecutor = taskExecutor;
        this.watchDog = watchDog;
        this.applicationEventPublisher = applicationEventPublisher;
        this.timeSource = timeSource;

        this.user = user;
        BackgroundTask<T, V> task = taskExecutor.getTask();
        if (task.getOwnerScreen() != null) {
            Screen ownerFrame = task.getOwnerScreen();

            afterDetachSubscription = addAfterDetachListener(ownerFrame, e -> ownerWindowRemoved(e.getSource()));

            // remove close listener on done
            taskExecutor.setFinalizer(() -> {
                log.trace("Start task finalizer. Task: {}", taskExecutor.getTask());

                removeAfterDetachListener();

                log.trace("Finish task finalizer. Task: {}", taskExecutor.getTask());
            });
        }
    }

    protected void ownerWindowRemoved(FrameOwner frameOwner) {
        if (log.isTraceEnabled()) {
            String windowClass = frameOwner.getClass().getCanonicalName();
            log.trace("Window removed. User: {}. Window: {}", user.getUsername(), windowClass);
        }

        taskExecutor.cancelExecution();
    }

    @Override
    public final void execute() {
        checkState(!started, "Task is already started. Task: " + taskExecutor.getTask().toString());

        this.started = true;

        this.startTimeStamp = timeSource.currentTimestamp().getTime();

        this.watchDog.manageTask(this);

        log.trace("Run task: {}. User: {}", taskExecutor.getTask(), user.getUsername());

        taskExecutor.startExecution();
    }

    @Override
    public final boolean cancel() {
        checkState(started, "Task is not running. Task: " + taskExecutor.getTask().toString());

        boolean canceled = taskExecutor.cancelExecution();
        if (canceled) {
            removeAfterDetachListener();

            BackgroundTask<T, V> task = taskExecutor.getTask();
            task.canceled();

            // Notify listeners
            for (BackgroundTask.ProgressListener listener : task.getProgressListeners()) {
                listener.onCancel();
            }

            if (log.isTraceEnabled()) {
                Screen ownerFrame = getTask().getOwnerScreen();
                if (ownerFrame != null) {
                    String windowClass = ownerFrame.getClass().getCanonicalName();

                    log.trace("Task was cancelled. Task: {}. User: {}. Frame: {}", taskExecutor.getTask(), user.getUsername(), windowClass);
                } else {
                    log.trace("Task was cancelled. Task: {}. User: {}", taskExecutor.getTask(), user.getUsername());
                }
            }
        } else {
            log.trace("Task wasn't cancelled. Execution is already cancelled. Task: {}", taskExecutor.getTask());
        }

        return canceled;
    }

    protected void removeAfterDetachListener() {
        if (afterDetachSubscription != null) {
            afterDetachSubscription.remove();
            afterDetachSubscription = null;
        }
    }

    /**
     * Join task thread to current <br>
     * <b>Caution!</b> Call this method only from synchronous gui action
     *;
     * @return Task result
     */
    @Nullable
    @Override
    public final V getResult() {
        checkState(started, "Task is not running");

        return taskExecutor.getResult();
    }

    /**
     * Cancel without events for tasks. Need to execute #timeoutExceeded after this method
     */
    public final void closeByTimeout() {
        timeoutHappens = true;
        kill();
    }

    /**
     * Cancel without events for tasks
     */
    public final void kill() {
        uiAccessor.access(() -> {
            Screen ownerFrame = getTask().getOwnerScreen();

            removeAfterDetachListener();

            if (log.isTraceEnabled()) {
                if (ownerFrame != null) {
                    String windowClass = ownerFrame.getClass().getCanonicalName();
                    log.trace("Task killed. Task: {}. User: {}. Frame: {}", taskExecutor.getTask(), user.getUsername(), windowClass);
                } else {
                    log.trace("Task killed. Task: {}. User: {}", taskExecutor.getTask(), user.getUsername());
                }
            }

            taskExecutor.cancelExecution();
        });
    }

    /**
     * Cancel with timeout exceeded event
     */
    public final void timeoutExceeded() {
        uiAccessor.access(() -> {
            Screen ownerFrame = getTask().getOwnerScreen();
            if (log.isTraceEnabled()) {
                if (ownerFrame != null) {
                    String windowClass = ownerFrame.getClass().getCanonicalName();
                    log.trace("Task timeout exceeded. Task: {}. Frame: {}", taskExecutor.getTask(), windowClass);
                } else {
                    log.trace("Task timeout exceeded. Task: {}", taskExecutor.getTask());
                }
            }

            checkState(started, "Task is not running");

            boolean canceled = taskExecutor.cancelExecution();
            if (canceled || timeoutHappens) {
                removeAfterDetachListener();

                BackgroundTask<T, V> task = taskExecutor.getTask();
                boolean handled = task.handleTimeoutException();
                if (!handled) {
                    log.error("Unhandled timeout exception in background task. Task: " + task.toString());
                    applicationEventPublisher.publishEvent(new BackgroundTaskTimeoutEvent(this, task));
                }
            }

            if (log.isTraceEnabled()) {
                if (ownerFrame != null) {
                    String windowClass = ownerFrame.getClass().getCanonicalName();
                    log.trace("Timeout was processed. Task: {}. Frame: {}", taskExecutor.getTask(), windowClass);
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
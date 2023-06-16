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

import io.jmix.core.TimeSource;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.UiBackgroundTaskProperties;
import io.jmix.flowui.backgroundtask.BackgroundTaskWatchDog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of {@link BackgroundTaskWatchDog} for {@link BackgroundWorker}.
 */
@ThreadSafe
public class BackgroundTaskWatchDogImpl implements BackgroundTaskWatchDog {
    private static final Logger log = LoggerFactory.getLogger(BackgroundTaskWatchDogImpl.class);

    public enum ExecutionStatus {
        NORMAL,
        TIMEOUT_EXCEEDED,
        SHOULD_BE_KILLED
    }

    protected TimeSource timeSource;
    protected UiBackgroundTaskProperties properties;

    private final Set<TaskHandlerImpl> watches = new LinkedHashSet<>();

    protected volatile boolean initialized;

    public BackgroundTaskWatchDogImpl(UiBackgroundTaskProperties properties,
                                      TimeSource timeSource) {
        this.properties = properties;
        this.timeSource = timeSource;
    }

    @EventListener(classes = {ContextRefreshedEvent.class, ContextStartedEvent.class})
    public void onContextRefreshed() {
        initialized = true;
    }

    @EventListener(ContextStoppedEvent.class)
    public void onContextStopped() {
        initialized = false;
    }

    @Override
    public synchronized void cleanupTasks() {
        if (!initialized) {
            return;
        }

        long actual = timeSource.currentTimestamp().getTime();

        List<TaskHandlerImpl> forRemove = new ArrayList<>();
        // copy watches since task.kill tries to remove task handler from watches
        for (TaskHandlerImpl task : new ArrayList<>(watches)) {
            if (task.isCancelled() || task.isDone()) {
                forRemove.add(task);
            } else {
                ExecutionStatus status = getExecutionStatus(actual, task);

                switch (status) {
                    case TIMEOUT_EXCEEDED:
                        task.closeByTimeout();
                        task.timeoutExceeded();
                        forRemove.add(task);
                        break;

                    case SHOULD_BE_KILLED:
                        task.kill();
                        forRemove.add(task);
                        break;

                    default:
                        break;
                }
            }
        }

        watches.removeAll(forRemove);
    }

    @Override
    public synchronized void stopTasks() {
        // copy watches since task.kill tries to remove task handler from watches
        ArrayList<TaskHandlerImpl> taskHandlers = new ArrayList<>(watches);
        watches.clear();
        for (TaskHandlerImpl task : taskHandlers) {
            task.kill();
        }
    }

    @Override
    public synchronized int getActiveTasksCount() {
        return watches.size();
    }

    @Override
    public synchronized void manageTask(TaskHandlerImpl taskHandler) {
        watches.add(taskHandler);
    }

    @Override
    public synchronized void removeTask(TaskHandlerImpl taskHandler) {
        watches.remove(taskHandler);
    }

    protected ExecutionStatus getExecutionStatus(long actualTimeMs, TaskHandlerImpl taskHandler) {
        long timeout = taskHandler.getTimeoutMs();

        // kill tasks, which do not update status for latency milliseconds
        long latencyMs = properties.getTaskKillingLatency().toMillis();
        if (timeout > 0 && (actualTimeMs - taskHandler.getStartTimeStamp()) > timeout + latencyMs) {
            if (log.isTraceEnabled()) {
                log.trace("Latency timeout exceeded for task: {}", taskHandler.getTask());
            }
            return ExecutionStatus.SHOULD_BE_KILLED;
        }

        if (timeout > 0 && (actualTimeMs - taskHandler.getStartTimeStamp()) > timeout) {
            if (log.isTraceEnabled()) {
                log.trace("Timeout exceeded for task: {}", taskHandler.getTask());
            }
            return ExecutionStatus.TIMEOUT_EXCEEDED;
        }

        return ExecutionStatus.NORMAL;
    }
}
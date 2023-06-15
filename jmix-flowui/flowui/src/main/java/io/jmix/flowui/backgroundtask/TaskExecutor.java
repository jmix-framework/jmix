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

package io.jmix.flowui.backgroundtask;

import org.springframework.lang.Nullable;

public interface TaskExecutor<T, V> {
    /**
     * Executes background task.
     */
    @ExecutedOnUIThread
    void startExecution();

    /**
     * Cancels the execution and removes task form {@link BackgroundTaskWatchDog} and from {@link BackgroundTaskManager}.
     * @return {@code true} if the canceling was successful
     */
    @ExecutedOnUIThread
    boolean cancelExecution();

    /**
     * Joins task thread to current and waits if task is not finished.
     * @return result of the task
     */
    @ExecutedOnUIThread
    @Nullable
    V getResult();

    /**
     * @return the task
     */
    BackgroundTask<T, V> getTask();

    boolean isCancelled();

    boolean isDone();

    boolean inProgress();

    /**
     * Sets done handler for clear resources.
     *
     * @param finalizer Runnable handler
     */
    void setFinalizer(Runnable finalizer);

    Runnable getFinalizer();

    /**
     * Handles changes from working thread.
     *
     * @param changes Changes
     */
    @SuppressWarnings({"unchecked"})
    void handleProgress(T... changes);
}